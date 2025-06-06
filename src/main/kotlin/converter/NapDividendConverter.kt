package converter

import converter.result.ConverterResult
import converter.result.NapDividend
import currency.LevExchanger
import parser.DividendsParser
import parser.output.Dividend
import util.CompanyNameExtractor
import util.CountryExtractor

class NapDividendConverter(
    private val levExchanger: LevExchanger,
    private val companyNameExtractor: CompanyNameExtractor,
    private val countryExtractor: CountryExtractor,
) {

    suspend fun convert(
        dividends: List<Dividend>,
    ): ConverterResult<NapDividend> {
        val errors = mutableListOf<String>()
        val napDividends = mutableListOf<NapDividend>()

        for (dividend in dividends) {
            val dividendCompanyName = run {
                val companyName = companyNameExtractor.extract(ticker = dividend.ticker)

                if (companyName == null) {
                    errors.addError("Не е намерено името на компанията за дивидент с actionId: ${dividend.actionId}")
                    dividend.ticker
                } else {
                    companyName
                }
            }

            val levForDividendDate = run {
                val lev = levExchanger.exchange(
                    fromDate = dividend.dateTime.toLocalDate(),
                    currency = dividend.currency,
                )

                if (lev.isFailure) {
                    errors.addError("Не е намеренца цена на лев за дивидент с actionId: ${dividend.actionId}")
                    null
                } else {
                    val actualLevDate = lev.getOrThrow().first

                    if (actualLevDate != dividend.dateTime.toLocalDate()) {
                        errors.addError(
                            "Не е намерена цена за лев за дата: ${
                                DividendsParser.DATE_TIME_FORMATTER.format(dividend.dateTime)
                            }. Използвана дата: ${DividendsParser.DATE_TIME_FORMATTER.format(actualLevDate)}"
                        )
                    }

                    lev.getOrThrow().second
                }
            }

            val country = run {
                val result = countryExtractor.extractFromTicker(ticker = dividend.ticker)

                if (result == null) {
                    errors.addError("Не е намерена държава за дивидент с actionId")
                    "Грешка"
                } else {
                    result
                }
            }

            napDividends.add(
                NapDividend(
                    companyName = dividendCompanyName,
                    country = country,
                    grossDividend = levForDividendDate?.multiply(dividend.amount)?.toString() ?: "Грешка",
                    dividendWithholdTax = levForDividendDate?.multiply(dividend.taxAmount.abs())?.toString() ?: "Грешка",
                )
            )
        }

        return ConverterResult(
            data = napDividends,
            errors = errors,
        )
    }

    private fun MutableList<String>.addError(error: String) {
        println(error)
        add(error)
    }
}