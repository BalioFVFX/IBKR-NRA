package parser.output

import currency.Currency
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Output of OpenPositionsParser
 */
data class OpenPosition(
    /**
     * The account id that created this order
     */
    val accountId: String,
    /**
     * Order currency
     */
    val currency: Currency,
    /**
     * Instrument name
     */
    val symbol: String,
    /**
     * ISIN Number
     */
    val isin: String,
    /**
     * Stock exchange, such as IBIS2 (XETRA), NASDAQ
     */
    val listingExchange: String,
    /**
     * Quantity of this order
     */
    val quantity: BigDecimal,
    /**
     * Original price (without commissions) + (commission / quantity)
     */
    val openPrice: BigDecimal,
    val costBasisPrice: BigDecimal,
    /**
     * openPrice * quantity
     */
    val costBasisMoney: BigDecimal,
    /**
     * Date of order
     */
    val openDate: LocalDateTime,
    /**
     * Transaction ID
     */
    val transactionId: String,
)