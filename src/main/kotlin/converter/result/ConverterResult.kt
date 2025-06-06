package converter.result

data class ConverterResult<T>(
    val data: List<T>,
    val errors: List<String>,
)