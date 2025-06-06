package currency

enum class Currency {
    EUR,
    USD;

    override fun toString(): String {
        return when (this) {
            EUR -> "EUR"
            USD -> "USD"
        }
    }

    companion object {
        fun parse(currencyString: String) : Currency {
            return when (currencyString.lowercase()) {
                "usd" -> USD
                "eur" -> EUR
                else -> throw IllegalArgumentException("Unknown currency: $currencyString")
            }
        }
    }
}