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
                    val panel = JPanel(BorderLayout())
                    panel.add(browser!!.uiComponent, BorderLayout.CENTER)
                    onCreated(panel)
                    panel
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
                    onCreated(panel)
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
