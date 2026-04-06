package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import kotlinx.coroutines.flow.collectLatest
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefRequestHandlerAdapter
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

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
    client: com.parkwoocheol.composewebview.client.ComposeWebViewClient,
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
    val state = rememberWebViewState(url)

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
        jsBridge = null,
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
    client.webViewState = state
    client.webViewController = controller

    var initialized by remember { mutableStateOf(false) }
    var initializationError by remember { mutableStateOf<Throwable?>(null) }
    var webView: DesktopWebView? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        runCatching {
            DesktopCefRuntime.initialize(settings)
        }.onSuccess {
            initialized = true
        }.onFailure { throwable ->
            initializationError = throwable
        }
    }

    if (initialized) {
        DisposableEffect(Unit) {
            val cefClient = DesktopCefRuntime.createClient()

            if (cefClient == null) {
                initializationError = IllegalStateException("Failed to create JCEF client.")
                onDispose { }
            } else {
                var activeView: DesktopWebView? = null

                var lastLoadingState = false

                // Add Load Handler
                cefClient.addLoadHandler(
                    object : CefLoadHandlerAdapter() {
                        override fun onLoadingStateChange(
                            browser: CefBrowser?,
                            isLoading: Boolean,
                            canGoBack: Boolean,
                            canGoForward: Boolean,
                        ) {
                            if (isLoading && !lastLoadingState) {
                                state.loadingState = LoadingState.Loading(0f)
                                state.lastLoadedUrl = browser?.url
                                client.onPageStarted(activeView, browser?.url, null)
                            } else {
                                state.loadingState = LoadingState.Finished
                                if (lastLoadingState) {
                                    client.onPageFinished(activeView, browser?.url)
                                }
                            }
                            lastLoadingState = isLoading
                            controller.canGoBack = canGoBack
                            controller.canGoForward = canGoForward
                        }
                    },
                )

                // Add Request Handler for shouldOverrideUrlLoading
                cefClient.addRequestHandler(
                    object : CefRequestHandlerAdapter() {
                        override fun onBeforeBrowse(
                            browser: CefBrowser?,
                            frame: CefFrame?,
                            request: org.cef.network.CefRequest?,
                            user_gesture: Boolean,
                            is_redirect: Boolean,
                        ): Boolean {
                            if (request == null) return false
                            val platformRequest =
                                PlatformWebResourceRequest(
                                    url = request.url,
                                    method = request.method,
                                    headers = emptyMap(),
                                    isForMainFrame = true,
                                )
                            return client.shouldOverrideUrlLoading(activeView, platformRequest)
                        }
                    },
                )

                // Add Display Handler
                cefClient.addDisplayHandler(
                    object : CefDisplayHandlerAdapter() {
                        override fun onAddressChange(
                            browser: CefBrowser?,
                            frame: CefFrame?,
                            url: String?,
                        ) {
                            state.lastLoadedUrl = url
                        }

                        override fun onTitleChange(
                            browser: CefBrowser?,
                            title: String?,
                        ) {
                            state.pageTitle = title
                        }
                    },
                )

                // Determine initial URL from state
                val initialUrl =
                    when (val content = state.content) {
                        is WebContent.Url -> content.url
                        is WebContent.Data -> "about:blank"
                        is WebContent.Post -> content.url
                        is WebContent.NavigatorOnly -> "about:blank"
                    }

                val browser = cefClient.createBrowser(initialUrl, false, false)
                browser.createImmediately()
                if (initialUrl.isNotBlank()) {
                    browser.loadURL(initialUrl)
                }
                val createdView = DesktopWebView(browser, cefClient)
                createdView.platformJavaScriptEnabled = settings.javaScriptEnabled
                createdView.platformDomStorageEnabled = settings.domStorageEnabled
                createdView.platformSupportZoom = settings.supportZoom
                createdView.platformBuiltInZoomControls = settings.supportZoom
                createdView.platformDisplayZoomControls = false
                activeView = createdView
                webView = createdView
                state.webView = createdView

                javaScriptInterfaces.forEach { (name, obj) ->
                    createdView.platformAddJavascriptInterface(obj, name)
                }
                onCreated(createdView)
                jsBridge?.attach(createdView)

                onDispose {
                    activeView?.let(onDispose)
                    if (state.webView === activeView) {
                        state.webView = null
                    }
                    webView = null
                    browser.close(true)
                    cefClient.dispose()
                }
            }
        }

        if (webView != null) {
            DisposableEffect(controller, state) {
                controller.bindState(state)
                onDispose {
                    controller.unbindState(state)
                }
            }

            LaunchedEffect(webView, controller) {
                controller.handleNavigationEvents(webView!!)
            }

            LaunchedEffect(webView) {
                snapshotFlow { state.currentContentRequest }.collectLatest { request ->
                    val activeWebView = webView ?: return@collectLatest
                    when (val content = request.content) {
                        is WebContent.Url -> {
                            if (content.url.isNotEmpty()) {
                                activeWebView.platformLoadUrl(content.url, content.additionalHttpHeaders)
                            }
                        }

                        is WebContent.Data -> {
                            activeWebView.platformLoadDataWithBaseURL(
                                content.baseUrl,
                                content.data,
                                content.mimeType,
                                content.encoding,
                                content.historyUrl,
                            )
                        }

                        is WebContent.Post -> {
                            activeWebView.platformPostUrl(content.url, content.postData)
                        }

                        is WebContent.NavigatorOnly -> Unit
                    }
                }
            }

            SwingPanel(
                modifier = modifier,
                factory = { webView!! },
                update = { },
            )

            // Inject JS Bridge script when page finishes loading
            jsBridge?.let { bridge ->
                LaunchedEffect(state.loadingState) {
                    if (state.loadingState is LoadingState.Finished) {
                        state.webView?.platformEvaluateJavascript(bridge.jsScript, null)
                    }
                }
            }
        } else {
            // Fallback if browser creation fails
            SwingPanel(
                modifier = modifier,
                factory = {
                    val jEditorPane =
                        javax.swing.JEditorPane().apply {
                            isEditable = false
                            contentType = "text/html"
                            text = "<html><body><h1>JCEF Initialization Failed</h1><p>Could not create JCEF browser.</p></body></html>"
                        }
                    val scrollPane = javax.swing.JScrollPane(jEditorPane)
                    val panel = JPanel(BorderLayout())
                    panel.add(scrollPane, BorderLayout.CENTER)
                    panel
                },
            )
        }
    } else if (initializationError != null) {
        errorContent(
            listOf(
                WebViewError(
                    description = initializationError?.message ?: "Desktop WebView initialization failed",
                ),
            ),
        )
    } else {
        // Show loading content while initializing JCEF runtime.
        loadingContent()
        SwingPanel(
            modifier = modifier,
            factory = {
                val label = JLabel("Initializing WebView (JCEF)...")
                label.horizontalAlignment = JLabel.CENTER
                val panel = JPanel(BorderLayout())
                panel.add(label, BorderLayout.CENTER)
                panel
            },
        )
    }
}
