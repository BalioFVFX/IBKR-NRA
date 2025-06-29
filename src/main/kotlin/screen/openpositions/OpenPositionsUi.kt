package screen.openpositions

import screen.dialog.ConversionCompleteDialogUi
import screen.dialog.ErrorDialogUi
import screen.util.FileItem

data class OpenPositionsUi(
    val trades: List<FileItem>,
    val openPositions: FileItem?,
    val canImport: Boolean,
    val canConvert: Boolean,
    val canRemove: Boolean,
    val progress: Int?,
    val conversionCompleteDialogUi: ConversionCompleteDialogUi?,
    val errorDialogUi: ErrorDialogUi?,
)