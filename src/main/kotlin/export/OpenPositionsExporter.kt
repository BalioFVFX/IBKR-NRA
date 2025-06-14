package export

import parser.output.OpenPosition
import parser.OpenPositionsParser
import util.PercentageCalculator
import util.WorkBookProvider
import java.io.File
import java.io.FileOutputStream

class OpenPositionsExporter(
    private val workBookProvider: WorkBookProvider,
    private val percentageCalculator: PercentageCalculator
) {

    val progress = percentageCalculator.percentage

    fun export(
        openPositions: List<OpenPosition>,
        sheetName: String,
        destination: File,
    ) : Result<Unit> {
        percentageCalculator.reset(maximum = openPositions.size)

        val workBook = workBookProvider.provideXSSFWorkbook()
        val sheet = workBook.createSheet(sheetName)

        var rowIndex = 0

        sheet.createRow(rowIndex).let { titleRow ->
            titleRow.createCell(0).setCellValue("Account ID")
            titleRow.createCell(1).setCellValue("Currency")
            titleRow.createCell(2).setCellValue("Symbol")
            titleRow.createCell(3).setCellValue("ISIN")
            titleRow.createCell(4).setCellValue("Listing Exchange")
            titleRow.createCell(5).setCellValue("OpenDate")
            titleRow.createCell(6).setCellValue("OpenPrice")
            titleRow.createCell(7).setCellValue("Quantity")
            titleRow.createCell(8).setCellValue("CostBasisPrice")
            titleRow.createCell(9).setCellValue("CostBasisMoney")
            titleRow.createCell(10).setCellValue("Transaction ID")
        }

        openPositions.forEach { openPosition ->
            rowIndex += 1
            val currentRow = sheet.createRow(rowIndex)

            currentRow.createCell(0).setCellValue(openPosition.accountId)
            currentRow.createCell(1).setCellValue(openPosition.currency.toString())
            currentRow.createCell(2).setCellValue(openPosition.symbol)
            currentRow.createCell(3).setCellValue(openPosition.isin)
            currentRow.createCell(4).setCellValue(openPosition.listingExchange)
            currentRow.createCell(5).setCellValue(openPosition.openDate.format(OpenPositionsParser.OPEN_DATE_FORMATTER))
            currentRow.createCell(6).setCellValue(openPosition.openPrice.toString())
            currentRow.createCell(7).setCellValue(openPosition.quantity.toString())
            currentRow.createCell(8).setCellValue(openPosition.costBasisPrice.toString())
            currentRow.createCell(9).setCellValue(openPosition.costBasisMoney.toString())
            currentRow.createCell(10).setCellValue(openPosition.transactionId)

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