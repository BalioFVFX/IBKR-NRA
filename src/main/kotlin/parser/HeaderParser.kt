package parser

import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object HeaderParser {

    fun tryParseBigDecimal(
        header: String,
        row: Map<String, String>,
    ): BigDecimal {
        return tryParseBlock(header) {
            row[header]!!.toBigDecimal()
        }
    }

    fun tryParseLocalDateTime(
        header: String,
        row: Map<String, String>,
        formatter: DateTimeFormatter,
    ): LocalDateTime {
        return tryParseBlock(header) {
            LocalDateTime.parse(
                row[header]!!,
                formatter,
            )
        }
    }

    private fun <T> tryParseBlock(
        header: String,
        action: () -> T,
    ): T {
        return try {
            action.invoke()
        } catch (ex: Exception) {
            throw IllegalArgumentException(
                "Failed to parse: $header",
                ex,
            )
        }
    }
}