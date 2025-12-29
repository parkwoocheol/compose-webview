package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.ConsoleMessage
import com.parkwoocheol.composewebview.WebView

actual open class ComposeWebChromeClient {
    internal var onConsoleMessageCallback: ((WebView, ConsoleMessage) -> Boolean)? = null

    actual open fun onProgressChanged(
        view: WebView?,
        newProgress: Int,
    ) {
        // No-op for now or handle progress
    }

    actual open fun onConsoleMessage(
        view: WebView?,
        message: ConsoleMessage,
    ): Boolean {
        return view?.let { onConsoleMessageCallback?.invoke(it, message) } ?: false
    }
}
