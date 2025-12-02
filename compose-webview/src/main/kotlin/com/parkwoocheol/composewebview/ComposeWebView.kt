package com.parkwoocheol.composewebview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A powerful and flexible wrapper around the Android WebView for Jetpack Compose.
 *
 * This composable allows you to harness the full power of the Android WebView while
 * benefiting from the declarative nature of Compose. It supports custom clients,
 * navigation control, state observation, lifecycle management, and JavaScript interfaces.
 *
 * @param url The URL to load.
 * @param modifier The modifier to be applied to the layout.
 * @param controller The controller to control the WebView (load, back, forward, post, evaluateJS, etc.).
 * @param javascriptInterfaces A map of interface objects to inject into JavaScript. Key is the name, Value is the object.
 * @param onCreated Called when the WebView is created. Use this to configure settings.
 * @param onDispose Called when the WebView is disposed.
 * @param client The [WebViewClient] to be used. Defaults to [ComposeWebViewClient].
 * @param chromeClient The [WebChromeClient] to be used. Defaults to [ComposeWebChromeClient].
 * @param factory A factory to create the WebView instance. Useful for custom WebView subclasses.
 * @param loadingContent A composable that is displayed when the WebView is in a loading state.
 * @param errorContent A composable that is displayed when the WebView encounters errors.
 * @param jsAlertContent A composable that is displayed when the WebView requests a JS Alert.
 * @param jsConfirmContent A composable that is displayed when the WebView requests a JS Confirm.
 * @param jsPromptContent A composable that is displayed when the WebView requests a JS Prompt.
 * @param customViewContent A composable that is displayed when the WebView requests a custom view (e.g. fullscreen video).
 * @param onPageStarted Callback for [WebViewClient.onPageStarted].
 * @param onPageFinished Callback for [WebViewClient.onPageFinished].
 * @param onReceivedError Callback for [WebViewClient.onReceivedError].
 * @param onProgressChanged Callback for [WebChromeClient.onProgressChanged].
 * @param onDownloadStart Callback for [DownloadListener.onDownloadStart].
 * @param onFindResultReceived Callback for [WebView.FindListener.onFindResultReceived].
 */
