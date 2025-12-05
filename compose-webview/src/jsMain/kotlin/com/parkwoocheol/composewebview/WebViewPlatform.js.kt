package com.parkwoocheol.composewebview

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement

actual class WebView(val iframe: HTMLIFrameElement)

actual typealias PlatformBitmap = Any // Placeholder
actual typealias PlatformBundle = Any
actual fun createPlatformBundle(): PlatformBundle = Any()

// actual typealias PlatformCustomView = HTMLElement
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

actual fun WebView.platformGoBack() {
    // contentWindow?.history?.back()
}
actual fun WebView.platformGoForward() {
    // contentWindow?.history?.forward()
}
actual fun WebView.platformReload() {
    // contentWindow?.location?.reload()
}
actual fun WebView.platformStopLoading() {
    // contentWindow?.stop()
}
actual fun WebView.platformLoadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
    // src = url
}
actual fun WebView.platformLoadDataWithBaseURL(
    baseUrl: String?,
    data: String,
    mimeType: String?,
    encoding: String?,
    historyUrl: String?
) {
    // src = "data:${mimeType ?: "text/html"};charset=${encoding ?: "utf-8"},$data"
}
actual fun WebView.platformPostUrl(url: String, postData: ByteArray) {
    // Not directly supported in iframe without form submission
}
actual fun WebView.platformEvaluateJavascript(script: String, callback: ((String) -> Unit)?) {
    // Requires postMessage or direct access if same-origin
}
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
actual fun WebView.platformScrollTo(x: Int, y: Int) {
    // contentWindow?.scrollTo(x.toDouble(), y.toDouble())
}
actual fun WebView.platformScrollBy(x: Int, y: Int) {
    // contentWindow?.scrollBy(x.toDouble(), y.toDouble())
}
actual fun WebView.platformSaveWebArchive(filename: String) {}

actual fun WebView.platformAddJavascriptInterface(obj: Any, name: String) {}

@Target(AnnotationTarget.FUNCTION)
actual annotation class PlatformJavascriptInterface actual constructor()

actual abstract class PlatformContext

actual typealias ComposeWebViewClient = com.parkwoocheol.composewebview.client.ComposeWebViewClient
actual typealias ComposeWebChromeClient = com.parkwoocheol.composewebview.client.ComposeWebChromeClient
