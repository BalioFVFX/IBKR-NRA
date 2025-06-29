package export

import converter.result.Issue
import java.io.File

class ConverterIssueExporter() {

    fun export(
        destination: File,
        issues: List<Issue>
    ) {
        destination.writeText(
            text = issues.joinToString(
                separator = "\n",
                transform = { issue ->
                    val prefix = when (issue) {
                        is Issue.Error -> "Грешка"
                        is Issue.Warning -> "Забележка"
                    }

                    "$prefix: ${issue.reason}"
                },
            )
        )
    }
}