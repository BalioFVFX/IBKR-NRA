package parser.output

enum class TradeType {
    BUY,
    SELL;

    override fun toString(): String {
        return when (this) {
            BUY -> "BUY"
            SELL -> "SELL"
        }
    }

    companion object {
        fun parse(tradeTypeString: String): TradeType {
            return when (tradeTypeString.lowercase()) {
                "buy" -> BUY
                "sell" -> SELL
                else -> throw IllegalArgumentException("Unknown trade type: $tradeTypeString")
            }
        }
    }
}