package com.parkwoocheol.composewebview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import com.parkwoocheol.composewebview.client.ComposeWebViewDelegate
import com.parkwoocheol.composewebview.client.ComposeWebViewUIDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.darwin.NSObjectProtocol
import platform.UIKit.UIColor
import platform.UIKit.UIScreen
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowDidBecomeHiddenNotification
import platform.UIKit.UIWindowDidBecomeVisibleNotification

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
    onDownloadStart: ((String, String, String, String, Long) -> Unit)?,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)?,
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
        onDownloadStart = onDownloadStart,
        onFindResultReceived = onFindResultReceived,
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
    onDownloadStart: ((String, String, String, String, Long) -> Unit)?,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)?,
) {
    val webView =
        state.webView ?: remember {
            val config =
                WKWebViewConfiguration().apply {
                    applySettings(settings)
                }
            WKWebView(frame = kotlinx.cinterop.cValue { }, configuration = config).apply {
                allowsBackForwardNavigationGestures = true
                autoresizingMask = platform.UIKit.UIViewAutoresizingFlexibleWidth or platform.UIKit.UIViewAutoresizingFlexibleHeight
                applyWebViewSettings(settings)
            }
        }.also { state.webView = it }

    val delegate = remember(client) { ComposeWebViewDelegate(client) }
    val uiDelegate = remember(state) { ComposeWebViewUIDelegate(state) }
    val fullscreenObserver = remember(state) { IosFullscreenVideoObserver(state) }
    val registeredInterfaceNames = remember(webView) { mutableSetOf<String>() }

    DisposableEffect(fullscreenObserver, customViewContent != null) {
        if (customViewContent != null) {
            fullscreenObserver.start()
        } else {
            fullscreenObserver.stop()
            state.customViewState = null
        }
        onDispose {
            fullscreenObserver.stop()
            state.customViewState = null
        }
    }

    LaunchedEffect(webView, chromeClient) {
        var lastProgress = -1
        var lastScrollX = -1
        var lastScrollY = -1
        while (isActive) {
            delay(100)

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
                chromeClient.onProgressChanged(webView, progressInt)
            }

            val offset = webView.scrollView.contentOffset
            var scrollX = 0
            var scrollY = 0
            offset.useContents {
                scrollX = this.x.toInt()
                scrollY = this.y.toInt()
            }
            if (scrollX != lastScrollX || scrollY != lastScrollY) {
                lastScrollX = scrollX
                lastScrollY = scrollY
                state.scrollPosition = ScrollPosition(scrollX, scrollY)
            }
        }
    }

    LaunchedEffect(webView, controller) {
        controller.handleNavigationEvents(webView)
    }

    val currentContent = state.content
    LaunchedEffect(webView, currentContent) {
        webView.runOnMainThread {
            when (currentContent) {
                is WebContent.Url -> {
                    if (currentContent.url.isNotEmpty() && webView.URL?.absoluteString != currentContent.url) {
                        webView.loadUrlWithHeaders(currentContent.url, currentContent.additionalHttpHeaders)
                    }
                }

                is WebContent.Data -> {
                    webView.loadHTMLString(
                        currentContent.data,
                        baseURL = currentContent.baseUrl?.let { NSURL.URLWithString(it) },
                    )
                }

                is WebContent.Post -> {
                    webView.platformPostUrl(currentContent.url, currentContent.postData)
                }

                WebContent.NavigatorOnly -> Unit
            }
        }
    }

    LaunchedEffect(webView, state.lastLoadedUrl, state.loadingState, state.content) {
        if (state.content is WebContent.NavigatorOnly && state.loadingState is LoadingState.Initializing) {
            val urlToRestore = state.lastLoadedUrl
            if (!urlToRestore.isNullOrEmpty()) {
                webView.runOnMainThread {
                    webView.loadUrlWithHeaders(urlToRestore, emptyMap())
                }
            }
        }
    }

    LaunchedEffect(webView, javaScriptInterfaces) {
        webView.runOnMainThread {
            val controller = webView.configuration.userContentController
            registeredInterfaceNames.forEach { controller.removeScriptMessageHandlerForName(it) }
            registeredInterfaceNames.clear()
            javaScriptInterfaces.forEach { (name, obj) ->
                webView.platformAddJavascriptInterface(obj, name)
                registeredInterfaceNames.add(name)
            }
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.runOnMainThread {
                val controller = webView.configuration.userContentController
                registeredInterfaceNames.forEach { controller.removeScriptMessageHandlerForName(it) }
                registeredInterfaceNames.clear()
            }
        }
    }

    LaunchedEffect(webView, jsBridge) {
        jsBridge?.attach(webView)
    }

    jsBridge?.let { bridge ->
        LaunchedEffect(webView, bridge, state.loadingState) {
            if (state.loadingState is LoadingState.Finished) {
                webView.runOnMainThread {
                    webView.evaluateJavaScript(bridge.jsScript, completionHandler = null)
                }
            }
        }
    }

    client.webViewState = state
    client.webViewController = controller

    // Use a Box to overlay loading/error/dialogs on top of the WebView
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier) {
        UIKitView(
            factory = {
                webView.navigationDelegate = delegate
                webView.UIDelegate = uiDelegate
                onCreated(webView)
                webView
            },
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            update = { _ ->
                webView.navigationDelegate = delegate
                webView.UIDelegate = uiDelegate
            },
            onRelease = {
                onDispose(webView)
                if (state.webView === webView) {
                    state.webView = null
                }
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

@OptIn(ExperimentalForeignApi::class)
private fun WKWebView.loadUrlWithHeaders(
    url: String,
    headers: Map<String, String>,
) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    val request = NSMutableURLRequest.requestWithURL(nsUrl) as NSMutableURLRequest
    headers.forEach { (key, value) ->
        request.setValue(value, forHTTPHeaderField = key)
    }
    loadRequest(request)
}

@OptIn(ExperimentalForeignApi::class)
private class IosFullscreenVideoObserver(
    private val state: WebViewState,
) {
    private val notificationCenter = NSNotificationCenter.defaultCenter
    private var visibleObserver: NSObjectProtocol? = null
    private var hiddenObserver: NSObjectProtocol? = null

    fun start() {
        if (visibleObserver != null || hiddenObserver != null) return
        visibleObserver =
            notificationCenter.addObserverForName(
                name = UIWindowDidBecomeVisibleNotification,
                `object` = null,
                queue = null,
            ) { notification: NSNotification? ->
                val window = notification?.`object` as? UIWindow ?: return@addObserverForName
                if (window.isFullscreenVideoWindow()) {
                    handleEnter()
                }
            }
        hiddenObserver =
            notificationCenter.addObserverForName(
                name = UIWindowDidBecomeHiddenNotification,
                `object` = null,
                queue = null,
            ) { notification: NSNotification? ->
                val window = notification?.`object` as? UIWindow ?: return@addObserverForName
                if (window.isFullscreenVideoWindow()) {
                    handleExit()
                }
            }
    }

    fun stop() {
        visibleObserver?.let { notificationCenter.removeObserver(it) }
        hiddenObserver?.let { notificationCenter.removeObserver(it) }
        visibleObserver = null
        hiddenObserver = null
    }

    private fun handleEnter() {
        if (state.customViewState != null) return
        val placeholder = UIView(frame = UIScreen.mainScreen.bounds).apply {
            backgroundColor = UIColor.blackColor
        }
        state.customViewState =
            CustomViewState(
                view = placeholder,
                callback = PlatformCustomViewCallback {
                    handleExit()
                },
            )
    }

    private fun handleExit() {
        if (state.customViewState != null) {
            state.customViewState = null
        }
    }
}

private fun UIWindow.isFullscreenVideoWindow(): Boolean {
    val description = this.description
    if (description == null) return false
    return description.contains("FullScreen", ignoreCase = true)
}
