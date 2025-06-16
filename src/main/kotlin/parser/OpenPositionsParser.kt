package parser

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import currency.Currency
import parser.output.OpenPosition
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Open Positions Flex query parser
 */
class OpenPositionsParser(
    private val reader: CsvReader,
) {
    companion object {
        val OPEN_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy;HH:mm:ss")
    }

    fun parse(file: File): Result<List<OpenPosition>> {
        val rows: List<Map<String, String>> = reader.readAllWithHeader(file)

        val openPositions = mutableListOf<OpenPosition>()
        var totalSummaries = 0

        for (row in rows) {
            if (isRowAHeader(row)) {
                println("Found a header row. Skipping!")
                continue
            }

            val levelOfDetail = row[Headers.LEVEL_OF_DETAIL]

            if (levelOfDetail == "SUMMARY") {
                totalSummaries += 1
                println("Summary, skipping!")
                continue
            } else if (levelOfDetail != "LOT") {
                return Result.failure(IllegalArgumentException("Unknown level of detail: $levelOfDetail"))
            }

            try {
                openPositions.add(
                    element = parseOpenPosition(
                        row = row,
                    )
                )
            } catch (ex: Exception) {
                return Result.failure(ex)
            }
        }

        println("Parsed ${openPositions.size} LOTS and $totalSummaries Summaries. Total rows: ${rows.size} (excluding headers)")

        return Result.success(openPositions)
    }

    private fun parseOpenPosition(row: Map<String, String>): OpenPosition {
        return OpenPosition(
            accountId = row[Headers.ACCOUNT_ID]!!,
            currency = Currency.parse(currencyString = row[Headers.CURRENCY]!!),
            symbol = row[Headers.SYMBOL]!!,
            isin = row[Headers.ISIN]!!,
            listingExchange = row[Headers.LISTING_EXCHANGE]!!,
            quantity = HeaderParser.tryParseBigDecimal(
                header = Headers.QUANTITY,
                row = row,
            ),
            openPrice = HeaderParser.tryParseBigDecimal(
                header = Headers.OPEN_PRICE,
                row = row,
            ),
            costBasisPrice = HeaderParser.tryParseBigDecimal(
                header = Headers.COST_BASIS_PRICE,
                row = row,
            ),
            costBasisMoney = HeaderParser.tryParseBigDecimal(
                header = Headers.COST_BASIS_MONEY,
                row = row,
            ),
            openDate = HeaderParser.tryParseLocalDateTime(
                header = Headers.OPEN_DATE_TIME,
                row = row,
                formatter = OPEN_DATE_FORMATTER,
            ),
            transactionId = row[Headers.TRANSACTION_ID]!!,
        )
    }

    /**
     * @return Whether the current row is a header
     */
    private fun isRowAHeader(row: Map<String, String>): Boolean {
        return row.containsValue(Headers.ACCOUNT_ID) &&
                row.containsValue(Headers.CURRENCY) &&
                row.containsValue(Headers.SYMBOL) &&
                row.containsValue(Headers.ISIN) &&
                row.containsValue(Headers.LISTING_EXCHANGE) &&
                row.containsValue(Headers.QUANTITY) &&
                row.containsValue(Headers.OPEN_PRICE) &&
                row.containsValue(Headers.COST_BASIS_PRICE) &&
                row.containsValue(Headers.COST_BASIS_MONEY) &&
                row.containsValue(Headers.LEVEL_OF_DETAIL) &&
                row.containsValue(Headers.OPEN_DATE_TIME) &&
                row.containsValue(Headers.TRANSACTION_ID)
    }

    private object Headers {
        const val ACCOUNT_ID = "ClientAccountID"
        const val CURRENCY = "CurrencyPrimary"
        const val SYMBOL = "Symbol"
        const val ISIN = "ISIN"
        const val LISTING_EXCHANGE = "ListingExchange"
        const val QUANTITY = "Quantity"
        const val OPEN_PRICE = "OpenPrice"
        const val COST_BASIS_PRICE = "CostBasisPrice"
        const val COST_BASIS_MONEY = "CostBasisMoney"
        const val LEVEL_OF_DETAIL = "LevelOfDetail"
        const val OPEN_DATE_TIME = "OpenDateTime"
        const val TRANSACTION_ID = "OriginatingTransactionID"
    }
}

