package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.WebView

actual open class ComposeWebChromeClient {
    actual open fun onProgressChanged(
        view: WebView?,
        newProgress: Int,
    ) {
        // No-op for now or handle progress
    }
}
