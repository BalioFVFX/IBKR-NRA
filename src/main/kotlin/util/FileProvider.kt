package util

import ca.gosyer.appdirs.AppDirs
import java.io.File
import java.io.InputStream
import java.time.format.DateTimeFormatter

class FileProvider(
    private val dateTimeProvider: DateTimeProvider,
) {

    companion object {
        private val EXPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.YYYY_HH.mm.ss")

        private val appDirs = AppDirs {
            appName = "IBKR-NRA"
        }

        fun createUserDataDir() {
            File(appDirs.getUserDataDir(roaming = false)).mkdirs()
        }
    }

    fun provide(path: String) : File {
        return File(path)
    }

    fun provide(parentFilePath: File, childPath: String): File {
        return File(parentFilePath, childPath)
    }

    fun provideExportDirectory() : ExportDirectory {
        val timestamp = dateTimeProvider.currentDateTime().format(EXPORT_DATE_TIME_FORMATTER)

        val export = provide(
            parentFilePath = provideUserDataDir(),
            childPath = "exports/$timestamp"
        ).apply {
            mkdir()
        }

        val debug = provide(
            parentFilePath = export,
            childPath = "debug"
        ).apply {
            mkdirs()
        }

        return ExportDirectory(
            export = export,
            debug = debug,
        )
    }


    /**
     * @return Application's directory for read/write operations
     */
    fun provideUserDataDir(): File {
        // * macOS: /Users/<Account>/Library/Application Support/<IBKR-NRA>
        // * Windows: C:\Users\<Account>\AppData\\<IBKR-NRA>
        // * Linux: /home/<account>/.local/share/<IBKR-NRA>
        val path = appDirs.getUserDataDir(roaming = false)

        return File(path)
    }

    /**
     * @return InputStream that is connected to a resource in `src/main/resources`
     */
    fun provideResource(resourceName: String): InputStream {
        return FileProvider::class.java.getResource("/$resourceName")!!.openStream()
    }
}