@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.parkwoocheol.composewebview

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest

actual typealias PlatformPermissionRequest = android.webkit.PermissionRequest

actual typealias WebView = android.webkit.WebView

actual typealias PlatformBitmap = android.graphics.Bitmap
actual typealias PlatformBundle = android.os.Bundle

actual fun createPlatformBundle(): PlatformBundle = android.os.Bundle()

actual typealias PlatformCustomView = android.view.View

actual class PlatformCustomViewCallback(val impl: android.webkit.WebChromeClient.CustomViewCallback) {
    actual fun onCustomViewHidden() {
        impl.onCustomViewHidden()
    }
}

actual class PlatformWebResourceError(val impl: WebResourceError) {
    actual val errorCode: Int get() = impl.errorCode
    actual val description: String get() = impl.description?.toString() ?: "Unknown error"
}

actual class PlatformWebResourceRequest(val impl: WebResourceRequest) {
    actual val url: String get() = impl.url.toString()
    actual val method: String get() = impl.method
    actual val headers: Map<String, String> get() = impl.requestHeaders
    actual val isForMainFrame: Boolean get() = impl.isForMainFrame
}

fun createPlatformWebResourceRequest(impl: WebResourceRequest) = PlatformWebResourceRequest(impl)

fun createPlatformWebResourceError(impl: WebResourceError) = PlatformWebResourceError(impl)

actual fun WebView.platformSaveState(bundle: PlatformBundle): Any? = this.saveState(bundle)

actual fun WebView.platformRestoreState(bundle: PlatformBundle): Any? = this.restoreState(bundle)

actual fun WebView.platformGoBack() = this.goBack()

actual fun WebView.platformGoForward() = this.goForward()

actual fun WebView.platformReload() = this.reload()

actual fun WebView.platformStopLoading() = this.stopLoading()

actual fun WebView.platformLoadUrl(
    url: String,
    additionalHttpHeaders: Map<String, String>,
) = this.loadUrl(url, additionalHttpHeaders)

actual fun WebView.platformLoadDataWithBaseURL(
    baseUrl: String?,
    data: String,
    mimeType: String?,
    encoding: String?,
    historyUrl: String?,
) = this.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)

actual fun WebView.platformPostUrl(
    url: String,
    postData: ByteArray,
) = this.postUrl(url, postData)

actual fun WebView.platformEvaluateJavascript(
    script: String,
    callback: ((String) -> Unit)?,
) = this.evaluateJavascript(script, callback)

actual fun WebView.platformZoomBy(zoomFactor: Float) = this.zoomBy(zoomFactor)

actual fun WebView.platformZoomIn(): Boolean = this.zoomIn()

actual fun WebView.platformZoomOut(): Boolean = this.zoomOut()

actual fun WebView.platformFindAllAsync(find: String) = this.findAllAsync(find)

actual fun WebView.platformFindNext(forward: Boolean) = this.findNext(forward)

actual fun WebView.platformClearMatches() = this.clearMatches()

actual fun WebView.platformClearCache(includeDiskFiles: Boolean) = this.clearCache(includeDiskFiles)

actual fun WebView.platformClearHistory() = this.clearHistory()

actual fun WebView.platformClearSslPreferences() = this.clearSslPreferences()

actual fun WebView.platformClearFormData() = this.clearFormData()

actual fun WebView.platformPageUp(top: Boolean): Boolean = this.pageUp(top)

actual fun WebView.platformPageDown(bottom: Boolean): Boolean = this.pageDown(bottom)

actual fun WebView.platformScrollTo(
    x: Int,
    y: Int,
) = this.scrollTo(x, y)

actual fun WebView.platformScrollBy(
    x: Int,
    y: Int,
) = this.scrollBy(x, y)

actual fun WebView.platformSaveWebArchive(filename: String) = this.saveWebArchive(filename)

@android.annotation.SuppressLint("JavascriptInterface")
actual fun WebView.platformAddJavascriptInterface(
    obj: Any,
    name: String,
) = this.addJavascriptInterface(obj, name)

actual typealias PlatformJavascriptInterface = android.webkit.JavascriptInterface

actual typealias PlatformContext = android.content.Context

actual var WebView.platformJavaScriptEnabled: Boolean
    get() = settings.javaScriptEnabled
    set(value) {
        settings.javaScriptEnabled = value
    }

actual var WebView.platformDomStorageEnabled: Boolean
    get() = settings.domStorageEnabled
    set(value) {
        settings.domStorageEnabled = value
    }

actual var WebView.platformSupportZoom: Boolean
    get() = settings.supportZoom()
    set(value) {
        settings.setSupportZoom(value)
    }

actual var WebView.platformBuiltInZoomControls: Boolean
    get() = settings.builtInZoomControls
    set(value) {
        settings.builtInZoomControls = value
    }

actual var WebView.platformDisplayZoomControls: Boolean
    get() = settings.displayZoomControls
    set(value) {
        settings.displayZoomControls = value
    }
