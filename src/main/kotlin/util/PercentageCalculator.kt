package util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PercentageCalculator() {

    private val _percentage = MutableStateFlow(value = Progress(value = 0))

    private var step = 0f
    private var currentValue = 0f

    val percentage = _percentage.asStateFlow()

    fun increment(times: Int = 1) {
        currentValue += step * times

        _percentage.tryEmit(value = Progress(value = (currentValue * 100).toInt()))
    }

    fun reset(maximum: Int) {
        this.step = 1f / maximum
        this.currentValue = 0f
        this._percentage.tryEmit(value = Progress(value = 0))
    }
}