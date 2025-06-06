package parser.output

import currency.Currency
import java.math.BigDecimal
import java.time.LocalDateTime

data class Trade(
    /**
     * Account id that made the trade
     */
    val accountId: String,
    /**
     * Currency of the trade
     */
    val currency: Currency,
    /**
     * The name of the instrument
     */
    val symbol: String,
    /**
     * ISIN number
     */
    val isin: String,
    /**
     * Stock exchange, such as IBIS2 (XETRA), NASDAQ
     */
    val listingExchange: String,
    /**
     * When was the trade executed
     */
    val dateTime: LocalDateTime,
    /**
     * Either BUY or SELL
     */
    val buySell: TradeType,
    /**
     * The quantity of the trade
     */
    val quantity: BigDecimal,
    /**
     * The original price of the trade. Does not contain commissions.
     */
    val tradePrice: BigDecimal,
    /**
     * All the commissions related to this trade
     */
    val commission: BigDecimal,
    /**
     * The currency of commission
     */
    val commissionCurrency: Currency,
    /**
     * Transaction ID
     */
    val transactionId: String,

    )