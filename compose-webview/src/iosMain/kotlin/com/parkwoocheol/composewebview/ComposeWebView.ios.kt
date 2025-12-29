package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import com.parkwoocheol.composewebview.client.ComposeWebViewDelegate
import com.parkwoocheol.composewebview.client.ComposeWebViewUIDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun ComposeWebViewImpl(
    url: String,
    modifier: Modifier,
    settings: WebViewSettings,
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
    onPermissionRequest: (PlatformPermissionRequest) -> Unit,
) {
    val state = rememberSaveableWebViewState(url = url)
    ComposeWebView(
        state = state,
        modifier = modifier,
        settings = settings,
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
        onPermissionRequest = onPermissionRequest,
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun ComposeWebViewImpl(
    state: WebViewState,
    modifier: Modifier,
    settings: WebViewSettings,
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
    onPermissionRequest: (PlatformPermissionRequest) -> Unit,
) {
    val webView =
        state.webView ?: remember {
            val config =
                WKWebViewConfiguration().apply {
                    // Apply settings to configuration
                    applySettings(settings)
                }
            WKWebView(frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config).apply {
                allowsBackForwardNavigationGestures = true
                // Apply additional settings to webView instance
                applyWebViewSettings(settings)
            }
        }.also { state.webView = it }

    // Observe estimatedProgress for onProgressChanged callback
    // Using LaunchedEffect with periodic polling since Kotlin/Native KVO is complex
    androidx.compose.runtime.LaunchedEffect(webView) {
        var lastProgress = -1
        while (true) {
            kotlinx.coroutines.delay(100) // Poll every 100ms
            val progress = webView.estimatedProgress
            val progressInt = (progress * 100).toInt()
            if (progressInt != lastProgress) {
                lastProgress = progressInt
                state.loadingState =
                    if (progressInt >= 100) {
                        LoadingState.Finished
                    } else {
                        LoadingState.Loading(progress.toFloat())
                    }
                onProgressChanged(webView, progressInt)
            }
        }
    }

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
    val uiDelegate = remember(state) { ComposeWebViewUIDelegate(state) }

    // Use a Box to overlay loading/error/dialogs on top of the WebView
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        UIKitView(
            factory = {
                val cvClient = client as ComposeWebViewClient
                cvClient.webViewState = state
                cvClient.webViewController = controller
                webView.navigationDelegate = delegate
                webView.UIDelegate = uiDelegate
                onCreated(webView)
                webView
            },
            modifier = androidx.compose.ui.Modifier.matchParentSize(),
            onRelease = {
                onDispose(webView)
            },
        )

        if (state.isLoading) {
            loadingContent()
        }

        if (state.errorsForCurrentRequest.isNotEmpty()) {
            errorContent(state.errorsForCurrentRequest)
        }

        state.jsDialogState?.let { dialogState ->
            when (dialogState) {
                is JsDialogState.Alert -> jsAlertContent(dialogState)
                is JsDialogState.Confirm -> jsConfirmContent(dialogState)
                is JsDialogState.Prompt -> jsPromptContent(dialogState)
            }
        }

        state.customViewState?.let { customView ->
            customViewContent?.invoke(customView)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun WKWebViewConfiguration.applySettings(webViewSettings: WebViewSettings) {
    preferences.apply {
        // JavaScript (Note: WKWebView JavaScript is always enabled by default)
        // javaScriptEnabled property is not available in WKPreferences
        // JavaScript can be controlled via setValue:forKey: if needed
    }

    // User Agent
    webViewSettings.userAgent?.let { ua ->
        applicationNameForUserAgent = ua
    }

    // Media Playback
    mediaTypesRequiringUserActionForPlayback =
        if (webViewSettings.mediaPlaybackRequiresUserAction) {
            platform.WebKit.WKAudiovisualMediaTypeAll
        } else {
            platform.WebKit.WKAudiovisualMediaTypeNone
        }

    // File Access is not directly configurable in WKWebView
    // allowFileAccessFromFileURLs is available via preferences for iOS 14+
}

@OptIn(ExperimentalForeignApi::class)
private fun WKWebView.applyWebViewSettings(webViewSettings: WebViewSettings) {
    // Allow inline media playback
    configuration.allowsInlineMediaPlayback = true

    // Most settings are applied via configuration
    // iOS WKWebView has limited settings compared to Android WebView
}
