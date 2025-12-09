package com.parkwoocheol.sample.composewebview.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.platformDomStorageEnabled
import com.parkwoocheol.composewebview.platformJavaScriptEnabled
import com.parkwoocheol.composewebview.rememberSaveableWebViewState
import com.parkwoocheol.composewebview.rememberWebViewController
import com.parkwoocheol.sample.composewebview.ui.components.AppTopBar
import com.parkwoocheol.sample.composewebview.ui.components.BrowserControls

@Composable
fun BasicBrowserScreen(onBack: () -> Unit) {
    val state = rememberSaveableWebViewState(url = "https://google.com")
    val controller = rememberWebViewController()
    var urlInput by remember { mutableStateOf("https://google.com") }

    // Update URL input when WebView loads a new page
    androidx.compose.runtime.LaunchedEffect(state.lastLoadedUrl) {
        state.lastLoadedUrl?.let {
            if (it != urlInput) {
                urlInput = it
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Basic Browser",
                onBack = onBack,
            )
        },
        bottomBar = {
            BrowserControls(
                url = urlInput,
                onUrlChange = { urlInput = it },
                onLoadUrl = { controller.loadUrl(urlInput) },
                canGoBack = controller.canGoBack,
                canGoForward = controller.canGoForward,
                onBack = { controller.navigateBack() },
                onForward = { controller.navigateForward() },
                onReload = { controller.reload() },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Box(modifier = Modifier.weight(1f)) {
                ComposeWebView(
                    state = state,
                    controller = controller,
                    modifier = Modifier.fillMaxSize(),
                    onCreated = { webView ->
                        webView.platformJavaScriptEnabled = true
                        webView.platformDomStorageEnabled = true
                    },
                )
            }
        }
    }
}
