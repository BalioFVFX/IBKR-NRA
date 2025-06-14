package util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

class PercentageCalculator() {

    private val _percentage = MutableStateFlow(value = Progress(value = 0))

    private var maximum = 0
    private var currentIncrement = 0

    val percentage = _percentage.asStateFlow()

    fun increment(times: Int = 1) {
        currentIncrement = min(maximum, currentIncrement + times)

        val percentageValue = (currentIncrement / maximum.toFloat()) * 100

        _percentage.tryEmit(
            value = Progress(
                value = percentageValue.toInt(),
            )
        )
    }

    fun reset(maximum: Int) {
        this.maximum = maximum
        this._percentage.tryEmit(value = Progress(value = 0))
    }
}