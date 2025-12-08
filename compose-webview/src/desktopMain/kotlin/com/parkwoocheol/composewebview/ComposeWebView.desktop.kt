package com.parkwoocheol.composewebview

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBrowser
import org.cef.browser.CefRendering
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest

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
            val client = KCEF.newClientOrNullBlocking()
            
            // Add Load Handler
            client?.addLoadHandler(object : CefLoadHandlerAdapter() {
                override fun onLoadingStateChange(
                    browser: CefBrowser?,
                    isLoading: Boolean,
                    canGoBack: Boolean,
                    canGoForward: Boolean
                ) {
                    state.loadingState = if (isLoading) LoadingState.Loading(0f) else LoadingState.Finished
                    controller.canGoBack = canGoBack
                    controller.canGoForward = canGoForward
                }
            })

            // Add Display Handler
            client?.addDisplayHandler(object : CefDisplayHandlerAdapter() {
                override fun onAddressChange(browser: CefBrowser?, frame: CefFrame?, url: String?) {
                    state.lastLoadedUrl = url
                }

                override fun onTitleChange(browser: CefBrowser?, title: String?) {
                    state.pageTitle = title
                }
            })

            val newBrowser = client?.createBrowser(url, CefRendering.DEFAULT, false)
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
                    webView
                },
                update = {
                    browser?.loadURL(url)
                }
            )
        } else {
            // Fallback to JEditorPane if CEF fails
            SwingPanel(
                modifier = modifier,
                factory = {
                    val jEditorPane = javax.swing.JEditorPane().apply {
                        isEditable = false
                        contentType = "text/html"
                        text = "<html><body><h1>CEF Initialization Failed</h1><p>Could not create CEF browser.</p></body></html>"
                    }
                    val scrollPane = javax.swing.JScrollPane(jEditorPane)
                    val panel = JPanel(BorderLayout())
                    panel.add(scrollPane, BorderLayout.CENTER)
                    // Note: We cannot pass 'panel' to onCreated because it expects 'WebView' (DesktopWebView).
                    // Since initialization failed, we can't create a DesktopWebView.
                    // We might need to handle this case better or make DesktopWebView nullable/flexible.
                    // For now, we skip onCreated or pass a dummy if possible, but DesktopWebView requires a browser.
                    // Let's just render the error panel.
                    panel
                }
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
            }
        )
    }
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
    ComposeWebView(
        url = url,
        modifier = modifier,
        controller = controller,
        javascriptInterfaces = javascriptInterfaces,
        onCreated = { webView ->
            jsBridge?.attach(webView)
            onCreated(webView)
        },
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

    // Inject JS Bridge script when page finishes loading
    jsBridge?.let { bridge ->
        LaunchedEffect(state.loadingState) {
            if (state.loadingState is LoadingState.Finished) {
                state.webView?.platformEvaluateJavascript(bridge.jsScript, null)
            }
        }
    }
}
