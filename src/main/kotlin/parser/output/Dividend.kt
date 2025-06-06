package parser.output

import currency.Currency
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Output of [parser.DividendsParser]
 */
data class Dividend(
    /**
     * Currency of dividend
     */
    val currency: Currency,
    /**
     * Symbol
     */
    val ticker: String,
    /**
     * Description
     */
    val description: String,
    /**
     * ISIN
     */
    val isin: String,
    /**
     * The country that issued the dividend
     */
    val issuerCountry: String,
    /**
     * Issued date
     */
    val dateTime: LocalDateTime,
    /**
     * The net amount of received dividend
     */
    val amount: BigDecimal,
    /**
     * Withhold tax
     */
    val taxAmount: BigDecimal,
    /**
     * ID for the dividend its taxes
     */
    val actionId: String,
)