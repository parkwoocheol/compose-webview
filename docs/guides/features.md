# Advanced Features

This guide covers advanced capabilities of `compose-webview` including WebView Configuration, State Management, JavaScript Debugging, File Uploads, Downloads, and Custom Views (Fullscreen Video).

---

## Platform Support Matrix

| Feature | Android | iOS | Desktop | Web |
|---------|:-------:|:---:|:-------:|:---:|
| WebViewSettings Configuration | ✅ | ⚠️ | ⚠️ | ❌ |
| Console Message Debugging | ✅ | ✅ | ❌ | ❌ |
| Scroll Position Tracking | ✅ | ✅ | ❌ | ⚠️ |
| Loading State with Progress | ✅ | ✅ | ⚠️ | ⚠️ |
| Typed Error Handling | ✅ | ✅ | ⚠️ | ⚠️ |
| File Uploads | ✅ | ✅ | ❌ | ❌ |
| Downloads | ✅ | ⚠️ | ❌ | ❌ |
| Custom View (Fullscreen) | ✅ | ❌ | ❌ | ❌ |
| JS Dialogs (Alert/Confirm/Prompt) | ✅ | ✅ | ❌ | ❌ |

**Legend**: ✅ Full Support | ⚠️ Partial/Limited | ❌ Not Supported

---

## WebView Configuration (WebViewSettings)

Configure WebView behavior with a unified API across platforms.

### Basic Usage

```kotlin
val settings = WebViewSettings(
    userAgent = "MyApp/1.0",
    javaScriptEnabled = true,
    domStorageEnabled = true,
    cacheMode = CacheMode.CACHE_ELSE_NETWORK,
    supportZoom = true,
    mediaPlaybackRequiresUserAction = false
)

ComposeWebView(
    state = webViewState,
    settings = settings
)
```

### Available Settings

| Setting | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| `userAgent` | ✅ | ✅ | ✅ | ❌ | Custom user agent string |
| `javaScriptEnabled` | ✅ | ✅* | ✅ | ❌ | *iOS: Always enabled |
| `domStorageEnabled` | ✅ | ✅ | ⚠️ | ❌ | localStorage/sessionStorage |
| `cacheMode` | ✅ | ⚠️ | ⚠️ | ❌ | Cache behavior control |
| `supportZoom` | ✅ | ⚠️** | ✅ | ❌ | **iOS: Pinch-to-zoom only |
| `mediaPlaybackRequiresUserAction` | ✅ | ✅ | ⚠️ | ❌ | Autoplay control |

!!! info "Platform-Specific Behavior"
    - **iOS**: JavaScript is always enabled and cannot be disabled. The `javaScriptEnabled` setting is ignored.
    - **iOS**: Programmatic zoom (`zoomIn()`/`zoomOut()`) is not supported. Only user pinch-to-zoom gestures work.
    - **Web**: Settings are browser-controlled and cannot be modified from the iframe.

### Cache Modes

```kotlin
enum class CacheMode {
    DEFAULT,              // Use cache when available
    CACHE_ELSE_NETWORK,  // Prefer cache, fallback to network
    NO_CACHE,            // Always load from network
    CACHE_ONLY           // Only use cache, don't fetch
}
```

---

## Console Message Debugging

Capture and debug JavaScript console messages from your WebView.

### Platform Support

| Platform | Status | Implementation |
|----------|--------|----------------|
| **Android** | ✅ Full | `WebChromeClient.onConsoleMessage` |
| **iOS** | ✅ Full | Custom message handler |
| **Desktop/Web** | ❌ Not supported | - |

### Usage

```kotlin
@Composable
fun DebuggableWebView() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    
    // Create handling chrome client
    val chromeClient = rememberWebChromeClient {
        onConsoleMessage { webView, message ->
            when (message.level) {
                ConsoleMessageLevel.ERROR -> {
                    Log.e("WebView", "[${message.sourceId}:${message.lineNumber}] ${message.message}")
                }
                ConsoleMessageLevel.WARNING -> {
                    Log.w("WebView", message.message)
                }
                ConsoleMessageLevel.LOG -> {
                    Log.d("WebView", message.message)
                }
                else -> {
                    Log.v("WebView", message.message)
                }
            }
            false // Return true to suppress default logging
        }
    }

    ComposeWebView(
        state = state,
        chromeClient = chromeClient
    )
}
```

### ConsoleMessage Data Class

```kotlin
data class ConsoleMessage(
    val message: String,           // Console message text
    val sourceId: String = "",     // Source file URL
    val lineNumber: Int = 0,       // Line number in source
    val level: ConsoleMessageLevel // Severity level
)

enum class ConsoleMessageLevel {
    LOG, DEBUG, WARNING, ERROR, TIP
}
```

