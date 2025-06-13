package util

data class Progress(@IntRange(from = 0, to = 100) val value: Int) {
    init {
        require(value in 0..100)
    }
}
