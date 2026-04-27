package com.parkwoocheol.composewebview

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.webkit.DownloadListener
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
 * @param javaScriptInterfaces A map of interface objects to inject into JavaScript. Key is the name, Value is the object.
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
 * @param onDownloadStart Callback for [DownloadListener.onDownloadStart].
 * @param onFindResultReceived Callback for [WebView.FindListener.onFindResultReceived].
 */
@Composable
internal actual fun ComposeWebViewImpl(
    url: String,
    modifier: Modifier,
    settings: WebViewSettings,
    releaseStrategy: WebViewReleaseStrategy,
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
    onStartActionMode: ((WebView, PlatformActionModeCallback?) -> PlatformActionModeCallback?)?,
) {
    val state = rememberSaveableWebViewState(url = url)

    ComposeWebView(
        state = state,
        modifier = modifier,
        settings = settings,
        releaseStrategy = releaseStrategy,
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
        onStartActionMode = onStartActionMode,
    )
}

@Composable
internal actual fun ComposeWebViewImpl(
    state: WebViewState,
    modifier: Modifier,
    settings: WebViewSettings,
    releaseStrategy: WebViewReleaseStrategy,
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
    onStartActionMode: ((WebView, PlatformActionModeCallback?) -> PlatformActionModeCallback?)?,
) {
    val webView = state.webView
    val lifecycleOwner = LocalLifecycleOwner.current

    // File chooser setup - default implementation
    var fileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uris =
                if (result.resultCode == Activity.RESULT_OK) {
                    WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
                } else {
                    null
                }
            fileChooserCallback?.onReceiveValue(uris)
            fileChooserCallback = null
        }

    chromeClient.defaultOnShowFileChooserCallback = { _, callback, params ->
        fileChooserCallback = callback
        try {
            launcher.launch(params.createIntent())
            true
        } catch (e: Exception) {
            fileChooserCallback?.onReceiveValue(null)
            fileChooserCallback = null
            false
        }
    }

    BackHandler(enabled = controller.canGoBack) {
        webView?.goBack()
    }

    DisposableEffect(controller, state) {
        controller.bindState(state)
        onDispose {
            controller.unbindState(state)
        }
    }

    webView?.let { wv ->
        LaunchedEffect(wv, controller) {
            withContext(Dispatchers.Main) {
                controller.handleNavigationEvents(wv)
            }
        }

        LaunchedEffect(wv, state) {
            snapshotFlow { state.currentContentRequest }.collect { request ->
                if (state.shouldSkipTopLevelLoadForCurrentRequest()) {
                    return@collect
                }

                when (val content = request.content) {
                    is WebContent.Url -> {
                        if (content.url.isNotEmpty()) {
                            wv.loadUrl(content.url, content.additionalHttpHeaders)
                        }
                    }

                    is WebContent.Data -> {
                        wv.loadDataWithBaseURL(
                            content.baseUrl,
                            content.data,
                            content.mimeType,
                            content.encoding,
                            content.historyUrl,
                        )
                    }

                    is WebContent.Post -> {
                        wv.postUrl(content.url, content.postData)
                        state.consumePostRequest()
                    }

                    is WebContent.NavigatorOnly -> {
                        // Do nothing, navigation is handled by controller
                    }
                }
            }
        }

        // Update JS interfaces if they change (though usually they are static)
        LaunchedEffect(javaScriptInterfaces) {
            wv.injectJavascriptInterfaces(javaScriptInterfaces)
        }

        // Inject JS Bridge script when page finishes loading
        jsBridge?.let { bridge ->
            LaunchedEffect(state.loadingState) {
                when (state.loadingState) {
                    is LoadingState.Loading -> bridge.onPageStarted()
                    is LoadingState.Finished -> {
                        bridge.pageFinishedBootstrapScript()?.let { script ->
                            wv.evaluateJavascript(script, null)
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    // Inject state into clients
    client.webViewState = state
    client.webViewController = controller

    chromeClient.state = state

    WebViewContainer(modifier = modifier) { layoutParams ->
        AndroidView(
            factory = { context ->
                val existingWebView = if (releaseStrategy == WebViewReleaseStrategy.KeepAlive) state.webView else null
                val isNewWebView = existingWebView == null
                val wv =
                    (
                        existingWebView
                            ?: factory?.invoke(context)
                            ?: object : WebView(context) {
                                override fun startActionMode(callback: android.view.ActionMode.Callback?): android.view.ActionMode? {
                                    val wrappedCallback = onStartActionMode?.invoke(this, callback) ?: callback
                                    return super.startActionMode(wrappedCallback)
                                }

                                override fun startActionMode(
                                    callback: android.view.ActionMode.Callback?,
                                    type: Int,
                                ): android.view.ActionMode? {
                                    val wrappedCallback = onStartActionMode?.invoke(this, callback) ?: callback
                                    return super.startActionMode(wrappedCallback, type)
                                }
                            }
                    ).apply {
                        this.layoutParams = layoutParams
                    }

                (wv.parent as? ViewGroup)?.removeView(wv)

                wv.apply {
                    // Inject context for DownloadUtils
                    DownloadUtils.setContext(context.applicationContext)

                    // Apply WebView settings
                    applySettings(settings)

                    webViewClient = client
                    webChromeClient = chromeClient

                    injectJavascriptInterfaces(javaScriptInterfaces)
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

                    // Track scroll position
                    setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
                        state.scrollPosition = ScrollPosition(scrollX, scrollY)
                    }
                }.also {
                    if (isNewWebView) {
                        // Restore state if available
                        val restoredState =
                            state.bundle?.let { bundle ->
                                it.restoreState(bundle)
                            }
                        if (restoredState != null) {
                            state.markTopLevelLoadHandledByRestore()
                        }

                        onCreated(it)
                    }

                    state.webView = it
                }
            },
            modifier = Modifier,
            update = { _ -> },
            onRelease = {
                if (releaseStrategy == WebViewReleaseStrategy.DestroyOnRelease) {
                    val bundle = createPlatformBundle()
                    it.platformSaveState(bundle)
                    state.bundle = bundle
                }
                onDispose(it)
                if (releaseStrategy == WebViewReleaseStrategy.DestroyOnRelease) {
                    state.webView = null
                    it.destroy() // Explicitly destroy to prevent leaks
                } else {
                    (it.parent as? ViewGroup)?.removeView(it)
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

    DisposableEffect(lifecycleOwner, webView) {
        val observer =
            LifecycleEventObserver { _, event ->
                webView?.let { wv ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            wv.onResume()
                            wv.resumeTimers()
                        }

                        Lifecycle.Event.ON_PAUSE -> {
                            wv.onPause()
                            wv.pauseTimers()
                        }

                        else -> Unit
                    }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // WebView release is handled by AndroidView onRelease based on releaseStrategy
        }
    }
}

@Composable
private fun WebViewContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.(FrameLayout.LayoutParams) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val width =
            if (constraints.hasFixedWidth) {
                LayoutParams.MATCH_PARENT
            } else {
                LayoutParams.WRAP_CONTENT
            }
        val height =
            if (constraints.hasFixedHeight) {
                LayoutParams.MATCH_PARENT
            } else {
                LayoutParams.WRAP_CONTENT
            }

        val layoutParams =
            FrameLayout.LayoutParams(
                width,
                height,
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

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.applySettings(webViewSettings: WebViewSettings) {
    settings.apply {
        // JavaScript
        javaScriptEnabled = webViewSettings.javaScriptEnabled

        // DOM Storage
        domStorageEnabled = webViewSettings.domStorageEnabled

        // Cache Mode
        cacheMode =
            when (webViewSettings.cacheMode) {
                CacheMode.DEFAULT -> android.webkit.WebSettings.LOAD_DEFAULT
                CacheMode.CACHE_ELSE_NETWORK -> android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                CacheMode.NO_CACHE -> android.webkit.WebSettings.LOAD_NO_CACHE
                CacheMode.CACHE_ONLY -> android.webkit.WebSettings.LOAD_CACHE_ONLY
            }

        // File Access
        allowFileAccess = webViewSettings.allowFileAccess
        allowContentAccess = webViewSettings.allowContentAccess

        // Zoom
        setSupportZoom(webViewSettings.supportZoom)
        builtInZoomControls = webViewSettings.supportZoom
        displayZoomControls = false // Hide zoom controls UI

        // Viewport
        loadWithOverviewMode = webViewSettings.loadWithOverviewMode
        useWideViewPort = webViewSettings.useWideViewPort

        // File URL Access
        allowFileAccessFromFileURLs = webViewSettings.allowFileAccessFromFileURLs
        allowUniversalAccessFromFileURLs = webViewSettings.allowUniversalAccessFromFileURLs

        // Media Playback
        mediaPlaybackRequiresUserGesture = webViewSettings.mediaPlaybackRequiresUserAction

        // User Agent
        webViewSettings.userAgent?.let { ua ->
            userAgentString = ua
        }

        // Dark Mode
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            isAlgorithmicDarkeningAllowed =
                when (webViewSettings.darkMode) {
                    DarkMode.AUTO -> true
                    DarkMode.LIGHT -> false
                    DarkMode.DARK -> true
                }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            forceDark =
                when (webViewSettings.darkMode) {
                    DarkMode.AUTO -> android.webkit.WebSettings.FORCE_DARK_AUTO
                    DarkMode.LIGHT -> android.webkit.WebSettings.FORCE_DARK_OFF
                    DarkMode.DARK -> android.webkit.WebSettings.FORCE_DARK_ON
                }
        }
    }
}

/**
 * Android-specific overload that allows customizing the file chooser behavior.
 *
 * This overload provides access to native Android types for file chooser customization,
 * allowing you to implement custom file selection logic.
 *
 * @param onShowFileChooser Callback for file chooser requests using native Android types.
 *                          Return true if you handle the request, false to use default behavior.
 *                          Parameters are:
 *                          - webView: The WebView instance
 *                          - filePathCallback: Callback to send selected files back to WebView
 *                          - fileChooserParams: Parameters describing the file chooser request
 *
 * Example:
 * ```kotlin
 * ComposeWebView(
 *     state = state,
 *     onShowFileChooser = { webView, callback, params ->
 *         // Custom file selection logic
 *         // launcher.launch(params.createIntent())
 *         // callback.onReceiveValue(selectedUris)
 *         true // Handled
 *     }
 * )
 * ```
 */
@Composable
fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    settings: WebViewSettings = WebViewSettings.Default,
    releaseStrategy: WebViewReleaseStrategy = WebViewReleaseStrategy.DestroyOnRelease,
    controller: WebViewController = rememberWebViewController(),
    javaScriptInterfaces: Map<String, Any> = emptyMap(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: ComposeWebViewClient = remember { ComposeWebViewClient() },
    chromeClient: ComposeWebChromeClient = remember { ComposeWebChromeClient() },
    factory: ((PlatformContext) -> WebView)? = null,
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (List<WebViewError>) -> Unit = {},
    jsAlertContent: @Composable (JsDialogState.Alert) -> Unit = {},
    jsConfirmContent: @Composable (JsDialogState.Confirm) -> Unit = {},
    jsPromptContent: @Composable (JsDialogState.Prompt) -> Unit = {},
    customViewContent: (@Composable (CustomViewState) -> Unit)? = null,
    jsBridge: WebViewJsBridge? = null,
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null,
    onShowFileChooser: (
        (
            android.webkit.WebView,
            android.webkit.ValueCallback<Array<android.net.Uri>>,
            android.webkit.WebChromeClient.FileChooserParams,
        ) -> Boolean
    )? = null,
    onStartActionMode: ((WebView, PlatformActionModeCallback?) -> PlatformActionModeCallback?)? = null,
) {
    // Set the custom file chooser callback if provided
    chromeClient.customOnShowFileChooserCallback = onShowFileChooser

    // Call the base implementation
    ComposeWebView(
        state = state,
        modifier = modifier,
        settings = settings,
        releaseStrategy = releaseStrategy,
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
        jsBridge = jsBridge,
        onDownloadStart = onDownloadStart,
        onFindResultReceived = onFindResultReceived,
        onStartActionMode = onStartActionMode,
    )
}
