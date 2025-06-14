package export

import converter.result.NapDividend
import util.PercentageCalculator
import util.WorkBookProvider
import java.io.File
import java.io.FileOutputStream

class NapDividendExporter(
    private val workBookProvider: WorkBookProvider,
    private val percentageCalculator: PercentageCalculator,
) {

    val progress = percentageCalculator.percentage

    fun export(
        dividends: List<NapDividend>,
        destination: File,
    ) : Result<Unit> {
        val workBook = workBookProvider.provideXSSFWorkbook()
        val sheet = workBook.createSheet("Дивиденти")

        percentageCalculator.reset(maximum = dividends.size)

        var rowIndex = 0

        sheet.createRow(rowIndex).let { titleRow ->
            titleRow.createCell(0).setCellValue("Наименование на лицето изплатило дохода")
            titleRow.createCell(1).setCellValue("Държава")
            titleRow.createCell(2).setCellValue("Брутен размер на дохода")
            titleRow.createCell(3).setCellValue("Платен данък в чужбина")
        }

        dividends.forEach { dividend ->
            percentageCalculator.increment()

            rowIndex += 1
            val currentRow = sheet.createRow(rowIndex)

            currentRow.createCell(0).setCellValue(dividend.companyName)
            currentRow.createCell(1).setCellValue(dividend.country)
            currentRow.createCell(2).setCellValue(dividend.grossDividend)
            currentRow.createCell(3).setCellValue(dividend.dividendWithholdTax)
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

            return Result.failure(Exception("Could not export trades to ${destination.name}"))
        }
    }
}