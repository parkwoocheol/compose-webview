# Error Handling

Handling network errors or page load failures gracefully is essential for a robust app. `compose-webview` provides mechanisms to observe errors and display custom UI.

---

## Platform Support

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| Typed Error Handling | ✅ Full | ✅ Full | ⚠️ Partial | ⚠️ Limited | Structured error information |
| `onReceivedError` Callback | ✅ | ✅ | ⚠️ | ⚠️ | Platform-specific error objects |
| `errorContent` UI | ✅ | ✅ | ✅ | ✅ | Custom error display |
| HTTP Error Detection | ✅ | ✅ | ⚠️ | ❌ | Via `onReceivedHttpError` |

---

## WebViewError Data Class

All errors are wrapped in a typed `WebViewError` data class for consistent handling across platforms:

```kotlin
data class WebViewError(
    val errorCode: Int,           // Platform-specific error code
    val description: String,      // Human-readable description
    val failingUrl: String? = null // The URL that failed to load
)
```

**Platform-Specific Error Codes:**

- **Android**: Maps to `WebViewClient` error constants (e.g., `ERROR_HOST_LOOKUP`, `ERROR_CONNECT`, `ERROR_TIMEOUT`)
- **iOS**: Maps to `NSURLError` codes (e.g., `NSURLErrorNotConnectedToInternet`, `NSURLErrorTimedOut`)
- **Desktop/Web**: Limited error information available

---

## Observing Errors

You can listen for errors via the `onReceivedError` callback. This corresponds to the standard `WebViewClient.onReceivedError`.

```kotlin
ComposeWebView(
    url = "https://example.com",
    onReceivedError = { webView, request, error ->
        // request: WebResourceRequest? (platform-specific)
        // error: WebResourceError? (platform-specific)

        Log.e("WebView", "Failed to load ${request?.url}: ${error?.description}")
    }
)
```

**Accessing Typed Errors:**

```kotlin
val state = rememberWebViewState(url = "https://example.com")

// Access errors from state
LaunchedEffect(state.errorsForCurrentRequest) {
    state.errorsForCurrentRequest.forEach { error ->
        println("Error ${error.errorCode}: ${error.description}")
        println("Failed URL: ${error.failingUrl}")
    }
}
```

---

## Displaying Custom Error UI

Instead of showing the default (and often ugly) browser error page, you can replace the WebView content with your own Composable when an error occurs.

Use the `errorContent` parameter.

```kotlin
ComposeWebView(
    url = "https://example.com",
    errorContent = { errors ->
        // errors is a List<WebViewError>
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Page Loading Failed",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Text(
                    text = errors.firstOrNull()?.description ?: "Unknown Error",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = { controller.reload() }) {
                    Text("Retry")
                }
            }
        }
    }
)
```

!!! tip "State Access"
    The `errors` list passed to `errorContent` is also available via `state.errorsForCurrentRequest`.

---

## Common Error Scenarios

### 1. Network Disconnected

You might want to check for network connectivity before even trying to load the URL, but if the connection drops during load, `ERROR_HOST_LOOKUP` or `ERROR_CONNECT` will occur.

### 2. HTTP Errors (4xx, 5xx)

Standard `WebView` does **not** treat HTTP errors (like 404 or 500) as "Resource Errors" in `onReceivedError`. Instead, they are valid responses.

To handle these, you would typically need to inspect `onReceivedHttpError` in a custom `WebViewClient`.

```kotlin
client = remember {
    object : ComposeWebViewClient() {
        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            // Handle HTTP error (e.g., show a toast or tracking)
            Log.e("WebView", "HTTP Error: ${errorResponse?.statusCode}")
        }
    }
}
```
