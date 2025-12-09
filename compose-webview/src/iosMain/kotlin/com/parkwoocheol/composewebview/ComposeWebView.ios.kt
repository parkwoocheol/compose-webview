package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import com.parkwoocheol.composewebview.client.ComposeWebViewDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ComposeWebView(
    url: String,
    modifier: Modifier,
    controller: WebViewController,
    javaScriptInterfaces: Map<String, Any>,
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
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)?,
) {
    val state = rememberSaveableWebViewState(url = url)
    ComposeWebView(
        state = state,
        modifier = modifier,
        controller = controller,
        javaScriptInterfaces = javaScriptInterfaces,
        onCreated = onCreated,
        onDispose = onDispose,
        client = client,
        chromeClient = chromeClient,
        factory = factory,
        loadingContent = loadingContent,
        errorContent = errorContent,
        jsAlertContent = jsAlertContent,
        jsConfirmContent = jsConfirmContent,
        jsPromptContent = jsPromptContent,
        customViewContent = customViewContent,
        onPageStarted = onPageStarted,
        onPageFinished = onPageFinished,
        onReceivedError = onReceivedError,
        onProgressChanged = onProgressChanged,
        onDownloadStart = onDownloadStart,
        onFindResultReceived = onFindResultReceived,
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier,
    controller: WebViewController,
    javaScriptInterfaces: Map<String, Any>,
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
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)?,
) {
    val webView =
        state.webView ?: remember {
            val config = WKWebViewConfiguration()
            WKWebView(frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config).apply {
                allowsBackForwardNavigationGestures = true
            }
        }.also { state.webView = it }

    // Connect controller
    // In Android implementation, this is done via LaunchedEffect
    // We need to replicate that logic here or in commonMain if possible.
    // But WebViewController logic is in commonMain now, but the connection logic was in ComposeWebView.kt (Android).
    // Let's check ComposeWebView.android.kt to see how it connects.

    // It calls:
    // LaunchedEffect(webView, navigator) {
    //     with(navigator) { webView.handleNavigationEvents() }
    // }

    // We should do the same here.

    androidx.compose.runtime.LaunchedEffect(webView, controller) {
        controller.handleNavigationEvents(webView)
    }

    // Load URL if needed
    androidx.compose.runtime.LaunchedEffect(state.lastLoadedUrl) {
        state.lastLoadedUrl?.let { url ->
            if (webView.URL?.absoluteString != url) {
                val request = platform.Foundation.NSURLRequest.requestWithURL(platform.Foundation.NSURL.URLWithString(url)!!)
                webView.loadRequest(request)
            }
        }
    }

    val delegate = remember(client) { ComposeWebViewDelegate(client) }

    UIKitView(
        factory = {
            val cvClient = client as ComposeWebViewClient
            cvClient.webViewState = state
            cvClient.webViewController = controller
            webView.navigationDelegate = delegate
            onCreated(webView)
            webView
        },
        modifier = modifier,
        onRelease = {
            onDispose(webView)
        },
    )
}