---

## Scroll Position Tracking

Track the WebView's scroll position in real-time.

### Platform Support

| Platform | Support | Implementation | Update Frequency |
|----------|---------|----------------|------------------|
| **Android** | ✅ Full | `setOnScrollChangeListener` | Real-time |
| **iOS** | ✅ Full | `contentOffset` polling | 100ms intervals |
| **Desktop** | ❌ Not supported | CEF limitations | - |
| **Web** | ⚠️ Limited | `onscroll` event | CORS restricted (same-origin only) |

### Usage

```kotlin
val webViewState = rememberWebViewState(url = "https://example.com")

// Access scroll position from state
LaunchedEffect(webViewState.scrollPosition) {
    val (x, y) = webViewState.scrollPosition
    println("Scrolled to: x=$x, y=$y")
}

ComposeWebView(state = webViewState)
```

### ScrollPosition Data Class

```kotlin
@Immutable
data class ScrollPosition(
    val x: Int = 0,  // Horizontal scroll position in pixels
    val y: Int = 0   // Vertical scroll position in pixels
)
```

!!! warning "Platform Limitations"
    - **iOS**: Uses polling (100ms intervals) instead of real-time updates due to Kotlin/Native KVO complexity.
    - **Web**: Only works for same-origin iframes. Cross-origin iframes will always report (0, 0) due to CORS restrictions.
    - **Desktop**: Not supported due to KCEF API limitations.

---

## File Uploads

Uploading files (e.g., via `<input type="file">`) is often a hassle to implement in Android WebViews because it requires handling `WebChromeClient.onShowFileChooser`.

### Platform Support

| Platform | Status | Notes |
|----------|--------|-------|
| **Android** | ✅ Automatic | Uses `onShowFileChooser` internally |
| **iOS** | ✅ Native | WKWebView handles file uploads by default |
| **Desktop/Web** | ❌ Not supported | |

### How it works (Android)

The library internally uses `rememberLauncherForActivityResult` to launch the Android File Picker intent when the WebView requests a file. When the user selects a file, the result is automatically passed back to the WebView.

!!! check "No Extra Code"
    You do NOT need to implement `onShowFileChooser` or handle Activity results manually. It works out-of-the-box.

### How it works (iOS)

iOS's `WKWebView` **natively supports file uploads** without any additional configuration. The system automatically presents a file picker when a web page requests file input.

!!! note "iOS 18.4+ Custom Implementation"
    Starting from iOS 18.4, you can optionally implement `WKUIDelegate.runOpenPanelWith` for custom file picker UI. However, this is not required for basic file upload functionality.

### Permissions

Standard Android file picking usually does not require runtime permissions on modern Android versions (API 21+). However, if your web page requests camera access (e.g., `<input type="file" capture>`), ensure you have declared and requested `CAMERA` permission in your app.

---

## Downloads

By default, `WebView` does not handle file downloads. You need to provide a callback to intercept download requests.

### Handling Downloads

Use the `onDownloadStart` parameter to receive download events.

```kotlin
ComposeWebView(
    url = "https://example.com",
    onDownloadStart = { url, userAgent, contentDisposition, mimeType, contentLength ->
        // Trigger download
        downloadFile(url, contentDisposition, mimeType)
    }
)
```

### Example Implementation

You can use Android's `DownloadManager` to handle the actual download.

```kotlin
fun downloadFile(context: Context, url: String, contentDisposition: String?, mimeType: String?) {
    val request = DownloadManager.Request(Uri.parse(url))
    request.setMimeType(mimeType)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    
    // Guess filename
    val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}
```

---

## Custom Views (Fullscreen Video)

To support fullscreen video (e.g., YouTube's fullscreen button), you need to handle "Custom Views".

### 1. Provide Custom View Content

Use the `customViewContent` parameter. This lambda is only called when a video requests fullscreen.

```kotlin
ComposeWebView(
    // ...
    customViewContent = { customViewState ->
        // customViewState.customView is the video implementation provided by WebView
        if (customViewState.customView != null) {
            AndroidView(
                factory = { customViewState.customView!! },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black) // Background for fullscreen
            )
        }
    }
)
```

### 2. Android Manifest Configuration

For fullscreen video to work smoothly (and to allow orientation changes), your Activity in `AndroidManifest.xml` should handle configuration changes manually.

```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenSize|keyboardHidden|smallestScreenSize|screenLayout"
    android:hardwareAccelerated="true"> <!-- Required for video -->
```
