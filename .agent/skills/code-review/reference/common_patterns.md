# Common Patterns

Established architecture patterns and best practices for ComposeWebView.

## Architecture Overview

ComposeWebView follows these core architectural principles:

1. **Multiplatform-first**: Common logic in `commonMain`, platform specifics via expect/actual
2. **State management**: Reactive state via `WebViewState`
3. **Controller pattern**: Actions via `WebViewController`
4. **Composable-centric**: UI as Composable functions

## State Management Pattern

### WebViewState

Centralized state holder for WebView:

```kotlin
@Stable
class WebViewState(
    initialUrl: String? = null
) {
    var loadingState by mutableStateOf<LoadingState>(LoadingState.Idle)
    var lastLoadedUrl by mutableStateOf<String?>(initialUrl)
    var pageTitle by mutableStateOf<String?>(null)
    var pageIcon by mutableStateOf<ImageBitmap?>(null)
    var errorsForCurrentRequest by mutableStateOf<List<WebViewError>>(emptyList())

    // Navigation state
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
}
```

**Usage**:
```kotlin
@Composable
fun MyWebView() {
    val state = rememberWebViewState(
        initialUrl = "https://example.com"
    )

    // State is reactive - UI updates automatically
    if (state.loadingState == LoadingState.Loading) {
        LoadingIndicator()
    }

    ComposeWebView(state = state)
}
```

### State Hoisting

✅ **Recommended**: Hoist state to caller

```kotlin
@Composable
fun WebViewScreen(
    url: String,
    onNavigate: (String) -> Unit
) {
    val state = rememberWebViewState(url)

    // State is hoisted - caller has access
    LaunchedEffect(state.lastLoadedUrl) {
        state.lastLoadedUrl?.let { onNavigate(it) }
    }

    ComposeWebView(state = state)
}
```

❌ **Avoid**: Internal state without access

```kotlin
@Composable
fun WebViewScreen() {
    val state = rememberWebViewState() // Caller can't access

    ComposeWebView(state = state)
}
```

## Controller Pattern

### WebViewController

Separates actions from state:

```kotlin
interface WebViewController {
    val state: WebViewState

    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String> = emptyMap())
    fun goBack()
    fun goForward()
    fun reload()
    fun stopLoading()
}
```

**Usage**:
```kotlin
@Composable
fun WebViewWithControls() {
    val state = rememberWebViewState("https://example.com")
    val controller = rememberWebViewController(state)

    Column {
        ComposeWebView(state = state, modifier = Modifier.weight(1f))

        Row {
            Button(
                onClick = { controller.goBack() },
                enabled = state.canGoBack
            ) {
                Text("Back")
            }
            Button(onClick = { controller.reload() }) {
                Text("Reload")
            }
        }
    }
}
```

## Composable Patterns

### Remember Functions

Create and remember instances:

```kotlin
@Composable
fun rememberWebViewState(
    initialUrl: String? = null
): WebViewState {
    return remember { WebViewState(initialUrl) }
}

@Composable
fun rememberWebViewController(
    state: WebViewState
): WebViewController {
    // Platform-specific implementation
    return remember(state) {
        createPlatformWebViewController(state)
    }
}
```

### Effect Handlers

#### LaunchedEffect

For coroutines and side effects:

```kotlin
@Composable
fun WebViewWithAnalytics(state: WebViewState) {
    // Track page loads
    LaunchedEffect(state.lastLoadedUrl) {
        state.lastLoadedUrl?.let { url ->
            analyticsService.trackPageView(url)
        }
    }

    ComposeWebView(state = state)
}
```

#### DisposableEffect

For cleanup:

```kotlin
@Composable
fun WebViewWithListeners() {
    val state = rememberWebViewState()

    DisposableEffect(Unit) {
        val listener = createListener()
        registerListener(listener)

        onDispose {
            unregisterListener(listener)
        }
    }

    ComposeWebView(state = state)
}
```

## JSBridge Pattern

### Type-Safe Bridge

```kotlin
// Define message types
@Serializable
data class UserData(val id: String, val name: String)

@Serializable
data class Response(val success: Boolean)

// Register handler
bridge.register<UserData, Response>("getUserData") { input ->
    Response(success = true)
}
```

### JavaScript Interface

```javascript
// In web page
window.AppBridge.call("getUserData", {
    id: "123",
    name: "John"
}).then(response => {
    console.log("Success:", response.success);
});
```

### Event Emission

```kotlin
// Kotlin side
bridge.emit("userLoggedIn", UserData(id = "123", name = "John"))
```

```javascript
// JavaScript side
window.AppBridge.on("userLoggedIn", (data) => {
    console.log("User logged in:", data.name);
});
```

## Error Handling Pattern

### Error Representation

```kotlin
sealed class WebViewError {
    data class HttpError(
        val code: Int,
        val description: String,
        val failingUrl: String?
    ) : WebViewError()

    data class SslError(
        val message: String
    ) : WebViewError()

    data class Unknown(
        val message: String
    ) : WebViewError()
}
```

### Error Handling

