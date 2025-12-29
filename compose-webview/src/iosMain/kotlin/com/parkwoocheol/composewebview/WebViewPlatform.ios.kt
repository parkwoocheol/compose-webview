package com.parkwoocheol.composewebview

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.Foundation.HTTPMethod
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.allHTTPHeaderFields
import platform.Foundation.dataWithBytes
import platform.Foundation.setValue
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.darwin.NSObject

fun <T> T.runOnMainThread(block: T.() -> Unit) {
    if (platform.Foundation.NSThread.isMainThread) {
        block()
    } else {
        platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
            block()
        }
    }
}

actual abstract class PlatformPermissionRequest

actual typealias WebView = WKWebView

actual class PlatformWebResourceError(val impl: NSError) {
    actual val errorCode: Int get() = impl.code.toInt()
    actual val description: String get() = impl.localizedDescription ?: "Unknown error"
}

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

actual fun WebView.platformLoadUrl(
    url: String,
    additionalHttpHeaders: Map<String, String>,
) {
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
    historyUrl: String?,
) {
    // mimeType and encoding are often inferred or can be set in other ways, but loadHTMLString is the main way
    loadHTMLString(data, baseURL = baseUrl?.let { NSURL.URLWithString(it) })
}

@OptIn(ExperimentalForeignApi::class)
actual fun WebView.platformPostUrl(
    url: String,
    postData: ByteArray,
) {
    val request = NSMutableURLRequest.requestWithURL(NSURL.URLWithString(url)!!) as NSMutableURLRequest
    request.setValue("POST", forKey = "HTTPMethod")

    val nsData =
        postData.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), postData.size.toULong())
        }
    request.setValue(nsData, forKey = "HTTPBody")
    loadRequest(request)
}

actual fun WebView.platformEvaluateJavascript(
    script: String,
    callback: ((String) -> Unit)?,
) {
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
actual fun WebView.platformScrollTo(
    x: Int,
    y: Int,
) {
    scrollView.setContentOffset(platform.CoreGraphics.CGPointMake(x.toDouble(), y.toDouble()), animated = true)
}

@OptIn(ExperimentalForeignApi::class)
actual fun WebView.platformScrollBy(
    x: Int,
    y: Int,
) {
    val current = scrollView.contentOffset
    current.useContents {
        val newX = this.x + x.toDouble()
        val newY = this.y + y.toDouble()
        scrollView.setContentOffset(platform.CoreGraphics.CGPointMake(newX, newY), animated = true)
    }
}

actual fun WebView.platformSaveWebArchive(filename: String) {
    // Not supported
}

@OptIn(ExperimentalForeignApi::class)
actual fun WebView.platformAddJavascriptInterface(
    obj: Any,
    name: String,
) {
    // This is a simplified implementation.
    // In a real-world scenario, you'd need a more robust way to map Kotlin objects to JS.
    // For now, we'll just register a message handler.
    configuration.userContentController.addScriptMessageHandler(
        scriptMessageHandler =
            object : NSObject(), WKScriptMessageHandlerProtocol {
                override fun userContentController(
                    userContentController: WKUserContentController,
                    didReceiveScriptMessage: WKScriptMessage,
                ) {
                    // Handle message from JS: window.webkit.messageHandlers.name.postMessage(message)
                    // Message body is expected to be a Dictionary/Map with 'method', 'data', 'callbackId'
                    val body = didReceiveScriptMessage.body as? Map<String, Any?>
                    if (body != null && obj is NativeWebBridge) {
                        val method = body["method"] as? String
                        val data = body["data"] as? String
                        val callbackId = body["callbackId"] as? String
                        if (method != null) {
                            obj.call(method, data, callbackId)
                        }
                    } else {
                        println("Received JS message for $name: ${didReceiveScriptMessage.body}")
                    }
                }
            },
        name = name,
    )

    // If the object is a NativeWebBridge, inject a JS adapter to mimic Android's addJavascriptInterface
    if (obj is NativeWebBridge) {
        val adapterScript =
            """
            window.$name = {
                call: function(method, data, callbackId) {
                    var message = {
                        method: method,
                        data: data,
                        callbackId: callbackId
                    };
                    window.webkit.messageHandlers.$name.postMessage(message);
                }
            };
            """.trimIndent()

        val userScript =
            WKUserScript(
                source = adapterScript,
                injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
                forMainFrameOnly = false,
            )
        configuration.userContentController.addUserScript(userScript)
    }
}

@Target(AnnotationTarget.FUNCTION)
actual annotation class PlatformJavascriptInterface actual constructor()

actual var WebView.platformJavaScriptEnabled: Boolean
    get() = configuration.defaultWebpagePreferences.allowsContentJavaScript
    set(value) {
        configuration.defaultWebpagePreferences.allowsContentJavaScript = value
    }

actual var WebView.platformDomStorageEnabled: Boolean
    get() = true // Always enabled on iOS WKWebView
    set(value) {
        // No-op: Cannot easily disable DOM storage on WKWebView instance
    }

actual var WebView.platformSupportZoom: Boolean
    get() = this.scrollView.minimumZoomScale != this.scrollView.maximumZoomScale
    set(value) {
        // WKWebView zoom is handled by content meta tags or scroll view delegates
        // Simplified: disable zooming by setting scales equal
        if (!value) {
            this.scrollView.minimumZoomScale = 1.0
            this.scrollView.maximumZoomScale = 1.0
        }
    }

actual var WebView.platformBuiltInZoomControls: Boolean
    get() = false
    set(value) {
        // Not applicable to iOS
    }

actual var WebView.platformDisplayZoomControls: Boolean
    get() = false
    set(value) {
        // Not applicable to iOS
    }
