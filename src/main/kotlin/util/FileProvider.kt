package util

import java.io.File
import java.time.format.DateTimeFormatter

class FileProvider(
    private val dateTimeProvider: DateTimeProvider,
) {

    companion object {
        private val EXPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.YYYY_HH.mm.ss")
        const val EXPORTS_PATH = "exports"
    }

    fun provide(path: String) : File {
        return File(path)
    }

    fun provide(parentFilePath: File, childPath: String): File {
        return File(parentFilePath, childPath)
    }

    fun provideExportDirectory() : ExportDirectory {
        val timestamp = dateTimeProvider.currentDateTime().format(EXPORT_DATE_TIME_FORMATTER)
        val export = File("$EXPORTS_PATH/$timestamp").apply { mkdirs() }
        val debug = File(export, "debug").apply { mkdirs()}

        return ExportDirectory(
            export = export,
            debug = debug,
        )
    }

}