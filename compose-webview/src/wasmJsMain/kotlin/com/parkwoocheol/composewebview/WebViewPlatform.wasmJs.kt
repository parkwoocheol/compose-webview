package com.parkwoocheol.composewebview

import kotlinx.browser.window
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.MessageEvent

actual class WebView(val iframe: HTMLIFrameElement) {
    var bridge: NativeWebBridge? = null
}

actual typealias PlatformBitmap = Any // Placeholder
actual typealias PlatformBundle = Any

actual fun createPlatformBundle(): PlatformBundle = Any()

actual class PlatformCustomView

actual class PlatformCustomViewCallback {
    actual fun onCustomViewHidden() {
        // No-op for WASM
    }
}

actual class PlatformWebResourceError {
    actual val errorCode: Int = -1
    actual val description: String = "Unknown error"
}

actual abstract class PlatformPermissionRequest

actual class PlatformWebResourceRequest {
    actual val url: String = ""
    actual val method: String = "GET"
    actual val headers: Map<String, String> = emptyMap()
    actual val isForMainFrame: Boolean = true
}

actual fun WebView.platformSaveState(bundle: PlatformBundle): Any? = null

actual fun WebView.platformRestoreState(bundle: PlatformBundle): Any? = null

actual fun WebView.platformGoBack() {
    try {
        iframe.contentWindow?.history?.back()
    } catch (e: Throwable) {
        println("WASM: goBack failed (CORS?): ${e.message}")
    }
}

actual fun WebView.platformGoForward() {
    try {
        iframe.contentWindow?.history?.forward()
    } catch (e: Throwable) {
        println("WASM: goForward failed (CORS?): ${e.message}")
    }
}

actual fun WebView.platformReload() {
    try {
        iframe.contentWindow?.location?.reload()
    } catch (e: Throwable) {
        println("WASM: reload failed (CORS?): ${e.message}")
    }
}

actual fun WebView.platformStopLoading() {
    try {
        iframe.contentWindow?.stop()
    } catch (e: Throwable) {
        println("WASM: stopLoading failed (CORS?): ${e.message}")
    }
}

actual fun WebView.platformLoadUrl(
    url: String,
    additionalHttpHeaders: Map<String, String>,
) {
    iframe.src = url
}

actual fun WebView.platformLoadDataWithBaseURL(
    baseUrl: String?,
    data: String,
    mimeType: String?,
    encoding: String?,
    historyUrl: String?,
) {
    // Data URL approach for WASM
}

actual fun WebView.platformPostUrl(
    url: String,
    postData: ByteArray,
) {
    println("platformPostUrl not fully supported on WASM target yet")
}

actual fun WebView.platformEvaluateJavascript(
    script: String,
    callback: ((String) -> Unit)?,
) {
    try {
        // WASM has limited eval support due to security restrictions
        // Using a safer approach with postMessage
        callback?.invoke("null")
    } catch (e: Throwable) {
        callback?.invoke("null")
        println("WASM: JS Evaluation not fully supported: ${e.message}")
    }
}

actual fun WebView.platformAddJavascriptInterface(
    obj: Any,
    name: String,
) {
    if (obj is NativeWebBridge) {
        this.bridge = obj

        // WASM message handling - simplified due to stricter typing
        window.addEventListener("message", { event ->
            val messageEvent = event as MessageEvent
            // In WASM, dynamic access is restricted
            // Bridge functionality may be limited
            println("WASM: Message received from iframe")
        })

        // Note: JS injection is limited in WASM for security
        // Full bridge functionality may require alternative approaches
    }
}

@Target(AnnotationTarget.FUNCTION)
actual annotation class PlatformJavascriptInterface actual constructor()

actual abstract class PlatformContext

actual fun WebView.platformZoomBy(zoomFactor: Float) {
    // Not supported in WASM
}

actual fun WebView.platformZoomIn(): Boolean = false

actual fun WebView.platformZoomOut(): Boolean = false

actual fun WebView.platformFindAllAsync(find: String) {
    // Not supported
}

actual fun WebView.platformFindNext(forward: Boolean) {
    // Not supported
}

actual fun WebView.platformClearMatches() {
    // Not supported
}

actual fun WebView.platformClearCache(includeDiskFiles: Boolean) {
    // Not supported
}

actual fun WebView.platformClearHistory() {
    // Not supported
}

actual fun WebView.platformClearSslPreferences() {
    // Not supported
}

actual fun WebView.platformClearFormData() {
    // Not supported
}

actual fun WebView.platformPageUp(top: Boolean): Boolean = false

actual fun WebView.platformPageDown(bottom: Boolean): Boolean = false

actual fun WebView.platformScrollTo(
    x: Int,
    y: Int,
) {
    iframe.contentWindow?.scrollTo(x.toDouble(), y.toDouble())
}

actual fun WebView.platformScrollBy(
    x: Int,
    y: Int,
) {
    iframe.contentWindow?.scrollBy(x.toDouble(), y.toDouble())
}

actual fun WebView.platformSaveWebArchive(filename: String) {
    // Not supported
}

actual var WebView.platformJavaScriptEnabled: Boolean
    get() = true
    set(value) {
        // JS is always enabled in browser
    }

actual var WebView.platformDomStorageEnabled: Boolean
    get() = true
    set(value) {
        // DOM storage is available in browser
    }

actual var WebView.platformSupportZoom: Boolean
    get() = false
    set(value) {
        // Browser handles zoom
    }

actual var WebView.platformBuiltInZoomControls: Boolean
    get() = false
    set(value) {
        // Browser handles zoom
    }

actual var WebView.platformDisplayZoomControls: Boolean
    get() = false
    set(value) {
        // Browser handles zoom
    }
