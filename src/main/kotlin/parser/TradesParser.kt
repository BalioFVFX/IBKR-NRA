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

        for (row in rows) {
            if (isRowAHeader(row)) {
                println("Found a header row. Skipping!")
                continue
            }

            val trade = try {
                parseTrade(row)
            } catch (ex: Exception) {
                println("Failed to parse trade")
                return Result.failure(ex)
            }

            trades.add(trade)
        }

        return Result.success(trades)
    }

    private fun parseTrade(row: Map<String, String>): Trade {
        return Trade(
            accountId = row[Headers.ACCOUNT_ID]!!,
            currency = Currency.parse(currencyString = row[Headers.CURRENCY]!!),
            symbol = row[Headers.SYMBOL]!!,
            isin = row[Headers.ISIN]!!,
            listingExchange = row[Headers.LISTING_EXCHANGE]!!,
            dateTime = HeaderParser.tryParseLocalDateTime(
                header = Headers.DATE_TIME,
                row = row,
                formatter = DATE_TIME_FORMATTER,
            ),
            buySell = TradeType.parse(tradeTypeString = row[Headers.BUY_SELL]!!),
            quantity = HeaderParser.tryParseBigDecimal(
                header = Headers.QUANTITY,
                row = row,
            ),
            tradePrice = HeaderParser.tryParseBigDecimal(
                header = Headers.TRADE_PRICE,
                row = row,
            ),
            commission = HeaderParser.tryParseBigDecimal(
                header = Headers.COMMISSION,
                row = row
            ),
            commissionCurrency = Currency.parse(currencyString = row[Headers.COMMISSION_CURRENCY]!!),
            transactionId = row[Headers.TRANSACTION_ID]!!,
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

