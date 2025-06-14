package converter

import converter.result.ConverterResult
import converter.result.NapOpenPosition
import currency.LevExchanger
import parser.output.OpenPosition
import parser.output.Trade
import util.CountryExtractor
import util.PercentageCalculator
import java.time.format.DateTimeFormatter

class NapOpenPositionConverter(
    private val levExchanger: LevExchanger,
    private val countryExtractor: CountryExtractor,
    private val percentageCalculator: PercentageCalculator,
) {

    companion object {
        private val OPEN_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    val progress = percentageCalculator.percentage

    suspend fun convert(
        trades: List<Trade>,
        openPositions: List<OpenPosition>,
    ): ConverterResult<NapOpenPosition> {

        percentageCalculator.reset(maximum = openPositions.size)

        val errors = mutableListOf<String>()
        val result = mutableListOf<NapOpenPosition>()

        for (openPosition in openPositions) {
            percentageCalculator.increment()

            val trade = trades.firstOrNull { trade ->
                trade.transactionId == openPosition.transactionId
            }

            println("Converting trade: $trade")

            if (trade == null) {
                errors.add(
                    "Не е намерена сделка за отворена позиция: ${openPosition.symbol} с идентификатор за" +
                            " транзакция: ${openPosition.transactionId}"
                )

                result.add(createErrorNapOpenPosition(orderId = openPosition.transactionId))

                continue
            }

            if (trade.symbol != openPosition.symbol) {
                errors.add("Не съвпадащ ticker за сделка и отворена позиция с идентификатор на транзакция: ${openPosition.transactionId}")
            }

            if (trade.isin != openPosition.isin) {
                errors.add("Не съвпадащ ISIN за сделка и отворена позиция с идентификатор на транзакция: ${openPosition.transactionId}")
            }


            val country = run {
                val result = getCountry(ticker = openPosition.symbol)

                if (result == null) {
                    errors.add("Не е намерена държава за ${openPosition.symbol} с идентификатор на транзакция: ${openPosition.transactionId}")
                    "Грешка"
                } else {
                    result
                }
            }

            val quantity = openPosition.quantity.toString()
            val date = run {
                val formattedDate = openPosition.openDate.format(OPEN_DATE_FORMATTER)

                if (openPosition.openDate != trade.dateTime) {
                    val tradeFormattedDate = trade.dateTime.format(OPEN_DATE_FORMATTER)

                    errors.add(
                        "Не съвпадаща дата на отворена позиция и сделка. " +
                                "Символ: ${openPosition.symbol} с идентификатор на транзакция ${openPosition.transactionId}, " +
                                "дата на отворена позиция: $formattedDate," +
                                "дата на сделка: $tradeFormattedDate"
                    )
                }

                formattedDate
            }

            val currencyPrice = trade.tradePrice.multiply(openPosition.quantity).toString()
            val levResult = levExchanger.exchange(
                fromDate = openPosition.openDate.toLocalDate(),
                currency = openPosition.currency,
            )

            val levPrice = run {
                if (levResult.isFailure) {
                    return@run "Не е намерен курс за лев за дата. ID на транзакция: ${openPosition.transactionId}"
                }

                val actualLevDate = levResult.getOrThrow().first

                if (actualLevDate != openPosition.openDate.toLocalDate()) {
                    errors.add(
                        "Не е намерен курс на лев за дата: ${openPosition.openDate.format(OPEN_DATE_FORMATTER)}." +
                                "Използван курс от дата: ${actualLevDate.format(OPEN_DATE_FORMATTER)}"
                    )
                }

                trade.tradePrice
                    .multiply(levResult.getOrThrow().second) // Lev
                    .multiply(openPosition.quantity)
                    .toString()
            }

            result.add(
                NapOpenPosition(
                    country = country,
                    quantity = quantity,
                    date = date,
                    currencyPrice = currencyPrice,
                    levPrice = levPrice,
                    detailOriginalCurrency = trade.currency.toString(),
                    detailOriginalPrice = trade.tradePrice.toString(),
                    detailLevDate = levResult.getOrNull()?.first?.format(OPEN_DATE_FORMATTER) ?: "Грешка",
                )
            )
        }

        return ConverterResult(
            data = result,
            errors = errors,
        )
    }

    private fun getCountry(ticker: String): String? {
        return countryExtractor.extractFromTicker(ticker = ticker)
    }

    private fun createErrorNapOpenPosition(orderId: String): NapOpenPosition {
        val message = "Грешка при конвентиране на: $orderId"
        return NapOpenPosition(
            country = message,
            quantity = message,
            date = message,
            currencyPrice = message,
            levPrice = message,
            detailOriginalCurrency = message,
            detailOriginalPrice = message,
            detailLevDate = message,
        )
    }
}