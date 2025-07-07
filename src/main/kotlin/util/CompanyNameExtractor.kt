package util

import com.github.doyaaaaaken.kotlincsv.client.CsvReader

class CompanyNameExtractor(
    private val csvReader: CsvReader,
    private val fileProvider: FileProvider,
) {

    private val cache = hashMapOf<String, String>()

    /**
     *
     * @param [ticker] The symbol of the company such as AAPL (Apple)
     *
     * @return The name of the company behind the provided [ticker] or `null` if not found
     */
    fun extract(ticker: String): String? {
        if (!cache.isEmpty()) {
            return cache[ticker.uppercase()]
        }

        csvReader.readAll(
            ips = fileProvider.provideResource(resourceName = "company_names.csv"),
        ).associateTo(
            destination = cache,
            transform = { column ->
                column[0].uppercase() to column[1]
            }
        )

        return cache[ticker.uppercase()]
    }
}