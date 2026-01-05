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
    settings: WebViewSettings = WebViewSettings.Default,
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
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null,
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

| Parameter | Type | Description | Platform Support |
| :--- | :--- | :--- | :--- |
| `url` | `String` | The initial URL to load. Ignored if `state` is provided and already has content. | All platforms |
| `state` | `WebViewState` | The `WebViewState` object holding the content and status of the WebView. | All platforms |
| `modifier` | `Modifier` | The modifier to be applied to the layout. | All platforms |
| `settings` | `WebViewSettings` | Configuration settings for WebView behavior (user agent, JavaScript, cache, zoom, etc.). | Android (full), iOS (partial), Desktop (partial), Web (none) |
| `controller` | `WebViewController` | The `WebViewController` for programmatic control (load, back, forward, zoom, etc.). | All platforms* |
| `javaScriptInterfaces` | `Map<String, Any>` | Map of native objects to inject into JavaScript. Key is the JS object name. | Android, Desktop |
| `jsBridge` | `WebViewJsBridge?` | The `WebViewJsBridge` instance for type-safe, promise-based communication. | All platforms |
| `onCreated` | `(WebView) -> Unit` | Callback invoked when the native `WebView` instance is created. | All platforms |
| `onDispose` | `(WebView) -> Unit` | Callback invoked when the WebView is about to be destroyed. | All platforms |
| `client` | `ComposeWebViewClient` | Custom `ComposeWebViewClient` (wraps `WebViewClient`). | All platforms |
| `chromeClient` | `ComposeWebChromeClient` | Custom `ComposeWebChromeClient` (wraps `WebChromeClient`). | All platforms |
| `factory` | `((PlatformContext) -> WebView)?` | Optional factory to provide a custom `WebView` instance. | Android, iOS |
| `loadingContent` | `@Composable () -> Unit` | Composable to display while the page is loading. | All platforms |
| `errorContent` | `@Composable (List<WebViewError>) -> Unit` | Composable replacement content when an error occurs. | All platforms |
| `jsAlertContent` | `@Composable (JsDialogState.Alert) -> Unit` | Custom UI for JavaScript `alert()` dialogs. | Android, iOS |
| `jsConfirmContent` | `@Composable (JsDialogState.Confirm) -> Unit` | Custom UI for JavaScript `confirm()` dialogs. | Android, iOS |
| `jsPromptContent` | `@Composable (JsDialogState.Prompt) -> Unit` | Custom UI for JavaScript `prompt()` dialogs. | Android, iOS |
| `customViewContent` | `(@Composable (CustomViewState) -> Unit)?` | Custom view content (e.g., fullscreen video). | Android |
| `onDownloadStart` | `((String, String, String, String, Long) -> Unit)?` | Callback for download requests (url, userAgent, contentDisposition, mimeType, contentLength). | Android, iOS (partial) |
| `onFindResultReceived` | `((Int, Int, Boolean) -> Unit)?` | Callback for text search results (activeMatchOrdinal, numberOfMatches, isDoneCounting). | Android |

\* **Controller**: All platforms support basic navigation, but some features (like zoom) are platform-specific. See `WebViewController` documentation for details.

---

## Client Configuration

Starting from version 1.4.0, you can configure WebView clients using convenient composable functions instead of extending classes or passing individual callback parameters.

### Using rememberWebViewClient

Configure navigation and page lifecycle events:

```kotlin
val client = rememberWebViewClient {
    onPageStarted { view, url, favicon ->
        println("Started loading: $url")
    }

    onPageFinished { view, url ->
        println("Finished loading: $url")
    }

    onReceivedError { view, request, error ->
        println("Error: ${error?.description}")
    }

    shouldOverrideUrlLoading { view, request ->
        // Return true to prevent loading
        false
    }
}

ComposeWebView(
    state = rememberSaveableWebViewState(url = "https://example.com"),
    client = client,
    modifier = Modifier.fillMaxSize()
)
```

### Using rememberWebChromeClient

Configure progress tracking and console messages:

```kotlin
val chromeClient = rememberWebChromeClient {
    onProgressChanged { view, progress ->
        println("Loading: $progress%")
    }

    onConsoleMessage { view, message ->
        when (message.level) {
            ConsoleMessageLevel.ERROR -> {
                Log.e("WebView", "[JS Error] ${message.message}")
            }
            else -> {
                Log.d("WebView", "[JS] ${message.message}")
            }
        }
        false // Return true to suppress default console logging
    }
}

ComposeWebView(
    state = rememberSaveableWebViewState(url = "https://example.com"),
    chromeClient = chromeClient,
    modifier = Modifier.fillMaxSize()
)
```

### Method Chaining

You can also configure clients using method chaining:

```kotlin
val client = rememberWebViewClient()
    .onPageStarted { view, url, favicon ->
        println("Started: $url")
    }
    .onPageFinished { view, url ->
        println("Finished: $url")
    }
```

For more detailed examples and use cases, see the [Client Configuration Guide](../guides/client-configuration.md).

---

## See Also

- [Client Configuration Guide](../guides/client-configuration.md) - Detailed guide on configuring WebView clients
- [State Management](../guides/state-management.md) - Managing WebView state
- [Error Handling](../guides/errors.md) - Handling errors and edge cases
