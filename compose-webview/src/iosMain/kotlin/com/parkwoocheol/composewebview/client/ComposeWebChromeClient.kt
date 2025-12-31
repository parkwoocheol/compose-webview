package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.ConsoleMessage
import com.parkwoocheol.composewebview.WebView

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
