package com.parkwoocheol.composewebview

import android.content.Context
import android.os.Bundle
import android.webkit.WebBackForwardList
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeWebViewTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testWebViewRenders() {
        val state = WebViewState(WebContent.Url("https://google.com"))

        composeTestRule.setContent {
            ComposeWebView(
                state = state,
                onCreated = {
                    it.settings.javaScriptEnabled = true
                },
            )
        }

        // Basic check to see if it doesn't crash and renders something.
        // In a real test, we might check for specific elements or use Espresso web assertions.
        // For now, we just ensure the composable can be set without error.
    }

    @Test
    fun destroyOnRelease_recreatesWebViewAndDestroysOldInstance() {
        var show by mutableStateOf(true)
        var testState: WebViewState? = null
        val createdWebViews = mutableListOf<TrackingWebView>()

        composeTestRule.setContent {
            val state = rememberWebViewState(url = "about:blank")
            testState = state
            val context = LocalContext.current
            if (show) {
                ComposeWebView(
                    state = state,
                    releaseStrategy = WebViewReleaseStrategy.DestroyOnRelease,
                    factory = { _: PlatformContext ->
                        TrackingWebView(context).also { createdWebViews += it }
                    },
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertNotNull(testState?.webView)
            assertTrue(createdWebViews.size == 1)
        }

        composeTestRule.runOnIdle { show = false }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertNull(testState?.webView)
            assertTrue(createdWebViews.first().destroyCalled)
        }

        composeTestRule.runOnIdle { show = true }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertNotNull(testState?.webView)
            assertTrue(createdWebViews.size == 2)
            assertNotSame(createdWebViews[0], createdWebViews[1])
        }
    }

    @Test
    fun keepAlive_reusesWebViewAcrossCompositionChanges() {
        var show by mutableStateOf(true)
        var testState: WebViewState? = null
        val createdWebViews = mutableListOf<TrackingWebView>()

        composeTestRule.setContent {
            val state = rememberWebViewState(url = "about:blank")
            testState = state
            val context = LocalContext.current
            if (show) {
                ComposeWebView(
                    state = state,
                    releaseStrategy = WebViewReleaseStrategy.KeepAlive,
                    factory = { _: PlatformContext ->
                        TrackingWebView(context).also { createdWebViews += it }
                    },
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertNotNull(testState?.webView)
            assertTrue(createdWebViews.size == 1)
        }

        composeTestRule.runOnIdle { show = false }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertNotNull(testState?.webView)
            assertTrue(!createdWebViews.first().destroyCalled)
        }

        composeTestRule.runOnIdle { show = true }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertNotNull(testState?.webView)
            assertTrue(createdWebViews.size == 1)
            assertSame(createdWebViews.first(), testState?.webView)
        }
    }

    @Test
    fun controllerLoadUrl_restoresLatestRequestedContentAfterReentry() {
        var show by mutableStateOf(true)
        var testController: WebViewController? = null
        val createdWebViews = mutableListOf<TrackingWebView>()

        composeTestRule.setContent {
            val state = rememberWebViewState(url = "about:blank")
            val controller = rememberWebViewController()
            testController = controller
            val context = LocalContext.current

            if (show) {
                ComposeWebView(
                    state = state,
                    controller = controller,
                    releaseStrategy = WebViewReleaseStrategy.DestroyOnRelease,
                    factory = { _: PlatformContext ->
                        TrackingWebView(context).also { createdWebViews += it }
                    },
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            testController?.loadUrl("https://next.example")
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertTrue(createdWebViews.first().loadedUrls.contains("https://next.example"))
        }

        composeTestRule.runOnIdle { show = false }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { show = true }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertTrue(createdWebViews.size == 2)
            assertEquals(listOf("https://next.example"), createdWebViews[1].loadedUrls)
        }
    }

    @Test
    fun controllerPostUrl_doesNotReplayAfterReentryWithoutNativeRestore() {
        var show by mutableStateOf(true)
        var testController: WebViewController? = null
        val createdWebViews = mutableListOf<TrackingWebView>()

        composeTestRule.setContent {
            val state = rememberWebViewState(url = "about:blank")
            val controller = rememberWebViewController()
            testController = controller
            val context = LocalContext.current

            if (show) {
                ComposeWebView(
                    state = state,
                    controller = controller,
                    releaseStrategy = WebViewReleaseStrategy.DestroyOnRelease,
                    factory = { _: PlatformContext ->
                        TrackingWebView(context).also { createdWebViews += it }
                    },
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            testController?.postUrl("https://post.example", byteArrayOf(1, 2, 3))
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertEquals(listOf("https://post.example"), createdWebViews.first().postedUrls)
        }

        composeTestRule.runOnIdle { show = false }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { show = true }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertTrue(createdWebViews.size == 2)
            assertTrue(createdWebViews[1].postedUrls.isEmpty())
        }
    }

    private class TrackingWebView(context: Context) : WebView(context) {
        var destroyCalled: Boolean = false
        val loadedUrls = mutableListOf<String>()
        val postedUrls = mutableListOf<String>()
        var savedStateCalled: Boolean = false

        override fun loadUrl(
            url: String,
            additionalHttpHeaders: MutableMap<String, String>,
        ) {
            loadedUrls += url
        }

        override fun postUrl(
            url: String,
            postData: ByteArray,
        ) {
            postedUrls += url
        }

        override fun saveState(outState: Bundle): WebBackForwardList? {
            savedStateCalled = true
            return null
        }

        override fun restoreState(inState: Bundle): WebBackForwardList? = null

        override fun destroy() {
            destroyCalled = true
            super.destroy()
        }
    }
}
