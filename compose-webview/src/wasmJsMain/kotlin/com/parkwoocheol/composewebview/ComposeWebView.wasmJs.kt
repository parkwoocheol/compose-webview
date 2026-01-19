package com.parkwoocheol.composewebview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLIFrameElement

// WASM WebView implementation using dynamic positioning
// iframe is positioned at the exact location of the Composable

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
    onStartActionMode: ((WebView, PlatformActionModeCallback?) -> PlatformActionModeCallback?)?,
) {
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val componentReady = remember { mutableStateOf(false) }

    val container =
        remember {
            (document.createElement("div") as HTMLDivElement).apply {
                className = "webview-container"
                style.position = "absolute"
            }
        }

    val iframe =
        remember {
            (document.createElement("iframe") as HTMLIFrameElement).apply {
                src = url
                style.apply {
                    width = "100%"
                    height = "100%"
                    border = "none"
                }
                setAttribute("allow", "fullscreen")
            }
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val location = coordinates.positionInWindow().round()
                    val size = coordinates.size

                    if (componentReady.value) {
                        // Update iframe position and size dynamically
                        container.style.apply {
                            left = "${location.x / density}px"
                            top = "${location.y / density}px"
                            width = "${size.width / density}px"
                            height = "${size.height / density}px"
                        }
                    }
                },
    )

    DisposableEffect(container, iframe) {
        container.appendChild(iframe)
        document.body?.appendChild(container)
        componentReady.value = true

        println("WASM WebView: Created iframe for $url")

        val webView = WebView(iframe)
        onCreated(webView)

        onDispose {
            println("WASM WebView: Disposing iframe")
            try {
                document.body?.removeChild(container)
            } catch (e: Throwable) {
                println("Failed to remove WebView container: ${e.message}")
            }
            componentReady.value = false
            onDispose(webView)
        }
    }
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
    onDownloadStart: ((String, String, String, String, Long) -> Unit)?,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)?,
    onStartActionMode: ((WebView, PlatformActionModeCallback?) -> PlatformActionModeCallback?)?,
) {
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val componentReady = remember { mutableStateOf(false) }
    val iframeElement = remember { mutableStateOf<HTMLIFrameElement?>(null) }

    LaunchedEffect(state) {
        snapshotFlow { state.content }.collect { content ->
            if (content is WebContent.Url) {
                state.lastLoadedUrl = content.url
            }
        }
    }

    // Handle navigation events
    state.webView?.let { webView ->
        LaunchedEffect(webView, controller) {
            controller.handleNavigationEvents(webView)
        }
    }

    val url = state.lastLoadedUrl ?: ""

    val container =
        remember {
            (document.createElement("div") as HTMLDivElement).apply {
                className = "webview-container"
                style.position = "absolute"
            }
        }

    val iframe =
        remember {
            (document.createElement("iframe") as HTMLIFrameElement).apply {
                style.apply {
                    width = "100%"
                    height = "100%"
                    border = "none"
                }
                setAttribute("allow", "fullscreen")
            }
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val location = coordinates.positionInWindow().round()
                    val size = coordinates.size

                    if (componentReady.value) {
                        // Update iframe position and size dynamically
                        container.style.apply {
                            left = "${location.x / density}px"
                            top = "${location.y / density}px"
                            width = "${size.width / density}px"
                            height = "${size.height / density}px"
                        }
                    }
                },
    )

    DisposableEffect(container, iframe) {
        container.appendChild(iframe)
        document.body?.appendChild(container)
        componentReady.value = true
        iframeElement.value = iframe

        println("WASM WebView: Created iframe")

        val webView = WebView(iframe)
        state.webView = webView

        // Attach JSBridge
        jsBridge?.attach(webView)

        // Load event handler
        iframe.onload = { _ ->
            println("WASM WebView: iframe loaded")
            state.loadingState = LoadingState.Finished
            jsBridge?.let { bridge ->
                webView.platformEvaluateJavascript(bridge.jsScript, null)
            }
            null
        }

        onCreated(webView)

        onDispose {
            println("WASM WebView: Disposing iframe")
            try {
                document.body?.removeChild(container)
            } catch (e: Throwable) {
                println("Failed to remove WebView container: ${e.message}")
            }
            componentReady.value = false
            iframeElement.value = null
            onDispose(webView)
            state.webView = null
        }
    }

    // Update iframe URL when state changes
    SideEffect {
        iframeElement.value?.let { frame ->
            if (url.isNotEmpty() && frame.src != url) {
                println("WASM WebView: Updating URL to $url")
                frame.src = url
            }
        }
    }
}