@Composable
fun ComposeWebView(
    url: String,
    modifier: Modifier = Modifier,
    controller: WebViewController = rememberWebViewController(),
    javascriptInterfaces: Map<String, Any> = emptyMap(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: ComposeWebViewClient = remember { ComposeWebViewClient() },
    chromeClient: ComposeWebChromeClient = remember { ComposeWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (List<WebViewError>) -> Unit = {},
    jsAlertContent: @Composable (JsDialogState.Alert) -> Unit = {},
    jsConfirmContent: @Composable (JsDialogState.Confirm) -> Unit = {},
    jsPromptContent: @Composable (JsDialogState.Prompt) -> Unit = {},
    customViewContent: (@Composable (CustomViewState) -> Unit)? = null,
    onPageStarted: (WebView, String?, Bitmap?) -> Unit = { _, _, _ -> },
    onPageFinished: (WebView, String?) -> Unit = { _, _ -> },
    onReceivedError: (WebView, WebResourceRequest?, WebResourceError?) -> Unit = { _, _, _ -> },
    onProgressChanged: (WebView, Int) -> Unit = { _, _ -> },
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null
) {
    val state = rememberSaveableWebViewState(url = url)
    ComposeWebView(
        state = state,
        modifier = modifier,
        controller = controller,
        javascriptInterfaces = javascriptInterfaces,
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
        onFindResultReceived = onFindResultReceived
    )
}

/**
 * A powerful and flexible wrapper around the Android WebView for Jetpack Compose.
 *
 * This composable allows you to harness the full power of the Android WebView while
 * benefiting from the declarative nature of Compose. It supports custom clients,
 * navigation control, state observation, lifecycle management, and JavaScript interfaces.
 *
 * @param state The state of the WebView, observing loading, title, etc.
 * @param modifier The modifier to be applied to the layout.
 * @param controller The controller to control the WebView (load, back, forward, post, evaluateJS, etc.).
 * @param javascriptInterfaces A map of interface objects to inject into JavaScript. Key is the name, Value is the object.
 * @param onCreated Called when the WebView is created. Use this to configure settings.
 * @param onDispose Called when the WebView is disposed.
 * @param client The [WebViewClient] to be used. Defaults to [ComposeWebViewClient].
 * @param chromeClient The [WebChromeClient] to be used. Defaults to [ComposeWebChromeClient].
 * @param factory A factory to create the WebView instance. Useful for custom WebView subclasses.
 * @param loadingContent A composable that is displayed when the WebView is in a loading state.
 * @param errorContent A composable that is displayed when the WebView encounters errors.
 * @param jsAlertContent A composable that is displayed when the WebView requests a JS Alert.
 * @param jsConfirmContent A composable that is displayed when the WebView requests a JS Confirm.
 * @param jsPromptContent A composable that is displayed when the WebView requests a JS Prompt.
 * @param customViewContent A composable that is displayed when the WebView requests a custom view (e.g. fullscreen video).
 * @param jsBridge The [WebViewJsBridge] to be used for JavaScript communication.
 * @param onPageStarted Callback for [WebViewClient.onPageStarted].
 * @param onPageFinished Callback for [WebViewClient.onPageFinished].
 * @param onReceivedError Callback for [WebViewClient.onReceivedError].
 * @param onProgressChanged Callback for [WebChromeClient.onProgressChanged].
 * @param onDownloadStart Callback for [DownloadListener.onDownloadStart].
 * @param onFindResultReceived Callback for [WebView.FindListener.onFindResultReceived].
 */
@Composable
fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    controller: WebViewController = rememberWebViewController(),
    javascriptInterfaces: Map<String, Any> = emptyMap(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: ComposeWebViewClient = remember { ComposeWebViewClient() },
    chromeClient: ComposeWebChromeClient = remember { ComposeWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (List<WebViewError>) -> Unit = {},
    jsAlertContent: @Composable (JsDialogState.Alert) -> Unit = {},
    jsConfirmContent: @Composable (JsDialogState.Confirm) -> Unit = {},
    jsPromptContent: @Composable (JsDialogState.Prompt) -> Unit = {},
    customViewContent: (@Composable (CustomViewState) -> Unit)? = null,
    jsBridge: WebViewJsBridge? = null,
    onPageStarted: (WebView, String?, Bitmap?) -> Unit = { _, _, _ -> },
    onPageFinished: (WebView, String?) -> Unit = { _, _ -> },
    onReceivedError: (WebView, WebResourceRequest?, WebResourceError?) -> Unit = { _, _, _ -> },
    onProgressChanged: (WebView, Int) -> Unit = { _, _ -> },
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null
) {
    val webView = state.webView
    val lifecycleOwner = LocalLifecycleOwner.current

    BackHandler(enabled = controller.canGoBack) {
        webView?.goBack()
    }

    webView?.let { wv ->
        LaunchedEffect(wv, controller) {
            withContext(Dispatchers.Main) {
                controller.handleNavigationEvents(wv)
            }
        }

        LaunchedEffect(wv, state) {
            snapshotFlow { state.content }.collect { content ->
                when (content) {
                    is WebContent.Url -> {
                        val url = content.url
                        if (url.isNotEmpty() && url != wv.url) {
                            wv.loadUrl(url, content.additionalHttpHeaders)
                        }
                    }

                    is WebContent.Data -> {
                        wv.loadDataWithBaseURL(
                            content.baseUrl,
                            content.data,
                            content.mimeType,
                            content.encoding,
                            content.historyUrl
                        )
                    }

                    is WebContent.Post -> {
                        wv.postUrl(content.url, content.postData)
                    }

                    is WebContent.NavigatorOnly -> {
                        // Do nothing, navigation is handled by controller
                    }
                }
            }
        }

        // Update JS interfaces if they change (though usually they are static)
        LaunchedEffect(javascriptInterfaces) {
            wv.injectJavascriptInterfaces(javascriptInterfaces)
        }

        // Inject JS Bridge script when page finishes loading
        if (jsBridge != null) {
            LaunchedEffect(state.loadingState) {
                if (state.loadingState is LoadingState.Finished) {
                    wv.evaluateJavascript(jsBridge.jsScript, null)
                }
            }
        }
    }

    // Inject state and callbacks into clients
    client.state = state
    client.controller = controller
    client.onPageStartedCallback = onPageStarted
    client.onPageFinishedCallback = onPageFinished
    client.onReceivedErrorCallback = onReceivedError

    chromeClient.state = state
    chromeClient.onProgressChangedCallback = onProgressChanged

    WebViewContainer(modifier = modifier) { layoutParams ->
        AndroidView(
            factory = { context ->
                val wv = factory?.invoke(context) ?: WebView(context).apply {
                    this.layoutParams = layoutParams
                }

                wv.apply {
                    webViewClient = client
                    webChromeClient = chromeClient

                    injectJavascriptInterfaces(javascriptInterfaces)
                    jsBridge?.attach(this)

                    onDownloadStart?.let { listener ->
                        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                            listener(url, userAgent, contentDisposition, mimetype, contentLength)
                        }
                    }

                    onFindResultReceived?.let { listener ->
                        setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                            listener(activeMatchOrdinal, numberOfMatches, isDoneCounting)
                        }
                    }
                }.also {
                    // Restore state if available
                    if (state.bundle != null) {
                        it.restoreState(state.bundle!!)
                    } else {
                        // Initial load logic if no bundle
                        when (val content = state.content) {
                            is WebContent.Url -> {
                                if (content.url.isNotEmpty()) {
                                    it.loadUrl(content.url, content.additionalHttpHeaders)
                                }
                            }

                            is WebContent.Data -> {
                                it.loadDataWithBaseURL(
                                    content.baseUrl,
                                    content.data,
                                    content.mimeType,
                                    content.encoding,
                                    content.historyUrl
                                )
                            }

                            is WebContent.Post -> {
                                it.postUrl(content.url, content.postData)
                            }

                            is WebContent.NavigatorOnly -> {
                                // Do nothing
                            }
                        }
                    }

                    state.webView = it
                    onCreated(it)
                }
            },
            modifier = Modifier,
            update = { _ -> },
            onRelease = {
                onDispose(it)
                state.webView = null
                it.destroy() // Explicitly destroy to prevent leaks
            }
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

    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            if (webView != null) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        webView.onResume()
                        webView.resumeTimers()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        webView.onPause()
                        webView.pauseTimers()
                    }

                    else -> Unit
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // WebView destroy is handled by AndroidView onRelease
        }
    }
}

@Composable
private fun WebViewContainer(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.BoxWithConstraintsScope.(android.widget.FrameLayout.LayoutParams) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val width =
            if (constraints.hasFixedWidth)
                LayoutParams.MATCH_PARENT
            else
                LayoutParams.WRAP_CONTENT
        val height =
            if (constraints.hasFixedHeight)
                LayoutParams.MATCH_PARENT
            else
                LayoutParams.WRAP_CONTENT

        val layoutParams = android.widget.FrameLayout.LayoutParams(
            width,
            height
        )
        content(layoutParams)
    }
}

@SuppressLint("JavascriptInterface")
private fun WebView.injectJavascriptInterfaces(interfaces: Map<String, Any>) {
    interfaces.forEach { (name, obj) ->
        addJavascriptInterface(obj, name)
    }
}
