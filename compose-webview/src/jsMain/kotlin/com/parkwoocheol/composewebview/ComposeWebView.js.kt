package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Iframe
import org.w3c.dom.HTMLIFrameElement

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
    onConsoleMessage: ((WebView, ConsoleMessage) -> Boolean)?,
) {
    // Connect callbacks
    chromeClient.onConsoleMessageCallback = onConsoleMessage

    // Use Iframe from org.jetbrains.compose.web.dom
    // Note: This works best when using Compose HTML (DOM).
    // If using Compose Multiplatform (Canvas), this might not render correctly without an overlay.
    // But this is the standard way to access DOM elements in KMP Web.

    // Note: Web platform (iframe) has very limited configuration options
    // Most WebViewSettings cannot be applied as iframe is controlled by the browser

    org.jetbrains.compose.web.dom.Iframe(
        attrs = {
            attr("src", url)
            style {
                width(100.percent)
                height(100.percent)
                border(0.px)
            }
            ref {
                val iframe = it as HTMLIFrameElement
                val webView = WebView(iframe)
                onCreated(webView)
                onDispose { onDispose(webView) }
            }
        },
    )
}

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
    onConsoleMessage: ((WebView, ConsoleMessage) -> Boolean)?,
) {
    // Connect callbacks
    chromeClient.onConsoleMessageCallback = onConsoleMessage

    LaunchedEffect(state) {
        snapshotFlow { state.content }.collect { content ->
            if (content is WebContent.Url) {
                state.lastLoadedUrl = content.url
            }
        }
    }

    // Handle navigation events (Back, Forward, Reload)
    state.webView?.let { webView ->
        LaunchedEffect(webView, controller) {
            controller.handleNavigationEvents(webView)
        }
    }

    // Note: Web platform (iframe) has very limited configuration options
    // Most WebViewSettings cannot be applied as iframe is controlled by the browser
    // Settings like JavaScript, cache mode, user agent, etc. are browser-controlled

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
                val iframe = it as HTMLIFrameElement
                val webView = WebView(iframe)
                state.webView = webView

                // Attach JSBridge
                jsBridge?.attach(webView)

                // Try to set up scroll tracking (only works for same-origin iframes)
                try {
                    iframe.contentWindow?.addEventListener("scroll", { _ ->
                        try {
                            val scrollX = iframe.contentWindow?.scrollX?.toInt() ?: 0
                            val scrollY = iframe.contentWindow?.scrollY?.toInt() ?: 0
                            state.scrollPosition = ScrollPosition(scrollX, scrollY)
                        } catch (e: dynamic) {
                            // CORS restriction - cannot access scroll position
                        }
                    })
                } catch (e: dynamic) {
                    // CORS restriction - cannot add event listener
                }

                onCreated(webView)

                // Handle Load Events
                iframe.onload = { _ ->
                    state.loadingState = LoadingState.Finished
                    // Inject JS Bridge script
                    jsBridge?.let { bridge ->
                        webView.platformEvaluateJavascript(bridge.jsScript, null)
                    }
                }

                onDispose {
                    onDispose(webView)
                    state.webView = null
                }
            }
        },
    )
}
