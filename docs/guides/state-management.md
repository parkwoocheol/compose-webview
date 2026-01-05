# State Management

Managing the state of a WebView (URL, history, scroll position) is crucial for a smooth user experience, especially in a Compose environment where recompositions and configuration changes occur frequently.

`compose-webview` provides two distinct approaches to state management.

---

## 1. Persistent State (Recommended)

Use `rememberSaveableWebViewState` when you want the WebView to **survive configuration changes** (like screen rotation). This is the standard behavior users expect from a browser.

### Features

* **Persistence**: Automatically saves and restores state via `SavedStateHandle`.
* **Safety**: Handles lifecycle cleanup to prevent memory leaks.

### Usage

```kotlin
// Automatically saved across rotations
val state = rememberSaveableWebViewState(
    url = "https://google.com",
    additionalHttpHeaders = mapOf("X-Custom-Header" to "Value")
)

ComposeWebView(state = state)
```

### Loading HTML Data

To load raw HTML content with persistence:

```kotlin
val state = rememberSaveableWebViewStateWithData(
    data = "<html><body><h1>Hello World</h1></body></html>",
    baseUrl = "https://example.com" // Optional Base URL
)
```

---

## 2. Transient State (Lightweight)

Use `rememberWebViewState` when you **do not need persistence** or if you are loading **very large HTML data**.

### When to use?

* **Large Data**: Android's `Bundle` has a size limit (approx 1MB). Trying to save a large HTML string in `rememberSaveable` will crash the app with `TransactionTooLargeException`.
* **Temporary Views**: For simple "About" screens or ToS pages where resetting on rotation is acceptable.

### Features

* **Lightweight**: No serialization overhead.
* **Reset on Rotation**: The WebView will reload from scratch if the screen rotates.

### Usage

```kotlin
// Will reload if screen rotates
val state = rememberWebViewState(url = "https://google.com")

ComposeWebView(state = state)
```

---

## Accessing State Properties

The `WebViewState` object exposes reactive properties that you can observe in your Composables.

### Available Properties

| Property | Type | Description | Platform Support |
| :--- | :--- | :--- | :--- |
| `lastLoadedUrl` | `String?` | The URL currently loaded in the WebView. | All platforms |
| `isLoading` | `Boolean` | `true` if a page is currently loading. | All platforms |
| `loadingState` | `LoadingState` | Detailed loading progress (Initializing, Loading, Finished, Failed, Cancelled). | All platforms* |
| `pageTitle` | `String?` | The title of the current page. | All platforms |
| `pageIcon` | `PlatformBitmap?` | The favicon of the current page. | Android, iOS, Desktop |
| `scrollPosition` | `ScrollPosition` | Current scroll position (x, y) in pixels. | Android (real-time), iOS (100ms polling), Web (CORS-limited)** |
| `errorsForCurrentRequest` | `SnapshotStateList<WebViewError>` | List of errors encountered during the current load. | Android, iOS, Desktop (partial), Web (limited) |
| `jsDialogState` | `JsDialogState?` | Active JavaScript dialog (Alert, Confirm, Prompt). | Android, iOS |
| `customViewState` | `CustomViewState?` | Custom view state (e.g., fullscreen video). | Android |

\* **LoadingState progress**: Android (real-time), iOS (100ms polling), Desktop/Web (limited)
\*\* **Web scrollPosition**: Only works for same-origin iframes due to CORS restrictions

### Example: Observing Loading Progress

```kotlin
val state = rememberSaveableWebViewState(url = "https://example.com")

if (state.isLoading) {
    val progress = (state.loadingState as? LoadingState.Loading)?.progress ?: 0f
    LinearProgressIndicator(progress = progress)
}
```

### Example: Tracking Scroll Position

```kotlin
val state = rememberSaveableWebViewState(url = "https://example.com")

// Observe scroll position changes
LaunchedEffect(state.scrollPosition) {
    val (x, y) = state.scrollPosition
    println("User scrolled to: x=$x, y=$y")

    // Example: Show "Back to Top" button when scrolled down
    if (y > 500) {
        showBackToTopButton = true
    }
}

ComposeWebView(state = state)
```

**Platform-Specific Behavior:**

* **Android**: Scroll position updates in real-time via `setOnScrollChangeListener`
* **iOS**: Scroll position updates every 100ms via polling of `scrollView.contentOffset`
* **Desktop**: Not supported (KCEF API limitations)
* **Web**: Only works for same-origin iframes; cross-origin iframes always report (0, 0) due to CORS
