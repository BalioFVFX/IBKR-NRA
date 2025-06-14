package export

import parser.output.Trade
import parser.TradesParser
import util.PercentageCalculator
import util.WorkBookProvider
import java.io.File
import java.io.FileOutputStream

class TradesExporter(
    private val workBookProvider: WorkBookProvider,
    private val percentageCalculator: PercentageCalculator,
) {

    val progress = percentageCalculator.percentage

    fun export(
        trades: List<Trade>,
        sheetName: String,
        destination: File,
    ) : Result<Unit> {
        val workBook = workBookProvider.provideXSSFWorkbook()
        percentageCalculator.reset(maximum = trades.size)

        val sheet = workBook.createSheet(sheetName)

        var rowIndex = 0

        sheet.createRow(rowIndex).let { titleRow ->
            titleRow.createCell(0).setCellValue("Account ID")
            titleRow.createCell(1).setCellValue("Currency")
            titleRow.createCell(2).setCellValue("Symbol")
            titleRow.createCell(3).setCellValue("ISIN")
            titleRow.createCell(4).setCellValue("Listing Exchange")
            titleRow.createCell(5).setCellValue("DateTime")
            titleRow.createCell(6).setCellValue("Buy/Sell")
            titleRow.createCell(7).setCellValue("Quantity")
            titleRow.createCell(8).setCellValue("TradePrice")
            titleRow.createCell(9).setCellValue("Commission")
            titleRow.createCell(10).setCellValue("Commission Currency")
            titleRow.createCell(11).setCellValue("Transaction ID")
        }

        trades.forEach { trade ->
            percentageCalculator.increment()

            rowIndex += 1
            val currentRow = sheet.createRow(rowIndex)

            currentRow.createCell(0).setCellValue(trade.accountId)
            currentRow.createCell(1).setCellValue(trade.currency.toString())
            currentRow.createCell(2).setCellValue(trade.symbol)
            currentRow.createCell(3).setCellValue(trade.isin)
            currentRow.createCell(4).setCellValue(trade.listingExchange)
            currentRow.createCell(5).setCellValue(trade.dateTime.format(TradesParser.DATE_TIME_FORMATTER))
            currentRow.createCell(6).setCellValue(trade.buySell.toString())
            currentRow.createCell(7).setCellValue(trade.quantity.toString())
            currentRow.createCell(8).setCellValue(trade.tradePrice.toString())
            currentRow.createCell(9).setCellValue(trade.commission.toString())
            currentRow.createCell(10).setCellValue(trade.commissionCurrency.toString())
            currentRow.createCell(11).setCellValue(trade.transactionId)
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