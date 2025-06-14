package util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

fun combineProgress(vararg flows: StateFlow<Progress>): Flow<Progress> {
    return combine(
        flows = flows,
        transform = { values ->
            values.sumOf { progress ->
                progress.value
            } / flows.size
        }
    )
        .map { Progress(value = it) }
}