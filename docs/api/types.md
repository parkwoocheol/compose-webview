# Types API

Reference documentation for the core classes used in the library.

---

## `WebViewState`

Holds the reactive state of the WebView.

### Properties

| Name | Type | Description |
| :--- | :--- | :--- |
| `lastLoadedUrl` | `String?` | The most recently loaded URL. |
| `content` | `WebContent` | Sealed class representing the content (Url, Data, or Post). |
| `isLoading` | `Boolean` | Whether the WebView is currently loading a page. |
| `loadingState` | `LoadingState` | Detailed loading progress state. |
| `pageTitle` | `String?` | The current page title. |
| `pageIcon` | `Bitmap?` | The current page favicon. |
| `errorsForCurrentRequest` | `List<WebViewError>` | Errors that occurred during the current page load. |

---

## `WebViewController`

Controls the navigation and execution of the WebView.

### Methods

#### `loadUrl(url: String, ...)`

Loads the given URL with optional headers.

#### `navigateBack()`

Goes back in history. Check `canGoBack` before calling.

#### `navigateForward()`

Goes forward in history. Check `canGoForward` before calling.

#### `reload()`

Reloads the current page.

#### `evaluateJavascript(script: String, callback: ...)`

Executes the given JavaScript string. The callback receives the result as a string.

---

## `WebViewJsBridge`

Helper class for managing the connection between Kotlin and JavaScript.

### Methods

#### `register<T, R>(name: String, handler: (T) -> R)`

Registers a handler that receives type `T` from JS and returns type `R`.

#### `emit(eventName: String, data: Any)`

Emits an event to JavaScript. The data object is automatically serialized to JSON.
