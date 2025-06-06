package screen.dividends

import screen.util.FileItem

data class DividendsUi(
    val dividendFile: FileItem?,
    val canRemove: Boolean,
    val canImport: Boolean,
    val canConvert: Boolean,
)