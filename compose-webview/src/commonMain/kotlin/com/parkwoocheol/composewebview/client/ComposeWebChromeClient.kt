package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.WebView

expect open class ComposeWebChromeClient() {
    open fun onProgressChanged(view: WebView?, newProgress: Int)
}
