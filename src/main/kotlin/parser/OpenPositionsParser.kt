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

        var rowIndex = 0
        for (csvRow in rows) {
            rowIndex += 1

            if (isRowAHeader(csvRow)) {
                println("Found a header row. Skipping!")
                continue
            }

            val levelOfDetail = csvRow[Headers.LEVEL_OF_DETAIL]

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
                        row = Row(map = csvRow),
                    )
                )
            } catch (ex: Exception) {
                return Result.failure(
                    IllegalArgumentException(
                        "Неуспешно обработване на ред: $rowIndex. Причина: ${ex.message}",
                    )
                )
            }
        }

        println("Parsed ${openPositions.size} LOTS and $totalSummaries Summaries. Total rows: ${rows.size} (excluding headers)")

        return Result.success(openPositions)
    }

    private fun parseOpenPosition(row: Row): OpenPosition {
        return OpenPosition(
            accountId = row.parseString(Headers.ACCOUNT_ID),
            currency = Currency.parse(currencyString = row.parseString(Headers.CURRENCY)),
            symbol = row.parseString(Headers.SYMBOL),
            isin = row.parseString(header = Headers.ISIN, allowEmpty = true),
            listingExchange = row.parseString(header = Headers.LISTING_EXCHANGE, allowEmpty = true),
            quantity = row.parseBigDecimal(
                header = Headers.QUANTITY,
            ),
            openPrice = row.parseBigDecimal(
                header = Headers.OPEN_PRICE,
            ),
            costBasisPrice = row.parseBigDecimal(
                header = Headers.COST_BASIS_PRICE,
            ),
            costBasisMoney = row.parseBigDecimal(
                header = Headers.COST_BASIS_MONEY,
            ),
            openDate = row.parseLocalDateTime(
                header = Headers.OPEN_DATE_TIME,
                formatter = OPEN_DATE_FORMATTER,
            ),
            transactionId = row.parseString(Headers.TRANSACTION_ID),
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