```kotlin
@Composable
fun WebViewWithErrorHandling() {
    val state = rememberWebViewState()

    Column {
        // Show errors
        state.errorsForCurrentRequest.forEach { error ->
            ErrorBanner(error)
        }

        ComposeWebView(state = state, modifier = Modifier.weight(1f))
    }
}
```

## Platform Abstraction Pattern

### Expect/Actual

**Step 1: Common interface**
```kotlin
// commonMain
expect class PlatformWebViewClient {
    fun onPageStarted(url: String)
    fun onPageFinished(url: String)
}
```

**Step 2: Platform implementation**
```kotlin
// androidMain
actual class PlatformWebViewClient actual constructor() {
    actual fun onPageStarted(url: String) {
        // Android-specific implementation
    }

    actual fun onPageFinished(url: String) {
        // Android-specific implementation
    }
}
```

### Platform-Specific Configuration

```kotlin
@Composable
expect fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier,
    onCreated: ((PlatformWebView) -> Unit)?
)
```

Usage:
```kotlin
ComposeWebView(
    state = state,
    onCreated = { webView ->
        // Platform-specific configuration
        when {
            isAndroid -> (webView as AndroidWebView).settings.apply {
                javaScriptEnabled = true
            }
            isIOS -> (webView as WKWebView).configuration.apply {
                // iOS-specific config
            }
        }
    }
)
```

## Content Loading Pattern

### WebContent Sealed Class

```kotlin
sealed class WebContent {
    data class Url(val url: String) : WebContent()
    data class Data(val data: String, val mimeType: String) : WebContent()
    data class Post(
        val url: String,
        val postData: ByteArray
    ) : WebContent()
}
```

### Loading Different Content

```kotlin
@Composable
fun WebViewWithContent(content: WebContent) {
    val state = rememberWebViewState()
    val controller = rememberWebViewController(state)

    LaunchedEffect(content) {
        when (content) {
            is WebContent.Url -> controller.loadUrl(content.url)
            is WebContent.Data -> controller.loadData(content.data, content.mimeType)
            is WebContent.Post -> controller.postUrl(content.url, content.postData)
        }
    }

    ComposeWebView(state = state)
}
```

## Lifecycle Integration Pattern

### Android Lifecycle

```kotlin
@Composable
fun WebViewWithLifecycle() {
    val state = rememberWebViewState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> state.onResume()
                Lifecycle.Event.ON_PAUSE -> state.onPause()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ComposeWebView(state = state)
}
```

## Cookie Management Pattern

```kotlin
expect object PlatformCookieManager {
    fun setCookie(url: String, cookie: String)
    fun getCookie(url: String): String?
    fun removeAllCookies(callback: (Boolean) -> Unit)
}
```

Usage:
```kotlin
fun setupCookies(url: String) {
    PlatformCookieManager.setCookie(
        url = url,
        cookie = "session=abc123; path=/; secure"
    )
}
```

## Download Handling Pattern

```kotlin
data class DownloadRequest(
    val url: String,
    val filename: String?,
    val mimeType: String?
)

// In WebView setup
ComposeWebView(
    state = state,
    onDownloadRequested = { request ->
        // Handle download
        downloadManager.download(request)
    }
)
```

## Testing Patterns

### State Testing

```kotlin
@Test
fun `state updates when URL loads`() {
    val state = WebViewState()

    state.loadingState = LoadingState.Loading
    assertEquals(LoadingState.Loading, state.loadingState)

    state.lastLoadedUrl = "https://example.com"
    assertEquals("https://example.com", state.lastLoadedUrl)
}
```

### Controller Testing

```kotlin
@Test
fun `controller loads URL in WebView`() {
    val state = WebViewState()
    val controller = TestWebViewController(state)

    controller.loadUrl("https://example.com")

    assertEquals("https://example.com", state.lastLoadedUrl)
}
```

## Common Anti-Patterns

### ❌ Direct Platform Access

```kotlin
// Don't expose platform WebView
@Composable
fun MyWebView(): AndroidWebView {
    return WebView(LocalContext.current) // Wrong!
}
```

### ❌ State in Multiple Places

```kotlin
// Don't duplicate state
var url by mutableStateOf("")  // Duplicates state.lastLoadedUrl
val state = rememberWebViewState(url)
```

### ❌ Ignoring Platform Differences

```kotlin
// Don't assume all platforms behave the same
fun setZoom(level: Float) {
    // iOS doesn't support arbitrary zoom levels!
}
```

### ❌ Blocking Operations

```kotlin
// Don't block the UI thread
fun loadUrlBlocking(url: String) {
    runBlocking {  // Wrong!
        heavyOperation()
    }
}
```

## Best Practices Summary

✅ **Do**:
- Use WebViewState for all reactive state
- Separate actions into WebViewController
- Use expect/actual for platform specifics
- Hoist state to enable composition
- Handle errors gracefully
- Clean up resources in DisposableEffect
- Document platform differences

❌ **Don't**:
- Expose platform-specific types publicly
- Duplicate state
- Ignore platform constraints
- Block the main thread
- Leak resources (listeners, etc.)

---

*These patterns ensure consistency, maintainability, and multiplatform compatibility.*

Last updated: 2025-12-28
