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

| Property | Type | Description |
| :--- | :--- | :--- |
| `lastLoadedUrl` | `String?` | The URL currently loaded in the WebView. |
| `isLoading` | `Boolean` | `true` if a page is currently loading. |
| `loadingState` | `LoadingState` | Detailed loading progress (Initial, Loading(0.0-1.0), Finished). |
| `pageTitle` | `String?` | The title of the current page. |
| `pageIcon` | `Bitmap?` | The favicon of the current page. |
| `errorsForCurrentRequest` | `List<WebViewError>` | List of errors encountered during the current load. |

### Example: Observing Progress

```kotlin
val state = rememberSaveableWebViewState(url = "https://example.com")

if (state.isLoading) {
    val progress = (state.loadingState as? LoadingState.Loading)?.progress ?: 0f
    LinearProgressIndicator(progress = progress)
}
```
