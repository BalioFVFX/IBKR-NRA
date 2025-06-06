package screen.openpositions

import screen.util.FileItem

data class OpenPositionsUi(
    val trades: List<FileItem>,
    val openPositions: FileItem?,
    val canImport: Boolean,
    val canConvert: Boolean,
    val canRemove: Boolean,
)