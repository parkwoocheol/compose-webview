# Types API

Reference documentation for the core classes used in the library.

---

## `WebViewState`

Holds the reactive state of the WebView.

### Properties

| Name | Type | Description | Platform Support |
| :--- | :--- | :--- | :--- |
| `lastLoadedUrl` | `String?` | The most recently loaded URL. | All platforms |
| `content` | `WebContent` | Sealed class representing the content (Url, Data, or Post). | All platforms |
| `isLoading` | `Boolean` | Whether the WebView is currently loading a page. | All platforms |
| `loadingState` | `LoadingState` | Detailed loading progress state (Initializing, Loading, Finished, Failed, Cancelled). | All platforms* |
| `pageTitle` | `String?` | The current page title. | All platforms |
| `pageIcon` | `PlatformBitmap?` | The current page favicon. | Android, iOS, Desktop |
| `scrollPosition` | `ScrollPosition` | Current scroll position (x, y) in pixels. | Android (real-time), iOS (100ms polling), Web (CORS-limited) |
| `errorsForCurrentRequest` | `List<WebViewError>` | Errors that occurred during the current page load. | Android, iOS, Desktop (partial), Web (limited) |
| `jsDialogState` | `JsDialogState?` | Active JavaScript dialog (Alert, Confirm, Prompt). | Android, iOS |
| `customViewState` | `CustomViewState?` | Custom view state (e.g., fullscreen video). | Android, iOS (native fullscreen trigger) |

\* **LoadingState progress**: Android (real-time), iOS (100ms polling), Desktop/Web (limited)

---

## `WebViewController`

Controls the navigation and execution of the WebView.

### Properties

| Name | Type | Description | Platform Support |
| :--- | :--- | :--- | :--- |
| `canGoBack` | `Boolean` | Whether the WebView can navigate back. | All platforms |
| `canGoForward` | `Boolean` | Whether the WebView can navigate forward. | All platforms |

### Core Navigation Methods

| Method | Platform Support | Notes |
| :--- | :--- | :--- |
| `loadUrl(url: String, headers: Map<String, String> = emptyMap())` | All platforms | Headers: Android/iOS only |
| `loadHtml(html: String, baseUrl: String? = null, ...)` | All platforms | Full data loading control |
| `postUrl(url: String, postData: ByteArray)` | Android, iOS | HTTP POST request |
| `navigateBack()` | All platforms | Check `canGoBack` first |
| `navigateForward()` | All platforms | Check `canGoForward` first |
| `reload()` | All platforms | Reload current page |
| `stopLoading()` | All platforms | Stop page load |

### JavaScript Execution

| Method | Platform Support | Notes |
| :--- | :--- | :--- |
| `evaluateJavascript(script: String, callback: ((String) -> Unit)? = null)` | All platforms | Execute JS and receive result |

### Zoom Control

| Method | Platform Support | Notes |
| :--- | :--- | :--- |
| `zoomIn(): Boolean` | Android, Desktop | iOS: Not supported (pinch-to-zoom only) |
| `zoomOut(): Boolean` | Android, Desktop | iOS: Not supported (pinch-to-zoom only) |
| `zoomBy(factor: Float)` | Android, Desktop | iOS: Not supported |

### Text Search

| Method | Platform Support | Notes |
| :--- | :--- | :--- |
| `findAllAsync(find: String)` | Android | Highlight all matches |
| `findNext(forward: Boolean)` | Android | Navigate through matches |
| `clearMatches()` | Android | Clear search highlights |

### Scroll Control

| Method | Platform Support | Notes |
| :--- | :--- | :--- |
| `scrollTo(x: Int, y: Int)` | Android, iOS, Desktop | Absolute scroll position |
| `scrollBy(x: Int, y: Int)` | Android, iOS, Desktop | Relative scroll offset |
| `pageUp(top: Boolean)` | Android | Scroll up one page |
| `pageDown(bottom: Boolean)` | Android | Scroll down one page |

### Cache & History Management

| Method | Platform Support | Notes |
| :--- | :--- | :--- |
| `clearCache(includeDiskFiles: Boolean)` | Android, iOS, Desktop | Clear WebView cache |
| `clearHistory()` | Android, iOS, Desktop | Clear navigation history |
| `clearSslPreferences()` | Android | Clear SSL certificate decisions |
| `clearFormData()` | Android | Clear form autofill data |

### File Operations

| Method | Platform Support | Notes |
| :--- | :--- | :--- |
| `saveWebArchive(filename: String)` | Android | Save page as .mht file |

---

## `WebViewJsBridge`

Helper class for managing the connection between Kotlin and JavaScript.

### Methods

| Method | Description | Platform Support |
| :--- | :--- | :--- |
| `register<T, R>(name: String, handler: (T) -> R)` | Registers a handler that receives type `T` from JS and returns type `R`. | All platforms |
| `emit(eventName: String, data: Any)` | Emits an event to JavaScript. The data object is automatically serialized to JSON. | All platforms |
| `call<T>(functionName: String, args: Any, callback: (T) -> Unit)` | Calls a JavaScript function and receives the result. | All platforms |

