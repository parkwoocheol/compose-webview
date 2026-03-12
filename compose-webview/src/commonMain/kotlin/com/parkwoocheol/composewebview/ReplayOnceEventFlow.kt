package com.parkwoocheol.composewebview

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow

internal class ReplayOnceEventFlow<T> {
    private val flow = MutableSharedFlow<T>(replay = 1)

    suspend fun emit(value: T) {
        flow.emit(value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun collect(collector: suspend (T) -> Unit) {
        flow.collect { value ->
            try {
                collector(value)
            } finally {
                flow.resetReplayCache()
            }
        }
    }
}
