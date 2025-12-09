package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
import com.parkwoocheol.composewebview.WebView
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewState

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class ComposeWebViewClient() {
    open var webViewState: WebViewState?
    open var webViewController: WebViewController?

    open fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: PlatformBitmap?,
    )

    open fun onPageFinished(
        view: WebView?,
        url: String?,
    )

    open fun onReceivedError(
        view: WebView?,
        request: PlatformWebResourceRequest?,
        error: PlatformWebResourceError?,
    )

    open fun shouldOverrideUrlLoading(
        view: WebView?,
        request: PlatformWebResourceRequest?,
    ): Boolean
}
