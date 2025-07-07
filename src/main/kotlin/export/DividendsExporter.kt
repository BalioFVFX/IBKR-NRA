package export

import parser.DividendsParser
import parser.output.Dividend
import util.PercentageCalculator
import util.WorkBookProvider
import java.io.File
import java.io.FileOutputStream

class DividendsExporter(
    private val workBookProvider: WorkBookProvider,
    private val percentageCalculator: PercentageCalculator,
) {

    val progress = percentageCalculator.percentage

    fun export(
        dividends: List<Dividend>,
        sheetName: String,
        destination: File,
    ) : Result<Unit> {
        val workBook = workBookProvider.provideXSSFWorkbook()
        val sheet = workBook.createSheet(sheetName)
        percentageCalculator.reset(maximum = dividends.size)

        var rowIndex = 0

        sheet.createRow(rowIndex).let { titleRow ->
            titleRow.createCell(0).setCellValue("Currency")
            titleRow.createCell(1).setCellValue("Symbol")
            titleRow.createCell(2).setCellValue("Description")
            titleRow.createCell(3).setCellValue("ISIN")
            titleRow.createCell(4).setCellValue("Issuer Country")
            titleRow.createCell(5).setCellValue("DateTime")
            titleRow.createCell(6).setCellValue("Amount")
            titleRow.createCell(7).setCellValue("Withhold tax")
            titleRow.createCell(8).setCellValue("ActionID")
        }

        dividends.forEach { dividend ->
            percentageCalculator.increment()

            rowIndex += 1
            val currentRow = sheet.createRow(rowIndex)

            currentRow.createCell(0).setCellValue(dividend.currency.toString())
            currentRow.createCell(1).setCellValue(dividend.ticker)
            currentRow.createCell(2).setCellValue(dividend.description)
            currentRow.createCell(3).setCellValue(dividend.isin)
            currentRow.createCell(4).setCellValue(dividend.issuerCountry)
            currentRow.createCell(5).setCellValue(dividend.dateTime.format(DividendsParser.DATE_TIME_FORMATTER))
            currentRow.createCell(6).setCellValue(dividend.amount.toString())
            currentRow.createCell(7).setCellValue(dividend.taxAmount.toString())
            currentRow.createCell(8).setCellValue(dividend.actionId)
        }

        try {
            FileOutputStream(destination).use { fileOutputStream ->
                workBook.use { workBook ->
                    workBook.write(fileOutputStream)
                }
            }

            return Result.success(Unit)
        } catch (ex: Exception) {
            ex.printStackTrace()

            return Result.failure(ex)
        }
    }
}