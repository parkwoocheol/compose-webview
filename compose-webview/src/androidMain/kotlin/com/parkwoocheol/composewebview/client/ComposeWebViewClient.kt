package com.parkwoocheol.composewebview.client

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.parkwoocheol.composewebview.LoadingState
import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
import com.parkwoocheol.composewebview.PlatformWebResourceResponse
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewError
import com.parkwoocheol.composewebview.WebViewState
import com.parkwoocheol.composewebview.createPlatformWebResourceError
import com.parkwoocheol.composewebview.createPlatformWebResourceRequest
import com.parkwoocheol.composewebview.WebView as ComposeWebView

/**
 * A [WebViewClient] implementation that integrates with [WebViewState] and [WebViewController].
 *
 * This client manages the loading state, error handling, and navigation history updates.
 * You can extend this class to provide custom behavior while maintaining the core functionality.
 */
actual open class ComposeWebViewClient : WebViewClient() {
    actual open var webViewState: WebViewState? = null
    actual open var webViewController: WebViewController? = null

    internal var onPageStartedCallback: (WebView?, String?, Bitmap?) -> Unit = { _, _, _ -> }
    internal var onPageFinishedCallback: (WebView?, String?) -> Unit = { _, _ -> }
    internal var onReceivedErrorCallback: (WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit = { _, _, _ -> }
    internal var shouldOverrideUrlLoadingCallback: ((WebView?, PlatformWebResourceRequest?) -> Boolean)? = null
    internal var shouldInterceptRequestCallback: (
        (WebView?, PlatformWebResourceRequest?) -> PlatformWebResourceResponse?
    )? = null

    internal actual fun setOnPageStartedHandler(handler: (ComposeWebView?, String?, PlatformBitmap?) -> Unit) {
        onPageStartedCallback = handler
    }

    internal actual fun setOnPageFinishedHandler(handler: (ComposeWebView?, String?) -> Unit) {
        onPageFinishedCallback = handler
    }

    internal actual fun setOnReceivedErrorHandler(
        handler: (ComposeWebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit,
    ) {
        onReceivedErrorCallback = handler
    }

    internal actual fun setShouldOverrideUrlLoadingHandler(handler: (ComposeWebView?, PlatformWebResourceRequest?) -> Boolean) {
        shouldOverrideUrlLoadingCallback = handler
    }

    internal actual fun setShouldInterceptRequestHandler(
        handler: (
            ComposeWebView?,
            PlatformWebResourceRequest?,
        ) -> PlatformWebResourceResponse?,
    ) {
        shouldInterceptRequestCallback = handler
    }

    actual override fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: Bitmap?,
    ) {
        super.onPageStarted(view, url, favicon)
        webViewState?.loadingState = LoadingState.Loading(0.0f)
        webViewState?.errorsForCurrentRequest?.clear()
        webViewState?.pageIcon = null
        webViewState?.pageTitle = null
        webViewState?.lastLoadedUrl = url
        view?.let {
            webViewController?.canGoBack = it.canGoBack()
            webViewController?.canGoForward = it.canGoForward()
        }
        onPageStartedCallback(view, url, favicon)
    }

    actual override fun onPageFinished(
        view: WebView?,
        url: String?,
    ) {
        super.onPageFinished(view, url)
        webViewState?.loadingState = LoadingState.Finished
        view?.let {
            webViewController?.canGoBack = it.canGoBack()
            webViewController?.canGoForward = it.canGoForward()
        }
        onPageFinishedCallback(view, url)
    }

    actual open fun onReceivedError(
        view: WebView?,
        request: PlatformWebResourceRequest?,
        error: PlatformWebResourceError?,
    ) {
        error?.let {
            webViewState?.errorsForCurrentRequest?.add(WebViewError(request, it))
        }
        onReceivedErrorCallback(view, request, error)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        val platformRequest = request?.let { createPlatformWebResourceRequest(it) }
        val platformError = error?.let { createPlatformWebResourceError(it) }

        onReceivedError(view, platformRequest, platformError)
    }

    actual open fun shouldOverrideUrlLoading(
        view: WebView?,
        request: PlatformWebResourceRequest?,
    ): Boolean {
        // If a custom handler is set and view is not null, use it
        view?.let { v ->
            shouldOverrideUrlLoadingCallback?.let { handler ->
                return handler(v, request)
            }
        }
        // Otherwise, default behavior: don't override
        return false
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        val platformRequest = request?.let { createPlatformWebResourceRequest(it) }
        val result = shouldOverrideUrlLoading(view, platformRequest)
        return if (result) true else super.shouldOverrideUrlLoading(view, request)
    }

    actual open fun shouldInterceptRequest(
        view: WebView?,
        request: PlatformWebResourceRequest?,
    ): PlatformWebResourceResponse? {
        view?.let { v ->
            shouldInterceptRequestCallback?.let { handler ->
                return handler(v, request)
            }
        }
        return null
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?,
    ): android.webkit.WebResourceResponse? {
        val platformRequest = request?.let { createPlatformWebResourceRequest(it) }
        val platformResponse = shouldInterceptRequest(view, platformRequest)
        return platformResponse?.let { it.asAndroidResponse() } ?: super.shouldInterceptRequest(view, request)
    }
}
