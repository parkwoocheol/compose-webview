package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
import com.parkwoocheol.composewebview.WebView
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewState

actual open class ComposeWebViewClient {
    actual open var webViewState: WebViewState? = null
    actual open var webViewController: WebViewController? = null

    actual open fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: PlatformBitmap?,
    ) {}

    actual open fun onPageFinished(
        view: WebView?,
        url: String?,
    ) {}

    actual open fun onReceivedError(
        view: WebView?,
        request: PlatformWebResourceRequest?,
        error: PlatformWebResourceError?,
    ) {}

    actual open fun shouldOverrideUrlLoading(
        view: WebView?,
        request: PlatformWebResourceRequest?,
    ): Boolean = false
}

actual open class ComposeWebChromeClient {
    actual open fun onProgressChanged(
        view: WebView?,
        newProgress: Int,
    ) {}
}
