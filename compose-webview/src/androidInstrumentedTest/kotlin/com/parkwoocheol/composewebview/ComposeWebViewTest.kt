package com.parkwoocheol.composewebview

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeWebViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

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
}
