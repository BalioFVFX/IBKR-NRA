package parser

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import currency.Currency
import parser.output.Dividend
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DividendsParser(
    private val csvReader: CsvReader,
) {

    companion object {
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy;HH:mm:ss")
    }

    fun parse(file: File): Result<List<Dividend>> {
        val rows = csvReader.readAllWithHeader(file)
        val fileRows = mutableListOf<FileRow>()

        for (row in rows) {
            if (row.size != 10) {
                return Result.failure(IllegalArgumentException("Невалиден брой колони"))
            }

            if (isRowAHeader(row)) {
                println("Header row, skipping!")
                continue
            }

            try {
                fileRows.add(parseFileRow(row))
            } catch (ex: Exception) {
                return Result.failure(ex)
            }
        }

        return try {
            val dividends = fileRows.groupBy { it.actionId }
                .map { entry ->
                    val totalTaxes = entry.value.filter { it.type == Type.TAX }.sumOf { it.amount }
                    val dividends = entry.value.filter { it.type == Type.DIVIDEND }
                    val totalDividends = dividends.sumOf { it.amount }

                    val dividend = dividends.first()

                    Dividend(
                        currency = dividend.currency,
                        ticker = dividend.symbol,
                        description = dividend.description,
                        isin = dividend.isin,
                        issuerCountry = dividend.issuerCountryCode,
                        dateTime = dividend.dateTime,
                        amount = totalDividends,
                        taxAmount = totalTaxes,
                        actionId = dividend.actionId,
                    )
                }

            Result.success(dividends)
        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }

    private fun parseFileRow(row: Map<String, String>): FileRow {
        return FileRow(
            clientAccountId = row[Headers.CLIENT_ACCOUNT_ID]!!,
            currency = Currency.parse(row[Headers.CURRENCY_PRIMARY]!!),
            symbol = row[Headers.SYMBOL]!!,
            description = row[Headers.DESCRIPTION]!!,
            isin = row[Headers.ISIN]!!,
            issuerCountryCode = row[Headers.ISSUER_COUNTRY_CODE]!!,
            dateTime = HeaderParser.tryParseLocalDateTime(
                header = Headers.DATE_TIME,
                row = row,
                formatter = DATE_TIME_FORMATTER,
            ),
            amount = HeaderParser.tryParseBigDecimal(
                header = Headers.AMOUNT,
                row = row,
            ),
            type = Type.parse(row[Headers.TYPE]!!),
            actionId = row[Headers.ACTION_ID]!!,
        )
    }

    private fun isRowAHeader(row: Map<String, String>): Boolean {
        return row.containsValue(Headers.CLIENT_ACCOUNT_ID) &&
                row.containsValue(Headers.CURRENCY_PRIMARY) &&
                row.containsValue(Headers.SYMBOL) &&
                row.containsValue(Headers.DESCRIPTION) &&
                row.containsValue(Headers.ISIN) &&
                row.containsValue(Headers.ISSUER_COUNTRY_CODE) &&
                row.containsValue(Headers.DATE_TIME) &&
                row.containsValue(Headers.AMOUNT) &&
                row.containsValue(Headers.TYPE) &&
                row.containsValue(Headers.ACTION_ID)
    }

    private object Headers {
        const val CLIENT_ACCOUNT_ID = "ClientAccountID"
        const val CURRENCY_PRIMARY = "CurrencyPrimary"
        const val SYMBOL = "Symbol"
        const val DESCRIPTION = "Description"
        const val ISIN = "ISIN"
        const val ISSUER_COUNTRY_CODE = "IssuerCountryCode"
        const val DATE_TIME = "Date/Time"
        const val AMOUNT = "Amount"
        const val TYPE = "Type"
        const val ACTION_ID = "ActionID"
    }

    private data class FileRow(
        val clientAccountId: String,
        val currency: Currency,
        val symbol: String,
        val description: String,
        val isin: String,
        val issuerCountryCode: String,
        val dateTime: LocalDateTime,
        val amount: BigDecimal,
        val type: Type,
        val actionId: String,
    )

    private enum class Type(val fileValue: String) {
        TAX("Withholding Tax"),
        DIVIDEND("Dividends");

        companion object {
            fun parse(value: String): Type {
                return when (value) {
                    TAX.fileValue -> {
                        TAX
                    }
                    DIVIDEND.fileValue -> {
                        DIVIDEND
                    }
                    else -> {
                        throw IllegalArgumentException("Unknown type: $value")
                    }
                }
            }
        }
    }
}