package parser

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import currency.Currency
import parser.output.Trade
import parser.output.TradeType
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Trade confirmation execution flex query parser
 */
class TradesParser(
    private val reader: CsvReader,
) {

    fun parse(file: File): Result<List<Trade>> {
        val rows = reader.readAllWithHeader(file = file)

        val trades = mutableListOf<Trade>()

        var rowIndex = 0
        for (csvRow in rows) {
            rowIndex++

            if (isRowAHeader(csvRow)) {
                println("Found a header row. Skipping!")
                continue
            }

            val trade = try {
                parseTrade(row = Row(map = csvRow))
            } catch (ex: Exception) {
                return Result.failure(
                    exception = IllegalArgumentException(
                        "Неуспешна обработка на ред: $rowIndex. Причина: ${ex.message}"
                    )
                )
            }

            trades.add(trade)
        }

        return Result.success(trades)
    }

    private fun parseTrade(row: Row): Trade {
        return Trade(
            accountId = row.parseString(Headers.ACCOUNT_ID),
            currency = Currency.parse(
                currencyString = row.parseString(Headers.CURRENCY),
            ),
            symbol = row.parseString(Headers.SYMBOL),
            isin = row.parseString(Headers.ISIN),
            listingExchange = row.parseString(Headers.LISTING_EXCHANGE),
            dateTime = row.parseLocalDateTime(
                header = Headers.DATE_TIME,
                formatter = DATE_TIME_FORMATTER,
            ),
            buySell = TradeType.parse(tradeTypeString = row.parseString(Headers.BUY_SELL)),
            quantity = row.parseBigDecimal(
                header = Headers.QUANTITY,
            ),
            tradePrice = row.parseBigDecimal(
                header = Headers.TRADE_PRICE,
            ),
            commission = row.parseBigDecimal(
                header = Headers.COMMISSION,
            ),
            commissionCurrency = Currency.parse(currencyString = row.parseString(Headers.COMMISSION_CURRENCY)),
            transactionId = row.parseString(Headers.TRANSACTION_ID),
        )
    }

    /**
     * @return Whether this row is a header
     */
    private fun isRowAHeader(row: Map<String, String>): Boolean {
        return row.containsValue(Headers.ACCOUNT_ID) &&
                row.containsValue(Headers.CURRENCY) &&
                row.containsValue(Headers.SYMBOL) &&
                row.containsValue(Headers.ISIN) &&
                row.containsValue(Headers.LISTING_EXCHANGE) &&
                row.containsValue(Headers.QUANTITY) &&
                row.containsValue(Headers.DATE_TIME) &&
                row.containsValue(Headers.TRANSACTION_ID) &&
                row.containsValue(Headers.BUY_SELL) &&
                row.containsValue(Headers.TRADE_PRICE) &&
                row.containsValue(Headers.COMMISSION) &&
                row.containsValue(Headers.COMMISSION_CURRENCY)
    }

    companion object {

        val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy;HH:mm:ss")

        private object Headers {
            const val ACCOUNT_ID = "ClientAccountID"
            const val CURRENCY = "CurrencyPrimary"
            const val SYMBOL = "Symbol"
            const val ISIN = "ISIN"
            const val LISTING_EXCHANGE = "ListingExchange"
            const val DATE_TIME = "DateTime"
            const val TRANSACTION_ID = "TransactionID"
            const val BUY_SELL = "Buy/Sell"
            const val QUANTITY = "Quantity"
            const val TRADE_PRICE = "TradePrice"
            const val COMMISSION = "IBCommission"
            const val COMMISSION_CURRENCY = "IBCommissionCurrency"
        }
    }

}

