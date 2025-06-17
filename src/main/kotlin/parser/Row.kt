package parser

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Row(
    private val map: Map<String, String>
) {

    /**
     * @return [String] value for the given [header]
     * @throws IllegalArgumentException If this Row does not contain [header]
     */
    fun parseString(header: String): String = extract(header) { it }

    /**
     * @return [BigDecimal] value for the given [header]
     * @throws IllegalArgumentException If this Row does not contain [header] or the value behind
     * the provided header is not [BigDecimal]
     */
    fun parseBigDecimal(
        header: String,
    ): BigDecimal = extract(header) {
        it.toBigDecimal()
    }

    /**
     * @return [LocalDateTime] value for the given [header]
     * @throws IllegalArgumentException If this Row does not contain [header] or the value behind
     * the provided header is not [LocalDateTime]
     */
    fun parseLocalDateTime(
        header: String,
        formatter: DateTimeFormatter,
    ): LocalDateTime = extract(header) {
        LocalDateTime.parse(
            map[header]!!,
            formatter,
        )
    }

    private fun<T> extract(
        header: String,
        action: (String) -> T,
    ): T {
        return try {
            action.invoke(map[header]!!)
        } catch (ex: Exception) {
            throw IllegalArgumentException(
                "Неуспешно обработване на: $header",
                ex,
            )
        }
    }
}
