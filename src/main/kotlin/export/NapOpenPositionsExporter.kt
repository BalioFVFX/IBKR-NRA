package export

import converter.result.NapOpenPosition
import util.PercentageCalculator
import util.WorkBookProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.forEach

class NapOpenPositionsExporter(
    private val workBookProvider: WorkBookProvider,
    private val percentageCalculator: PercentageCalculator,
) {

    val progress = percentageCalculator.percentage

    fun export(
        openPositions: List<NapOpenPosition>,
        destination: File,
    ) : Result<Unit> {
        percentageCalculator.reset(maximum = openPositions.size)

        val workBook = workBookProvider.provideXSSFWorkbook()

        val sheet = workBook.createSheet("Нап")
        val extendedSheet = workBook.createSheet("Разширен")

        var rowIndex = 0

        sheet.createRow(rowIndex).let { titleRow ->
            titleRow.createCell(0).setCellValue("Държава")
            titleRow.createCell(1).setCellValue("Брой")
            titleRow.createCell(2).setCellValue("Дата и година на придобиване")
            titleRow.createCell(3).setCellValue("В съответната валута")
            titleRow.createCell(4).setCellValue("В лева")
        }

        extendedSheet.createRow(rowIndex).let { titleRow ->
            titleRow.createCell(0).setCellValue("Държава")
            titleRow.createCell(1).setCellValue("Брой")
            titleRow.createCell(2).setCellValue("Дата и година на придобиване")
            titleRow.createCell(3).setCellValue("В съответната валута")
            titleRow.createCell(4).setCellValue("В лева")
            titleRow.createCell(5).setCellValue("Оригинална валута на придобиване")
            titleRow.createCell(6).setCellValue("Оригинална цена в оригинална валута")
            titleRow.createCell(7).setCellValue("Дата конвентиране валута - лев")
        }

        openPositions.forEach { openPosition ->
            rowIndex += 1
            val currentRow = sheet.createRow(rowIndex)
            val currentExtendedRow = extendedSheet.createRow(rowIndex)

            currentRow.createCell(0).setCellValue(openPosition.country)
            currentRow.createCell(1).setCellValue(openPosition.quantity)
            currentRow.createCell(2).setCellValue(openPosition.date)
            currentRow.createCell(3).setCellValue(openPosition.currencyPrice)
            currentRow.createCell(4).setCellValue(openPosition.levPrice)

            currentExtendedRow.createCell(0).setCellValue(openPosition.country)
            currentExtendedRow.createCell(1).setCellValue(openPosition.quantity)
            currentExtendedRow.createCell(2).setCellValue(openPosition.date)
            currentExtendedRow.createCell(3).setCellValue(openPosition.currencyPrice)
            currentExtendedRow.createCell(4).setCellValue(openPosition.levPrice)
            currentExtendedRow.createCell(5).setCellValue(openPosition.detailOriginalCurrency)
            currentExtendedRow.createCell(6).setCellValue(openPosition.detailOriginalPrice)
            currentExtendedRow.createCell(7).setCellValue(openPosition.detailLevDate)

            percentageCalculator.increment()
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

            return Result.failure(Exception("Could not export Open Positions to ${destination.name}"))
        }
    }
}