package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewState
import com.parkwoocheol.composewebview.WebView

expect open class ComposeWebViewClient() {
    open var webViewState: WebViewState?
    open var webViewController: WebViewController?

    open fun onPageStarted(view: WebView?, url: String?, favicon: PlatformBitmap?)
    open fun onPageFinished(view: WebView?, url: String?)
    open fun onReceivedError(view: WebView?, request: PlatformWebResourceRequest?, error: PlatformWebResourceError?)
    open fun shouldOverrideUrlLoading(view: WebView?, request: PlatformWebResourceRequest?): Boolean
}
