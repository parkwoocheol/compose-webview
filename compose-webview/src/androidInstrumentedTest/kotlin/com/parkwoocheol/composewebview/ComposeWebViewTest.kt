package com.parkwoocheol.composewebview

import android.content.Context
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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

    private class TrackingWebView(context: Context) : WebView(context) {
        var destroyCalled: Boolean = false

        override fun destroy() {
            destroyCalled = true
            super.destroy()
        }
    }
}
