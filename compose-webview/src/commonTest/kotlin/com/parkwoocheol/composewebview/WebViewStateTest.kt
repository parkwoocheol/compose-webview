package com.parkwoocheol.composewebview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebViewStateTest {
    @Test
    fun testInitialState() {
        val state = WebViewState(WebContent.Url("https://example.com"))
        assertTrue(state.content is WebContent.Url)
        assertEquals("https://example.com", (state.content as WebContent.Url).url)
        assertEquals(null, state.pageTitle) // Default title is null
        assertEquals(LoadingState.Initializing, state.loadingState)
    }

    @Test
    fun testLoadingStateChanges() {
        val state = WebViewState(WebContent.Url("https://example.com"))

        state.loadingState = LoadingState.Loading(0.5f)
        assertTrue(state.loadingState is LoadingState.Loading)
        assertEquals(0.5f, (state.loadingState as LoadingState.Loading).progress)

        state.loadingState = LoadingState.Finished
        assertEquals(LoadingState.Finished, state.loadingState)
    }

    @Test
    fun postRequestsAreConsumedIntoNavigatorOnly() {
        val state = WebViewState(WebContent.Post("https://example.com/post", byteArrayOf(1, 2, 3)))

        state.consumePostRequest()

        assertEquals(WebContent.NavigatorOnly, state.content)
        assertTrue(state.suppressLastLoadedUrlFallback)
    }

    @Test
    fun savedPostRequestsRestoreWithoutReplayablePostContent() {
        val state = WebViewState(WebContent.Post("https://example.com/post", byteArrayOf(1, 2, 3)))
        state.lastLoadedUrl = "https://example.com/post"

        val restored = state.toSaveableStateMap().toRestoredWebViewState()

        assertEquals(WebContent.NavigatorOnly, restored.content)
        assertTrue(restored.suppressLastLoadedUrlFallback)
        assertEquals(LoadingState.Finished, restored.loadingState)
        assertFalse(restored.shouldSkipTopLevelLoadForCurrentRequest())
    }

    // testErrorsForUrl removed as it requires platform-specific types that are hard to instantiate in commonTest
}
