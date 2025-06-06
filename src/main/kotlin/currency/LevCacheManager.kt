package currency

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import util.FileProvider
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * USD-BGN cache manager. Thread unsafe
 */
class LevCacheManager(
    private val csvReader: CsvReader,
    private val csvWriter: CsvWriter,
    private val fileProvider: FileProvider,
) {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    private val cacheFile by lazy {
        fileProvider.provide("cache").mkdirs()
        val file = fileProvider.provide("cache/usd-bgn.csv").apply { createNewFile() }
        file
    }

    private val memoryCache = mutableMapOf<LocalDate, BigDecimal>()

    /**
     * @return Lev for the provided date. Returns `null` if there is no cached value for the given date
     */
    fun getCachedLev(
        forDate: LocalDate,
    ): BigDecimal? {
        if (memoryCache.isEmpty()) {
            csvReader.readAll(cacheFile)
                .associateTo(
                    destination = memoryCache,
                    transform = { row ->
                        LocalDate.parse(row[0], DATE_FORMATTER) to BigDecimal(row[1])
                    }
                )
        }

        return memoryCache[forDate]
    }

    /**
     * Updates the cache for the given Date [forDate] with [value]
     */
    fun cache(
        forDate: LocalDate,
        value: BigDecimal,
    ) {
        memoryCache[forDate] = value

        csvWriter.writeAll(
            targetFile = cacheFile,
            rows = memoryCache.map { entry -> listOf(DATE_FORMATTER.format(entry.key), entry.value) },
            append = false,
        )
    }
}