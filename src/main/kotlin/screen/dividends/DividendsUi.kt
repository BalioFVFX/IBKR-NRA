package screen.dividends

import screen.dialog.ConversionCompleteDialogUi
import screen.dialog.ErrorDialogUi
import screen.util.FileItem

data class DividendsUi(
    val dividendFile: FileItem?,
    val canRemove: Boolean,
    val canImport: Boolean,
    val canConvert: Boolean,
    val progress: Int?,
    val conversionCompleteDialogUi: ConversionCompleteDialogUi?,
    val errorDialogUi: ErrorDialogUi?,
)