# ComposeWebView API

The primary entry point for the library is the `ComposeWebView` composable.

---

## Signatures

### 1. Basic Overload

Use this when you just want to load a URL without managing state explicitly.

```kotlin
@Composable
fun ComposeWebView(
    url: String,
    modifier: Modifier = Modifier,
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
    onPageStarted: (WebView, String?, PlatformBitmap?) -> Unit = { _, _, _ -> },
    onPageFinished: (WebView, String?) -> Unit = { _, _ -> },
    onReceivedError: (WebView, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit = { _, _, _ -> },
    onProgressChanged: (WebView, Int) -> Unit = { _, _ -> },
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null
)
```

### 2. State-Aware Overload

Use this when you are providing a `WebViewState` (e.g., from `rememberSaveableWebViewState`).

```kotlin
@Composable
fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    controller: WebViewController = rememberWebViewController(),
    // ... all other parameters are identical
)
```

---

## Parameters

| Parameter | Description |
| :--- | :--- |
| `url` | The initial URL to load. Ignored if `state` is provided and already has content. |
| `state` | The `WebViewState` object holding the content and status of the WebView. |
| `modifier` | The modifier to be applied to the layout. |
| `controller` | The `WebViewController` for programmatic control (load, back, forward). |
| `javaScriptInterfaces` | Map of native objects to inject into JavaScript. Key is the JS object name. |
| `jsBridge` | The `WebViewJsBridge` instance for type-safe communication. |
| `onCreated` | Callback invoked when the native `WebView` instance is created. Use this to configure settings. |
| `onDispose` | Callback invoked when the WebView is about to be destroyed. |
| `client` | Custom `ComposeWebViewClient` (wraps `WebViewClient`). |
| `chromeClient` | Custom `ComposeWebChromeClient` (wraps `WebChromeClient`). |
| `factory` | Optional factory to provide a custom `WebView` instance (Android only). |
| `loadingContent` | Composable to display while the page is loading. |
| `errorContent` | Composable replacement content when an error occurs. |
| `onPageStarted` | Standard `WebViewClient` callback. |
| `onPageFinished` | Standard `WebViewClient` callback. |