---

## `WebViewSettings`

Configuration settings for WebView behavior across all platforms.

### Properties

| Setting | Type | Default | Platform Support | Notes |
| :--- | :--- | :--- | :--- | :--- |
| `userAgent` | `String?` | `null` | Android, iOS, Desktop | Custom user agent string |
| `javaScriptEnabled` | `Boolean` | `true` | Android, iOS*, Desktop | *iOS: Always enabled |
| `domStorageEnabled` | `Boolean` | `true` | Android, iOS, Desktop (partial) | localStorage/sessionStorage |
| `cacheMode` | `CacheMode` | `DEFAULT` | Android, iOS (partial), Desktop (partial) | Cache behavior control |
| `allowFileAccess` | `Boolean` | `false` | Android, iOS (partial), Desktop (partial) | Access to file:// URLs |
| `allowContentAccess` | `Boolean` | `false` | Android | Access to content:// URLs |
| `supportZoom` | `Boolean` | `true` | Android, iOS**, Desktop | **iOS: Pinch-to-zoom only |
| `loadWithOverviewMode` | `Boolean` | `true` | Android | Load page with overview mode |
| `useWideViewPort` | `Boolean` | `true` | Android, iOS (partial), Desktop (partial) | Viewport meta tag support |
| `allowFileAccessFromFileURLs` | `Boolean` | `false` | Android, iOS (partial), Desktop (partial) | Security setting |
| `allowUniversalAccessFromFileURLs` | `Boolean` | `false` | Android, iOS (partial), Desktop (partial) | Security setting |
| `mediaPlaybackRequiresUserAction` | `Boolean` | `false` | Android, iOS, Desktop (partial) | Autoplay control |

### CacheMode Enum

| Value | Description | Platform Support |
| :--- | :--- | :--- |
| `DEFAULT` | Use cache when available | All platforms |
| `CACHE_ELSE_NETWORK` | Prefer cache, fallback to network | Android, iOS (partial), Desktop (partial) |
| `NO_CACHE` | Always load from network | Android, iOS (partial), Desktop (partial) |
| `CACHE_ONLY` | Only use cache, don't fetch | Android, iOS (partial), Desktop (partial) |

---

## `ScrollPosition`

Represents the current scroll position of the WebView.

```kotlin
@Immutable
data class ScrollPosition(
    val x: Int = 0,  // Horizontal scroll position in pixels
    val y: Int = 0   // Vertical scroll position in pixels
)
```

**Platform Support:**

- **Android**: Real-time updates via `setOnScrollChangeListener`
- **iOS**: 100ms polling of `scrollView.contentOffset`
- **Desktop**: Not supported (KCEF limitations)
- **Web**: CORS-limited (same-origin iframes only)

---

## `ConsoleMessage`

Represents a JavaScript console message from the WebView.

```kotlin
data class ConsoleMessage(
    val message: String,           // Console message text
    val sourceId: String = "",     // Source file URL
    val lineNumber: Int = 0,       // Line number in source
    val level: ConsoleMessageLevel // Severity level
)
```

### ConsoleMessageLevel Enum

| Level | Description |
| :--- | :--- |
| `LOG` | Standard console.log() |
| `DEBUG` | console.debug() |
| `WARNING` | console.warn() |
| `ERROR` | console.error() |
| `TIP` | Browser tip/info |

**Platform Support:** Android, iOS

---

## `WebViewError`

Represents an error that occurred during page loading.

```kotlin
data class WebViewError(
    val errorCode: Int,           // Platform-specific error code
    val description: String,      // Human-readable description
    val failingUrl: String? = null // The URL that failed to load
)
```

**Platform-Specific Error Codes:**

- **Android**: Maps to `WebViewClient` error constants (e.g., `ERROR_HOST_LOOKUP`, `ERROR_CONNECT`)
- **iOS**: Maps to `NSURLError` codes (e.g., `NSURLErrorNotConnectedToInternet`)
- **Desktop/Web**: Limited error information

---

## `LoadingState`

Sealed class representing the loading state of the WebView.

| State | Description | Platform Support |
| :--- | :--- | :--- |
| `Initializing` | WebView is being initialized | All platforms |
| `Loading(progress: Float)` | Page is loading (0.0 to 1.0) | Android (real-time), iOS (100ms polling), Desktop/Web (limited) |
| `Finished` | Page finished loading | All platforms |
| `Failed(error: WebViewError)` | Page load failed | Android, iOS, Desktop (partial), Web (limited) |
| `Cancelled` | Load was cancelled | All platforms |

---

## `JsDialogState`

Sealed class representing JavaScript dialog state (alert, confirm, prompt).

### Variants

```kotlin
sealed class JsDialogState {
    data class Alert(
        val message: String,
        val callback: () -> Unit
    ) : JsDialogState()

    data class Confirm(
        val message: String,
        val result: (Boolean) -> Unit
    ) : JsDialogState()

    data class Prompt(
        val message: String,
        val defaultValue: String,
        val result: (String?) -> Unit
    ) : JsDialogState()
}
```

**Platform Support:** Android, iOS
