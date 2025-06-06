package currency

import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import currency.Currency
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import kotlin.random.Random

class LevExchanger(
    private val httpClient: HttpClient,
    private val levCacheManager: LevCacheManager,
) {

    companion object {
        private val EUR_TO_BGN = BigDecimal("1.95583")
    }

    /**
     * Converts the given currency to Lev for the provided date. If there are no rates for the given date it searches
     * for a next date with rate.
     *
     * @return The Lev for the given or closest date.
     */
    suspend fun exchange(
        fromDate: LocalDate,
        currency: Currency,
    ): Result<Pair<LocalDate, BigDecimal>> {

        if (currency == Currency.EUR) {
            return Result.success(fromDate to EUR_TO_BGN)
        }

        val cachedLev = levCacheManager.getCachedLev(forDate = fromDate)

        if (cachedLev != null) {
            return Result.success(fromDate to cachedLev)
        }

        for (i in 0 until 7) {
            val delayMs = Random.Default.nextLong(from = 1000L, until =  10000L)
            println("Delay: $delayMs")
            delay(delayMs)

            val currentDate = if (i == 0) {
                fromDate
            } else {
                fromDate.plusDays(i.toLong())
            }

            val request = HttpRequest.newBuilder()
                .uri(createUrl(currentDate))
                .GET()
                .build()

            try {
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() != 200) {
                    println("Lev exchange request failed with status code: ${response.statusCode()}")

                    return Result.failure(IllegalStateException("Lev exchange request failed with status code: ${response.statusCode()}"))
                }

                val body = response.body()

                if (body == null) {
                    return Result.failure(java.lang.IllegalStateException("Lev exchange request failed. Body not found"))
                }

                val document = Jsoup.parse(body)
                val tableBox = document.selectFirst("div.table_box")

                if (tableBox == null) {
                    println("Page does not have exchange table, skip. Current try: $i")
                    continue
                }

                val usdRow = tableBox.select("td:contains(USD)").first()

                if (usdRow == null) {
                    println("Page does not have USD row, skip. Current try: $i")
                    continue
                }

                val bgnStringValue = usdRow.parent()?.select("td")?.takeIf { it.size > 4 }?.get(3)?.text()

                if (bgnStringValue.isNullOrBlank()) {
                    println("Page does not have USD value, skip. Current try: $i")
                    continue
                }

                val bgn = bgnStringValue.toBigDecimalOrNull()

                if (bgn == null) {
                    return Result.failure(IllegalStateException("Could not parse USD value"))
                }

                levCacheManager.cache(
                    forDate = fromDate,
                    value = bgn,
                )

                return Result.success(currentDate to bgn)

            } catch (ex: Exception) {
                println("Lev exchange request failed.")

                return Result.failure(ex)
            }
        }

        return Result.failure(IllegalStateException("Could not find lev exchange"))
    }

    private fun createUrl(fromDate: LocalDate): URI {
        val day = formattedUrlNumber(fromDate.dayOfMonth)
        val month = formattedUrlNumber(fromDate.monthValue)
        val year = formattedUrlNumber(fromDate.year)

        val url = "https://www.bnb.bg/Statistics/StExternalSector/StExchangeRates/StERForeignCurrencies/" +
                "index.htm?downloadOper=&group1=first&firstDays=${day}&firstMonths=${month}&firstYear=${year}&search=true" +
                "&showChart=false&showChartButton=false"

        return URI.create(url)
    }

    private fun formattedUrlNumber(number: Int): String {
        return if (number < 10) {
            "0$number"
        } else {
            number.toString()
        }
    }
}