package util

import com.github.doyaaaaaken.kotlincsv.client.CsvReader

class CountryExtractor(
    private val csvReader: CsvReader,
    private val fileProvider: FileProvider,
) {

    private val cache = mutableMapOf<String, String>()

    /**
     * @return Country for the provided [ticker] or `null` if country is not found
     */
    fun extractFromTicker(ticker: String): String? {
        if (cache.isNotEmpty()) {
            return cache[ticker.uppercase()]
        }

        csvReader.readAll(
            ips = fileProvider.provideResource(resourceName = "ticker_countries.csv"),
        )
            .associateTo(
            destination = cache,
        ) { column ->
            column[0].uppercase() to column[1]
        }

        cache.remove("TICKER")

        return cache[ticker.uppercase()]
    }
}

// Sources:
// https://www.justetf.com/en/etf-profile.html?isin=IE00BK5BQT80#basics (VWCE)
// https://www.sec.gov/edgar/browse/?CIK=320193&owner=exclude (AAPL)
// https://www.sec.gov/edgar/browse/?CIK=1639920&owner=exclude (SPOT)
// https://www.sec.gov/edgar/browse/?CIK=1736541&owner=exclude (NIO)