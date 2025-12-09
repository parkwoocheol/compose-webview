package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRendering
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

@Composable
internal actual fun ComposeWebViewImpl(
    url: String,
    modifier: Modifier,
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
    onPageStarted: (WebView, String?, PlatformBitmap?) -> Unit,
    onPageFinished: (WebView, String?) -> Unit,
    onReceivedError: (WebView, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit,
    onProgressChanged: (WebView, Int) -> Unit,
    onDownloadStart: ((String, String, String, String, Long) -> Unit)?,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)?,
) {
    val state = rememberWebViewState(url)
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
        jsBridge = null,
        onPageStarted = onPageStarted,
        onPageFinished = onPageFinished,
        onReceivedError = onReceivedError,
        onProgressChanged = onProgressChanged,
        onDownloadStart = onDownloadStart,
        onFindResultReceived = onFindResultReceived,
    )
}

@Composable
internal actual fun ComposeWebViewImpl(
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
    var initialized by remember { mutableStateOf(false) }
    var browser: KCEFBrowser? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        // Initialize KCEF (downloads binaries if needed)
        KCEF.init(builder = {
            // Configure if needed
            // addArgs("--no-sandbox")
        }, onError = {
            it?.printStackTrace()
        }, onRestartRequired = {
            // Handle restart
        })
        initialized = true
    }

    if (initialized) {
        DisposableEffect(Unit) {
            val kcefClient = KCEF.newClientOrNullBlocking()

            // Add Load Handler
            kcefClient?.addLoadHandler(
                object : CefLoadHandlerAdapter() {
                    override fun onLoadingStateChange(
                        browser: CefBrowser?,
                        isLoading: Boolean,
                        canGoBack: Boolean,
                        canGoForward: Boolean,
                    ) {
                        if (isLoading) {
                            state.loadingState = LoadingState.Loading(0f)
                            // We don't have a direct "onPageStarted" equivalent with URL here easily without CefDisplayHandler
                            // But we can approximate.
                        } else {
                            state.loadingState = LoadingState.Finished
                            // Trigger onPageFinished
                            browser?.let {
                                // We need a way to get the WebView instance here, but it's created inside the factory.
                                // For now, we update state directly which is what the default client does.
                                // To support custom clients, we should ideally call client.onPageFinished.
                                // However, we don't have the WebView instance yet or it's hard to access.
                                // We will rely on state updates for now.
                            }
                        }
                        controller.canGoBack = canGoBack
                        controller.canGoForward = canGoForward
                    }
                },
            )

            // Add Request Handler for shouldOverrideUrlLoading
            kcefClient?.addRequestHandler(
                object : org.cef.handler.CefRequestHandlerAdapter() {
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
                                // Headers not easily accessible here in this signature
                                headers = emptyMap(),
                                // Simplified
                                isForMainFrame = true,
                            )
                        // If client returns true, cancel navigation (return true)
                        // We need to pass a WebView instance. Since we don't have the wrapper easily, pass null for now
                        // or refactor to hold reference.
                        // For the sample app's CustomClient, it just checks the URL.
                        return client.shouldOverrideUrlLoading(null, platformRequest)
                    }
                },
            )

            // Add Display Handler
            kcefClient?.addDisplayHandler(
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
                    is WebContent.Data -> "about:blank" // TODO: Handle data loading
                    is WebContent.Post -> content.url
                    is WebContent.NavigatorOnly -> "about:blank"
                }

            val newBrowser = kcefClient?.createBrowser(initialUrl, CefRendering.DEFAULT, false)
            browser = newBrowser

            onDispose {
                browser?.dispose()
            }
        }

        if (browser != null) {
            SwingPanel(
                modifier = modifier,
                factory = {
                    val webView = DesktopWebView(browser!!)
                    onCreated(webView)
                    jsBridge?.attach(webView)
                    webView
                },
                update = {
                    // Handle state updates if needed, e.g. loading new URL from state
                    // For now, we rely on controller for navigation
                },
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
            // Fallback to JEditorPane if CEF fails
            SwingPanel(
                modifier = modifier,
                factory = {
                    val jEditorPane =
                        javax.swing.JEditorPane().apply {
                            isEditable = false
                            contentType = "text/html"
                            text = "<html><body><h1>CEF Initialization Failed</h1><p>Could not create CEF browser.</p></body></html>"
                        }
                    val scrollPane = javax.swing.JScrollPane(jEditorPane)
                    val panel = JPanel(BorderLayout())
                    panel.add(scrollPane, BorderLayout.CENTER)
                    panel
                },
            )
        }
    } else {
        // Show loading content while initializing CEF (downloading binaries)
        loadingContent()
        // Optional: Show a SwingPanel with "Initializing..." text if loadingContent is empty
        SwingPanel(
            modifier = modifier,
            factory = {
                val label = JLabel("Initializing WebView (Downloading CEF)...")
                label.horizontalAlignment = JLabel.CENTER
                val panel = JPanel(BorderLayout())
                panel.add(label, BorderLayout.CENTER)
                panel
            },
        )
    }
}
