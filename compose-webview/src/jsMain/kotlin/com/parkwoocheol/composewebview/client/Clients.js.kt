package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.ConsoleMessage
import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
import com.parkwoocheol.composewebview.WebView
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewState

actual open class ComposeWebViewClient {
    actual open var webViewState: WebViewState? = null
    actual open var webViewController: WebViewController? = null

    internal var onPageStartedCallback: (WebView?, String?, PlatformBitmap?) -> Unit = { _, _, _ -> }
    internal var onPageFinishedCallback: (WebView?, String?) -> Unit = { _, _ -> }
    internal var onReceivedErrorCallback: (WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit = { _, _, _ -> }
    internal var shouldOverrideUrlLoadingCallback: ((WebView?, PlatformWebResourceRequest?) -> Boolean)? = null

    internal actual fun setOnPageStartedHandler(handler: (WebView?, String?, PlatformBitmap?) -> Unit) {
        onPageStartedCallback = handler
    }

    internal actual fun setOnPageFinishedHandler(handler: (WebView?, String?) -> Unit) {
        onPageFinishedCallback = handler
    }

    internal actual fun setOnReceivedErrorHandler(handler: (WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit) {
        onReceivedErrorCallback = handler
    }

    internal actual fun setShouldOverrideUrlLoadingHandler(handler: (WebView?, PlatformWebResourceRequest?) -> Boolean) {
        shouldOverrideUrlLoadingCallback = handler
    }

    actual open fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: PlatformBitmap?,
    ) {
        onPageStartedCallback(view, url, favicon)
    }

    actual open fun onPageFinished(
        view: WebView?,
        url: String?,
    ) {
        onPageFinishedCallback(view, url)
    }

    actual open fun onReceivedError(
        view: WebView?,
        request: PlatformWebResourceRequest?,
        error: PlatformWebResourceError?,
    ) {
        onReceivedErrorCallback(view, request, error)
    }

    actual open fun shouldOverrideUrlLoading(
        view: WebView?,
        request: PlatformWebResourceRequest?,
    ): Boolean {
        view?.let { v ->
            shouldOverrideUrlLoadingCallback?.let { handler ->
                return handler(v, request)
            }
        }
        return false
    }
}

actual open class ComposeWebChromeClient {
    internal var onProgressChangedCallback: (WebView?, Int) -> Unit = { _, _ -> }
    internal var onConsoleMessageCallback: (WebView?, ConsoleMessage) -> Boolean = { _, _ -> false }
    internal var onPermissionRequestCallback: ((com.parkwoocheol.composewebview.PlatformPermissionRequest) -> Unit)? = null

    internal actual fun setOnProgressChangedHandler(handler: (WebView?, Int) -> Unit) {
        onProgressChangedCallback = handler
    }

    internal actual fun setOnConsoleMessageHandler(handler: (WebView?, ConsoleMessage) -> Boolean) {
        onConsoleMessageCallback = handler
    }

    internal actual fun setOnPermissionRequestHandler(handler: (com.parkwoocheol.composewebview.PlatformPermissionRequest) -> Unit) {
        onPermissionRequestCallback = handler
    }

    actual open fun onProgressChanged(
        view: WebView?,
        newProgress: Int,
    ) {
        onProgressChangedCallback(view, newProgress)
    }

    actual open fun onConsoleMessage(
        view: WebView?,
        message: ConsoleMessage,
    ): Boolean {
        return onConsoleMessageCallback(view, message)
    }
}
