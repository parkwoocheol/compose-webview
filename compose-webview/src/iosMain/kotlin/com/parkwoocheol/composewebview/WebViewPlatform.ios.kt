package com.parkwoocheol.composewebview

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.WebKit.WKWebView
import platform.Foundation.NSError
import platform.Foundation.*
import platform.darwin.NSObject

actual typealias WebView = WKWebView
actual class PlatformWebResourceError(val impl: NSError)
actual class PlatformWebResourceRequest(val impl: NSURLRequest, val isMainFrame: Boolean) {
    actual val url: String get() = impl.URL?.absoluteString ?: ""
    actual val method: String get() = impl.HTTPMethod ?: "GET"
    actual val headers: Map<String, String> get() = (impl.allHTTPHeaderFields as? Map<String, String>) ?: emptyMap()
    actual val isForMainFrame: Boolean get() = isMainFrame
}
actual typealias PlatformBitmap = UIImage

actual class PlatformBundle(val map: Map<String, Any?>)
actual fun createPlatformBundle(): PlatformBundle = PlatformBundle(emptyMap())

actual typealias PlatformCustomView = UIView

actual class PlatformCustomViewCallback {
    actual fun onCustomViewHidden() {
        // No-op
    }
}

actual abstract class PlatformContext


actual fun WebView.platformSaveState(bundle: PlatformBundle): Any? {
    // iOS WKWebView does not have a direct equivalent to Android's saveState
    return null
}

actual fun WebView.platformRestoreState(bundle: PlatformBundle): Any? {
    // iOS WKWebView does not have a direct equivalent to Android's restoreState
    return null
}

actual fun WebView.platformGoBack() {
    if (canGoBack) goBack()
}

actual fun WebView.platformGoForward() {
    if (canGoForward) goForward()
}

actual fun WebView.platformReload() {
    reload()
}

actual fun WebView.platformStopLoading() {
    stopLoading()
}

actual fun WebView.platformLoadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
    val request = NSMutableURLRequest.requestWithURL(NSURL.URLWithString(url)!!) as NSMutableURLRequest
    additionalHttpHeaders.forEach { (key, value) ->
        request.setValue(value, forHTTPHeaderField = key)
    }
    loadRequest(request)
}

actual fun WebView.platformLoadDataWithBaseURL(
    baseUrl: String?,
    data: String,
    mimeType: String?,
    encoding: String?,
    historyUrl: String?
) {
    // mimeType and encoding are often inferred or can be set in other ways, but loadHTMLString is the main way
    loadHTMLString(data, baseURL = baseUrl?.let { NSURL.URLWithString(it) })
}

@OptIn(ExperimentalForeignApi::class)
actual fun WebView.platformPostUrl(url: String, postData: ByteArray) {
    val request = NSMutableURLRequest.requestWithURL(NSURL.URLWithString(url)!!) as NSMutableURLRequest
    request.setValue("POST", forKey = "HTTPMethod")
    
    val nsData = postData.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), postData.size.toULong())
    }
    request.setValue(nsData, forKey = "HTTPBody")
    loadRequest(request)
}

actual fun WebView.platformEvaluateJavascript(script: String, callback: ((String) -> Unit)?) {
    evaluateJavaScript(script) { result, error ->
        if (error == null && result != null) {
            callback?.invoke(result.toString())
        } else {
            callback?.invoke("null") // or handle error
        }
    }
}

actual fun WebView.platformZoomBy(zoomFactor: Float) {
    // Not directly supported
}

actual fun WebView.platformZoomIn(): Boolean {
    // Not directly supported
    return false
}

actual fun WebView.platformZoomOut(): Boolean {
    // Not directly supported
    return false
}

actual fun WebView.platformFindAllAsync(find: String) {
    // Not directly supported
}

actual fun WebView.platformFindNext(forward: Boolean) {
    // Not directly supported
}

actual fun WebView.platformClearMatches() {
    // Not directly supported
}

actual fun WebView.platformClearCache(includeDiskFiles: Boolean) {
    // Complex to implement on iOS, requires WebsiteDataStore
}

actual fun WebView.platformClearHistory() {
    // Not directly supported, maybe by recreating WebView
}

actual fun WebView.platformClearSslPreferences() {
    // Not supported
}

actual fun WebView.platformClearFormData() {
    // Not supported
}

actual fun WebView.platformPageUp(top: Boolean): Boolean {
    // Not supported
    return false
}

actual fun WebView.platformPageDown(bottom: Boolean): Boolean {
    // Not supported
    return false
}

@OptIn(ExperimentalForeignApi::class)
actual fun WebView.platformScrollTo(x: Int, y: Int) {
    scrollView.setContentOffset(platform.CoreGraphics.CGPointMake(x.toDouble(), y.toDouble()), animated = true)
}

@OptIn(ExperimentalForeignApi::class)
actual fun WebView.platformScrollBy(x: Int, y: Int) {
    val current = scrollView.contentOffset
    // Need to extract x and y from CGPoint, which is CStruct
    // Simplified:
    // scrollView.setContentOffset(...)
}

actual fun WebView.platformSaveWebArchive(filename: String) {
    // Not supported
}

actual fun WebView.platformAddJavascriptInterface(obj: Any, name: String) {
    // Requires WKUserContentController and WKScriptMessageHandler
    // This is complex to map 1:1 with Android's addJavascriptInterface
    // For now, we can leave it empty or implement a basic version if needed
}

@Target(AnnotationTarget.FUNCTION)
actual annotation class PlatformJavascriptInterface actual constructor()


actual open class ComposeWebViewClient actual constructor()
actual open class ComposeWebChromeClient actual constructor()
