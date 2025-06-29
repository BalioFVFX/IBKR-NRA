package converter.result

data class ConverterResult<T>(
    val data: List<T>,
    val issues: List<Issue>,
)