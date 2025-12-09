# Lifecycle Management

This guide explains how `compose-webview` manages the lifecycle of the underlying WebView instance and how it integrates with the Android lifecycle.

---

## Automatic Lifecycle Handling

Managing the lifecycle of a `WebView` is critical for performance and preventing memory leaks. Standard Android `WebView` requires manual calls to `onResume`, `onPause`, and `destroy`.

`compose-webview` handles all of this automatically within the Composable.

### 1. Resume & Pause

* **On Resume**: When your App or Composable comes to the foreground, `webView.onResume()` is called. This resumes JavaScript execution, timers, and any active layout processing.
* **On Pause**: When your App goes to the background or the Composable is hidden, `webView.onPause()` is called. This suspends JavaScript and reduces battery consumption.

### 2. Destruction (Dispose)

* **On Dispose**: When the `ComposeWebView` leaves the composition tree (e.g., navigating to another screen), `webView.destroy()` is called.
* **Cleanup**: DOM storage and other resources are cleaned up to prevent memory leaks.

!!! success "Zero Boilerplate"
    You do NOT need to manually handle lifecycle events in your Activity or Fragment.

---

## Manual Control

If you need to manually dispose of the WebView or clear its state before the Composable is removed, you can use the `onDispose` callback.

```kotlin
ComposeWebView(
    // ...
    onDispose = { webView ->
        // Additional cleanup if needed
        webView.clearCache(true)
        webView.clearHistory()
        
        // Note: webView.destroy() is called automatically after this block
    }
)
```
