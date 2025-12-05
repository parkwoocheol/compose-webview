package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*
import org.w3c.dom.HTMLIFrameElement

@Composable
actual fun ComposeWebView(
    url: String,
    modifier: Modifier,
    controller: WebViewController,
    javascriptInterfaces: Map<String, Any>,
    onCreated: (WebView) -> Unit,
    onDispose: (WebView) -> Unit,
    client: ComposeWebViewClient,
    chromeClient: ComposeWebChromeClient,
    factory: ((PlatformContext) -> WebView)?,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (List<WebViewError>) -> Unit,
    jsAlertContent: @Composable (JsDialogState.Alert) -> Unit,
    jsConfirmContent: @Composable (JsDialogState.Confirm) -> Unit,
    jsPromptContent: @Composable (JsDialogState.Prompt) -> Unit,
    customViewContent: (@Composable (CustomViewState) -> Unit)?,
    onPageStarted: (WebView, String?, PlatformBitmap?) -> Unit,
    onPageFinished: (WebView, String?) -> Unit,
    onReceivedError: (WebView, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit,
    onProgressChanged: (WebView, Int) -> Unit,
    onDownloadStart: ((String, String, String, String, Long) -> Unit)?,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)?
) {
    // Use Iframe from org.jetbrains.compose.web.dom
    // Note: This works best when using Compose HTML (DOM).
    // If using Compose Multiplatform (Canvas), this might not render correctly without an overlay.
    // But this is the standard way to access DOM elements in KMP Web.
    
    org.jetbrains.compose.web.dom.Iframe(
        attrs = {
            attr("src", url)
            style {
                width(100.percent)
                height(100.percent)
                border(0.px)
            }
            ref {
                onCreated(WebView(it as HTMLIFrameElement))
                onDispose { onDispose(WebView(it as HTMLIFrameElement)) }
            }
        }
    )
}

@Composable
actual fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier,
    controller: WebViewController,
    javascriptInterfaces: Map<String, Any>,
    onCreated: (WebView) -> Unit,
    onDispose: (WebView) -> Unit,
    client: ComposeWebViewClient,
    chromeClient: ComposeWebChromeClient,
    factory: ((PlatformContext) -> WebView)?,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (List<WebViewError>) -> Unit,
    jsAlertContent: @Composable (JsDialogState.Alert) -> Unit,
    jsConfirmContent: @Composable (JsDialogState.Confirm) -> Unit,
    jsPromptContent: @Composable (JsDialogState.Prompt) -> Unit,
    customViewContent: (@Composable (CustomViewState) -> Unit)?,
    jsBridge: WebViewJsBridge?,
    onPageStarted: (WebView, String?, PlatformBitmap?) -> Unit,
    onPageFinished: (WebView, String?) -> Unit,
    onReceivedError: (WebView, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit,
    onProgressChanged: (WebView, Int) -> Unit,
    onDownloadStart: ((String, String, String, String, Long) -> Unit)?,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)?
) {
    val url = state.lastLoadedUrl ?: ""
    Iframe(
        attrs = {
            attr("src", url)
            style {
                width(100.percent)
                height(100.percent)
                border(0.px)
            }
            ref {
                onCreated(WebView(it as HTMLIFrameElement))
                onDispose { onDispose(WebView(it as HTMLIFrameElement)) }
            }
        }
    )
}
