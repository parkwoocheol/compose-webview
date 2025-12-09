package com.parkwoocheol.sample.composewebview.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.platformBuiltInZoomControls
import com.parkwoocheol.composewebview.platformDisplayZoomControls
import com.parkwoocheol.composewebview.platformDomStorageEnabled
import com.parkwoocheol.composewebview.platformJavaScriptEnabled
import com.parkwoocheol.composewebview.platformSupportZoom
import com.parkwoocheol.composewebview.rememberSaveableWebViewState
import com.parkwoocheol.sample.composewebview.ui.components.AppTopBar
import com.parkwoocheol.sample.composewebview.ui.components.SampleCustomView

@Composable
fun FullscreenVideoScreen(onBack: () -> Unit) {
    // A YouTube video that supports fullscreen
    val state = rememberSaveableWebViewState(url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ")

    Scaffold(
        topBar = {
            // Hide TopBar when in fullscreen mode (handled by custom view content)
            if (state.customViewState == null) {
                AppTopBar(
                    title = "Fullscreen Video Support",
                    onBack = onBack,
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(if (state.customViewState == null) paddingValues else PaddingValues(all = 0.dp))
                    .fillMaxSize()
                    .background(Color.Black),
        ) {
            ComposeWebView(
                state = state,
                modifier = Modifier.fillMaxSize(),
                onCreated = { webView ->
                    webView.platformJavaScriptEnabled = true
                    webView.platformDomStorageEnabled = true
                    // Enable Zoom for better video controls access
                    webView.platformSupportZoom = true
                    webView.platformBuiltInZoomControls = true
                    webView.platformDisplayZoomControls = false
                },
                customViewContent = { customViewState ->
                    // This block renders when the WebView requests a custom view (e.g. fullscreen video)
                    // We need to display the view provided by the WebChromeClient
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                    ) {
                        SampleCustomView(
                            view = customViewState.view,
                            modifier = Modifier.fillMaxSize(),
                            onRelease = {
                                // The WebChromeClient.onHideCustomView will handle the cleanup/callback
                                // But we should ensure the view is detached if needed,
                                // though AndroidView handles removing it from its internal ViewGroup.
                            },
                        )
                    }
                },
            )
        }
    }
}
