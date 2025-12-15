package com.parkwoocheol.composewebview

import dev.datlag.kcef.KCEFBrowser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.awt.BorderLayout
import java.awt.image.BufferedImage
import javax.swing.JPanel

class DesktopWebView(val browser: KCEFBrowser) : JPanel(BorderLayout()) {
    init {
        add(browser.uiComponent, BorderLayout.CENTER)
    }

    var bridge: NativeWebBridge? = null
    var javaScriptEnabled: Boolean = true
    var domStorageEnabled: Boolean = true
    var supportZoom: Boolean = true
    var builtInZoomControls: Boolean = false
    var displayZoomControls: Boolean = false
}

actual typealias WebView = DesktopWebView

actual abstract class PlatformPermissionRequest

actual typealias PlatformBitmap = java.awt.image.BufferedImage
actual typealias PlatformBundle = Any

actual fun createPlatformBundle(): PlatformBundle = Any()

actual typealias PlatformCustomView = JPanel

actual class PlatformCustomViewCallback {
    actual fun onCustomViewHidden() {
        // No-op
    }
}

actual class PlatformWebResourceError

actual class PlatformWebResourceRequest(
    actual val url: String = "",
    actual val method: String = "GET",
    actual val headers: Map<String, String> = emptyMap(),
    actual val isForMainFrame: Boolean = true,
)

actual fun WebView.platformSaveState(bundle: PlatformBundle): Any? = null

actual fun WebView.platformRestoreState(bundle: PlatformBundle): Any? = null

actual fun WebView.platformGoBack() {
    if (browser.canGoBack()) {
        browser.goBack()
    }
}

actual fun WebView.platformGoForward() {
    if (browser.canGoForward()) {
        browser.goForward()
    }
}

actual fun WebView.platformReload() {
    browser.reload()
}

actual fun WebView.platformStopLoading() {
    browser.stopLoad()
}

actual fun WebView.platformLoadUrl(
    url: String,
    additionalHttpHeaders: Map<String, String>,
) {
    browser.loadURL(url)
}

actual fun WebView.platformLoadDataWithBaseURL(
    baseUrl: String?,
    data: String,
    mimeType: String?,
    encoding: String?,
    historyUrl: String?,
) {
    browser.loadHtml(data, baseUrl ?: "about:blank")
}

actual fun WebView.platformPostUrl(
    url: String,
    postData: ByteArray,
) {}

actual fun WebView.platformEvaluateJavascript(
    script: String,
    callback: ((String) -> Unit)?,
) {
    browser.executeJavaScript(script, browser.url, 0)
}

actual fun WebView.platformZoomBy(zoomFactor: Float) {
    if (this.supportZoom) {
        browser.zoomLevel = browser.zoomLevel + zoomFactor
    }
}

actual fun WebView.platformZoomIn(): Boolean {
    if (this.supportZoom) {
        browser.zoomLevel = browser.zoomLevel + 0.5
        return true
    }
    return false
}

actual fun WebView.platformZoomOut(): Boolean {
    if (this.supportZoom) {
        browser.zoomLevel = browser.zoomLevel - 0.5
        return true
    }
    return false
}

actual fun WebView.platformFindAllAsync(find: String) {}

actual fun WebView.platformFindNext(forward: Boolean) {}

actual fun WebView.platformClearMatches() {}

actual fun WebView.platformClearCache(includeDiskFiles: Boolean) {}

actual fun WebView.platformClearHistory() {}

actual fun WebView.platformClearSslPreferences() {}

actual fun WebView.platformClearFormData() {}

actual fun WebView.platformPageUp(top: Boolean): Boolean = false

actual fun WebView.platformPageDown(bottom: Boolean): Boolean = false

actual fun WebView.platformScrollTo(
    x: Int,
    y: Int,
) {}

actual fun WebView.platformScrollBy(
    x: Int,
    y: Int,
) {}

actual fun WebView.platformSaveWebArchive(filename: String) {}

actual fun WebView.platformAddJavascriptInterface(
    obj: Any,
    name: String,
) {
    if (obj is NativeWebBridge) {
        this.bridge = obj

        // 1. Create and add Message Router
        val router = org.cef.browser.CefMessageRouter.create()
        router.addHandler(
            object : CefMessageRouterHandlerAdapter() {
                override fun onQuery(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    queryId: Long,
                    request: String?,
                    persistent: Boolean,
                    callback: CefQueryCallback?,
                ): Boolean {
                    if (request == null) return false

                    try {
                        // Parse request: { method: "...", data: "...", callbackId: "..." }
                        val json = Json.parseToJsonElement(request).jsonObject
                        val method = json["method"]?.jsonPrimitive?.content ?: return false
                        val data = json["data"]?.jsonPrimitive?.content
                        val callbackId = json["callbackId"]?.jsonPrimitive?.content

                        obj.call(method, data, callbackId)

                        callback?.success("")
                        return true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback?.failure(500, e.message)
                        return false
                    }
                }
            },
            true,
        )

        this.browser.client.addMessageRouter(router)

        // 2. Inject Polyfill Script
        // This script adapts window.AppBridgeNative.call(...) to window.cefQuery(...)
        val polyfill =
            """
            window.$name = {
                call: function(method, data, callbackId) {
                    if (!window.cefQuery) {
                        console.error("CEF JSBridge Error: window.cefQuery is not available.");
                        return;
                    }
                    window.cefQuery({
                        request: JSON.stringify({ method: method, data: data, callbackId: callbackId }),
                        onSuccess: function(response) {},
                        onFailure: function(error_code, error_message) {
                            console.error("CEF JSBridge Failed: " + error_message);
                        }
                    });
                    }
            };
            """.trimIndent()

        // Inject immediately and also on every page load
        this.platformEvaluateJavascript(polyfill, null)

        // To ensure it persists across navigations, we should ideally use a CefLoadHandler
        // But for now, we rely on the fact that WebViewJsBridge might re-inject or we need to hook into load events.
        // KCEF doesn't easily expose "inject on load" without a LoadHandler.
        // We will handle re-injection in ComposeWebView.desktop.kt via onPageFinished or similar if possible.
        // Actually, WebViewJsBridge.jsScript checks `if (window.AppBridge) return;`.
        // Our polyfill needs to be there before AppBridge uses it.
        // We'll rely on onPageFinished for now or the user calling it.
        // We will handle re-injection in ComposeWebView.desktop.kt via onPageFinished or similar if possible.
        // Actually, WebViewJsBridge.jsScript checks `if (window.AppBridge) return;`.
        // Our polyfill needs to be there before AppBridge uses it.
        // We'll rely on onPageFinished for now or the user calling it.
    }
}

@Target(AnnotationTarget.FUNCTION)
actual annotation class PlatformJavascriptInterface actual constructor()

actual abstract class PlatformContext

actual var WebView.platformJavaScriptEnabled: Boolean
    get() = this.javaScriptEnabled
    set(value) {
        this.javaScriptEnabled = value
    }

actual var WebView.platformDomStorageEnabled: Boolean
    get() = this.domStorageEnabled
    set(value) {
        this.domStorageEnabled = value
    }

actual var WebView.platformSupportZoom: Boolean
    get() = this.supportZoom
    set(value) {
        this.supportZoom = value
    }

actual var WebView.platformBuiltInZoomControls: Boolean
    get() = this.builtInZoomControls
    set(value) {
        this.builtInZoomControls = value
    }

actual var WebView.platformDisplayZoomControls: Boolean
    get() = this.displayZoomControls
    set(value) {
        this.displayZoomControls = value
    }
