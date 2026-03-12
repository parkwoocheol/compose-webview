package com.parkwoocheol.composewebview

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ReplayOnceEventFlowTest {
    @Test
    fun bufferedEventIsDeliveredToFirstCollectorOnly() =
        runTest {
            val flow = ReplayOnceEventFlow<String>()
            flow.emit("load")

            val firstResult = CompletableDeferred<String>()
            val firstCollector =
                backgroundScope.launch {
                    flow.collect { firstResult.complete(it) }
                }

            assertEquals("load", firstResult.await())
            firstCollector.cancelAndJoin()

            var secondCollectorTriggered = false
            val secondCollector =
                backgroundScope.launch {
                    flow.collect { secondCollectorTriggered = true }
                }

            advanceUntilIdle()
            assertFalse(secondCollectorTriggered)
            secondCollector.cancelAndJoin()
        }

    @Test
    fun freshEventsStillReachNewCollectors() =
        runTest {
            val flow = ReplayOnceEventFlow<String>()
            flow.emit("stale")

            val firstResult = CompletableDeferred<String>()
            val firstCollector =
                backgroundScope.launch {
                    flow.collect { firstResult.complete(it) }
                }

            assertEquals("stale", firstResult.await())
            firstCollector.cancelAndJoin()

            val secondResult = CompletableDeferred<String>()
            val secondCollector =
                backgroundScope.launch {
                    flow.collect { secondResult.complete(it) }
                }

            flow.emit("fresh")

            assertEquals("fresh", secondResult.await())
            secondCollector.cancelAndJoin()
        }
}
