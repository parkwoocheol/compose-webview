package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.client.ComposeWebChromeClient
import com.parkwoocheol.composewebview.client.ComposeWebViewClient

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
 * @param client The [ComposeWebViewClient] to be used.
 * @param chromeClient The [ComposeWebChromeClient] to be used.
 * @param factory A factory to create the WebView instance. Useful for custom WebView subclasses.
 * @param loadingContent A composable that is displayed when the WebView is in a loading state.
 * @param errorContent A composable that is displayed when the WebView encounters errors.
 * @param jsAlertContent A composable that is displayed when the WebView requests a JS Alert.
 * @param jsConfirmContent A composable that is displayed when the WebView requests a JS Confirm.
 * @param jsPromptContent A composable that is displayed when the WebView requests a JS Prompt.
 * @param customViewContent A composable that is displayed when the WebView requests a custom view (e.g. fullscreen video).
 * @param onPageStarted Callback for [ComposeWebViewClient.onPageStarted].
 * @param onPageFinished Callback for [ComposeWebViewClient.onPageFinished].
 * @param onReceivedError Callback for [ComposeWebViewClient.onReceivedError].
 * @param onProgressChanged Callback for [ComposeWebChromeClient.onProgressChanged].
 * @param onDownloadStart Callback for [android.webkit.DownloadListener.onDownloadStart].
 * @param onFindResultReceived Callback for [WebView.FindListener.onFindResultReceived].
 */
@Composable
expect fun ComposeWebView(
    url: String,
    modifier: Modifier = Modifier,
    controller: WebViewController = rememberWebViewController(),
    javascriptInterfaces: Map<String, Any> = emptyMap(),
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
    onPageStarted: (WebView, String?, PlatformBitmap?) -> Unit = { _, _, _ -> },
    onPageFinished: (WebView, String?) -> Unit = { _, _ -> },
    onReceivedError: (WebView, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit = { _, _, _ -> },

    onProgressChanged: (WebView, Int) -> Unit = { _, _ -> },
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null
)

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
 * @param client The [ComposeWebViewClient] to be used.
 * @param chromeClient The [ComposeWebChromeClient] to be used.
 * @param factory A factory to create the WebView instance. Useful for custom WebView subclasses.
 * @param loadingContent A composable that is displayed when the WebView is in a loading state.
 * @param errorContent A composable that is displayed when the WebView encounters errors.
 * @param jsAlertContent A composable that is displayed when the WebView requests a JS Alert.
 * @param jsConfirmContent A composable that is displayed when the WebView requests a JS Confirm.
 * @param jsPromptContent A composable that is displayed when the WebView requests a JS Prompt.
 * @param customViewContent A composable that is displayed when the WebView requests a custom view (e.g. fullscreen video).
 * @param jsBridge The [WebViewJsBridge] to be used for JavaScript communication.
 * @param onPageStarted Callback for [ComposeWebViewClient.onPageStarted].
 * @param onPageFinished Callback for [ComposeWebViewClient.onPageFinished].
 * @param onReceivedError Callback for [ComposeWebViewClient.onReceivedError].
 * @param onProgressChanged Callback for [ComposeWebChromeClient.onProgressChanged].
 * @param onDownloadStart Callback for [android.webkit.DownloadListener.onDownloadStart].
 * @param onFindResultReceived Callback for [WebView.FindListener.onFindResultReceived].
 */
@Composable
expect fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    controller: WebViewController = rememberWebViewController(),
    javascriptInterfaces: Map<String, Any> = emptyMap(),
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
    onPageStarted: (WebView, String?, PlatformBitmap?) -> Unit = { _, _, _ -> },
    onPageFinished: (WebView, String?) -> Unit = { _, _ -> },
    onReceivedError: (WebView, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit = { _, _, _ -> },

    onProgressChanged: (WebView, Int) -> Unit = { _, _ -> },
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null
)
