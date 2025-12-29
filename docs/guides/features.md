# Advanced Features

This guide covers advanced capabilities of `compose-webview` such as File Uploads, Downloads, and Custom Views (Fullscreen Video).

---

## Platform Support Matrix

| Feature | Android | iOS | Desktop | Web |
|---------|:-------:|:---:|:-------:|:---:|
| File Uploads | ✅ | ✅ (native) | ❌ | ❌ |
| Downloads | ✅ | ⚠️ | ❌ | ❌ |
| Custom View (Fullscreen) | ✅ | ❌ | ❌ | ❌ |
| Progress Callback | ✅ | ✅ | ❌ | ❌ |

**Legend**: ✅ Supported | ⚠️ Partial | ❌ Not supported

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
