package com.parkwoocheol.composewebview

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebViewControllerTest {
    @Test
    fun loadUrl_updatesBoundStateAndInvalidatesSavedBundle() =
        runTest {
            val controller = WebViewController(this)
            val state = WebViewState(WebContent.Url("https://initial.example"))
            val initialVersion = state.currentContentRequest.version

            state.bundle = createPlatformBundle()
            state.markTopLevelLoadHandledByRestore()

            controller.bindState(state)
            controller.loadUrl(
                url = "https://next.example",
                additionalHttpHeaders = mapOf("X-Test" to "true"),
            )

            val content = state.content as WebContent.Url
            assertEquals("https://next.example", content.url)
            assertEquals(mapOf("X-Test" to "true"), content.additionalHttpHeaders)
            assertEquals(initialVersion + 1, state.currentContentRequest.version)
            assertNull(state.bundle)
            assertFalse(state.shouldSkipTopLevelLoadForCurrentRequest())
        }

    @Test
    fun pendingTopLevelRequest_appliesWhenStateBinds() =
        runTest {
            val controller = WebViewController(this)
            val state = WebViewState(WebContent.Url("https://initial.example"))
            val initialVersion = state.currentContentRequest.version

            controller.loadHtml("<html><body>queued</body></html>")
            controller.bindState(state)

            val content = state.content as WebContent.Data
            assertEquals("<html><body>queued</body></html>", content.data)
            assertEquals(initialVersion + 1, state.currentContentRequest.version)
        }

    @Test
    fun sameUrlRequest_stillAdvancesRequestVersion() =
        runTest {
            val controller = WebViewController(this)
            val state = WebViewState(WebContent.Url("https://same.example"))

            controller.bindState(state)
            controller.loadUrl("https://same.example")
            val firstVersion = state.currentContentRequest.version

            controller.loadUrl("https://same.example")

            assertEquals(firstVersion + 1, state.currentContentRequest.version)
            assertTrue(state.content is WebContent.Url)
            assertEquals("https://same.example", (state.content as WebContent.Url).url)
        }
}
