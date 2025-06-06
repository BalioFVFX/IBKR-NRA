package export

import java.io.File

class ConverterErrorExporter() {

    fun export(
        destination: File,
        errors: List<String>
    ) {
        destination.writeText(
            text = errors.joinToString(separator = "\n")
        )
    }
}