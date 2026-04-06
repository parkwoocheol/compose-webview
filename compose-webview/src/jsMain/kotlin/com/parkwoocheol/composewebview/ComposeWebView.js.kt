package com.parkwoocheol.composewebview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val density = LocalDensity.current.density
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

        val webView = WebView(iframe)
        onCreated(webView)

        onDispose {
            try {
                document.body?.removeChild(container)
            } catch (_: Throwable) {
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
    val density = LocalDensity.current.density
    val componentReady = remember { mutableStateOf(false) }
    val iframeElement = remember { mutableStateOf<HTMLIFrameElement?>(null) }
    val currentContentRequest = state.currentContentRequest

    DisposableEffect(controller, state) {
        controller.bindState(state)
        onDispose {
            controller.unbindState(state)
        }
    }

    // Handle navigation events (Back, Forward, Reload)
    state.webView?.let { webView ->
        LaunchedEffect(webView, controller) {
            controller.handleNavigationEvents(webView)
        }
    }

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

        val webView = WebView(iframe)
        state.webView = webView
        jsBridge?.attach(webView)

        try {
            iframe.contentWindow?.addEventListener("scroll", { _ ->
                try {
                    val scrollX = iframe.contentWindow?.scrollX?.toInt() ?: 0
                    val scrollY = iframe.contentWindow?.scrollY?.toInt() ?: 0
                    state.scrollPosition = ScrollPosition(scrollX, scrollY)
                } catch (_: dynamic) {
                }
            })
        } catch (_: dynamic) {
        }

        iframe.onload = { _ ->
            state.loadingState = LoadingState.Finished
            jsBridge?.let { bridge ->
                webView.platformEvaluateJavascript(bridge.jsScript, null)
            }
            null
        }

        onCreated(webView)

        onDispose {
            try {
                document.body?.removeChild(container)
            } catch (_: Throwable) {
            }
            componentReady.value = false
            iframeElement.value = null
            onDispose(webView)
            state.webView = null
        }
    }

    LaunchedEffect(currentContentRequest.version, iframeElement.value) {
        iframeElement.value?.let { frame ->
            when (val content = currentContentRequest.content) {
                is WebContent.Url -> {
                    state.lastLoadedUrl = content.url
                    if (content.url.isNotEmpty()) {
                        frame.src = content.url
                    }
                }

                is WebContent.Data -> {
                    frame.srcdoc = content.data
                }

                is WebContent.Post -> {
                    state.webView?.platformPostUrl(content.url, content.postData)
                    state.consumePostRequest()
                }

                is WebContent.NavigatorOnly -> Unit
            }
        }
    }
}
