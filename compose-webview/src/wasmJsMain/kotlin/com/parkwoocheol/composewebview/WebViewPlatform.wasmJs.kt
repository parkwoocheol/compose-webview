package com.parkwoocheol.composewebview

// Wasm doesn't support DOM directly in the same way as JS (yet) without interop, 
// but for now we use placeholders similar to JS to satisfy the compiler.
// We might need to use `kotlinx.browser` if available for Wasm, or just stubs.

// actual typealias WebView = Any // Placeholder for Wasm
actual class WebView

actual typealias PlatformBitmap = Any
actual typealias PlatformBundle = Any
actual fun createPlatformBundle(): PlatformBundle = Any()

// actual typealias PlatformCustomView = Any
actual class PlatformCustomView

actual class PlatformCustomViewCallback {
    actual fun onCustomViewHidden() {
        // No-op
    }
}

actual class PlatformWebResourceError
actual class PlatformWebResourceRequest {
    actual val url: String = ""
    actual val method: String = "GET"
    actual val headers: Map<String, String> = emptyMap()
    actual val isForMainFrame: Boolean = true
}

actual fun WebView.platformSaveState(bundle: PlatformBundle): Any? = null
actual fun WebView.platformRestoreState(bundle: PlatformBundle): Any? = null

actual fun WebView.platformGoBack() {}
actual fun WebView.platformGoForward() {}
actual fun WebView.platformReload() {}
actual fun WebView.platformStopLoading() {}
actual fun WebView.platformLoadUrl(url: String, additionalHttpHeaders: Map<String, String>) {}
actual fun WebView.platformLoadDataWithBaseURL(
    baseUrl: String?,
    data: String,
    mimeType: String?,
    encoding: String?,
    historyUrl: String?
) {}
actual fun WebView.platformPostUrl(url: String, postData: ByteArray) {}
actual fun WebView.platformEvaluateJavascript(script: String, callback: ((String) -> Unit)?) {}
actual fun WebView.platformZoomBy(zoomFactor: Float) {}
actual fun WebView.platformZoomIn(): Boolean = false
actual fun WebView.platformZoomOut(): Boolean = false
actual fun WebView.platformFindAllAsync(find: String) {}
actual fun WebView.platformFindNext(forward: Boolean) {}
actual fun WebView.platformClearMatches() {}
actual fun WebView.platformClearCache(includeDiskFiles: Boolean) {}
actual fun WebView.platformClearHistory() {}
actual fun WebView.platformClearSslPreferences() {}
actual fun WebView.platformClearFormData() {}
actual fun WebView.platformPageUp(top: Boolean): Boolean = false
actual fun WebView.platformPageDown(bottom: Boolean): Boolean = false
actual fun WebView.platformScrollTo(x: Int, y: Int) {}
actual fun WebView.platformScrollBy(x: Int, y: Int) {}
actual fun WebView.platformSaveWebArchive(filename: String) {}

actual fun WebView.platformAddJavascriptInterface(obj: Any, name: String) {}

@Target(AnnotationTarget.FUNCTION)
actual annotation class PlatformJavascriptInterface actual constructor()

actual abstract class PlatformContext

actual typealias ComposeWebViewClient = com.parkwoocheol.composewebview.client.ComposeWebViewClient
actual typealias ComposeWebChromeClient = com.parkwoocheol.composewebview.client.ComposeWebChromeClient
