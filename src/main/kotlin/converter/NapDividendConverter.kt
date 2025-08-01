package converter

import converter.result.ConverterResult
import converter.result.Issue
import converter.result.NapDividend
import converter.result.addError
import converter.result.addWarning
import currency.LevExchanger
import parser.DividendsParser
import parser.output.Dividend
import util.CompanyNameExtractor
import util.CountryExtractor
import util.PercentageCalculator

class NapDividendConverter(
    private val levExchanger: LevExchanger,
    private val companyNameExtractor: CompanyNameExtractor,
    private val countryExtractor: CountryExtractor,
    private val percentageCalculator: PercentageCalculator,
) {

    val progress = percentageCalculator.percentage

    suspend fun convert(
        dividends: List<Dividend>,
    ): ConverterResult<NapDividend> {
        val issues = mutableListOf<Issue>()
        val napDividends = mutableListOf<NapDividend>()
        percentageCalculator.reset(maximum = dividends.size)

        for (dividend in dividends) {
            percentageCalculator.increment()

            val dividendCompanyName = run {
                val companyName = companyNameExtractor.extract(ticker = dividend.ticker)

                if (companyName == null) {
                    issues.addWarning(
                        reason = "Не е намерено името на компанията за дивидент с actionId: ${dividend.actionId}",
                    )

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
                    issues.addError(
                        reason = "Не е намеренца цена на лев за дивидент с actionId: ${dividend.actionId}",
                    )

                    null
                } else {
                    val actualLevDate = lev.getOrThrow().first

                    if (actualLevDate != dividend.dateTime.toLocalDate()) {
                        issues.addWarning(
                            reason = "Не е намерена цена за лев за дата: ${
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
                    issues.addError(reason = "Не е намерена държава за дивидент с actionId: ${dividend.actionId}")
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
                    dividendWithholdTax = levForDividendDate?.multiply(dividend.taxAmount.abs())?.toString()
                        ?: "Грешка",
                )
            )
        }

        return ConverterResult(
            data = napDividends,
            issues = issues,
        )
    }
}