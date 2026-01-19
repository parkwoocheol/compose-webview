package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
import com.parkwoocheol.composewebview.PlatformWebResourceResponse
import com.parkwoocheol.composewebview.WebView
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewState

/**
 * A WebView client implementation that integrates with [WebViewState] and [WebViewController].
 *
 * This client manages the loading state, error handling, and navigation history updates.
 * You can extend this class to provide custom behavior, or use the extension functions
 * for convenient configuration.
 *
 * @see rememberWebViewClient
 */
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

    open fun shouldInterceptRequest(
        view: WebView?,
        request: PlatformWebResourceRequest?,
    ): PlatformWebResourceResponse?

    // Internal setters for extension functions
    internal fun setOnPageStartedHandler(handler: (WebView?, String?, PlatformBitmap?) -> Unit)

    internal fun setOnPageFinishedHandler(handler: (WebView?, String?) -> Unit)

    internal fun setOnReceivedErrorHandler(handler: (WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit)

    internal fun setShouldOverrideUrlLoadingHandler(handler: (WebView?, PlatformWebResourceRequest?) -> Boolean)

    internal fun setShouldInterceptRequestHandler(handler: (WebView?, PlatformWebResourceRequest?) -> PlatformWebResourceResponse?)
}
