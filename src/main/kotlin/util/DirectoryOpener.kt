package util

import java.awt.Desktop
import java.io.File

class DirectoryOpener {

    fun openDirectory(path: String): Boolean {
        val directory = File(path)

        if (!directory.exists() || !directory.isDirectory) {
            println("Invalid directory")

            return false
        }

        return try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(directory)
                true
            } else {
                openDirWithProcess(directory)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun openDirWithProcess(directory: File): Boolean {
        val os = System.getProperty("os.name").lowercase()
        val command = when {
            os.contains("linux") -> listOf("xdg-open", directory.absolutePath)
            os.contains("mac") -> listOf("open", directory.absolutePath)
            os.contains("windows") -> listOf("explorer", directory.absolutePath)
            else -> null
        }

        if (command.isNullOrEmpty()) {
            println("Unknown OS: $os")
            return false
        }

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()

        return exitCode != 0
    }
}
