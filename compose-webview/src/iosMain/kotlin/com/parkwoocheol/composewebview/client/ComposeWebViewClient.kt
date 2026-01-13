package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.LoadingState
import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
import com.parkwoocheol.composewebview.PlatformWebResourceResponse
import com.parkwoocheol.composewebview.WebView
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewError
import com.parkwoocheol.composewebview.WebViewState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSError
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

actual open class ComposeWebViewClient {
    actual open var webViewState: WebViewState? = null
    actual open var webViewController: WebViewController? = null

    internal var onPageStartedCallback: (WebView?, String?, PlatformBitmap?) -> Unit = { _, _, _ -> }
    internal var onPageFinishedCallback: (WebView?, String?) -> Unit = { _, _ -> }
    internal var onReceivedErrorCallback: (WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit = { _, _, _ -> }
    internal var shouldOverrideUrlLoadingCallback: ((WebView?, PlatformWebResourceRequest?) -> Boolean)? = null
    internal var shouldInterceptRequestCallback: (
        (WebView?, PlatformWebResourceRequest?) -> PlatformWebResourceResponse?
    )? = null

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

    internal actual fun setShouldInterceptRequestHandler(
        handler: (
            WebView?,
            PlatformWebResourceRequest?,
        ) -> PlatformWebResourceResponse?,
    ) {
        shouldInterceptRequestCallback = handler
    }

    actual open fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: PlatformBitmap?,
    ) {
        webViewState?.loadingState = LoadingState.Loading(0.0f)
        webViewState?.errorsForCurrentRequest?.clear()
        webViewState?.pageIcon = null
        webViewState?.pageTitle = null
        webViewState?.lastLoadedUrl = url
        onPageStartedCallback(view, url, favicon)
    }

    actual open fun onPageFinished(
        view: WebView?,
        url: String?,
    ) {
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
}

@Suppress("CONFLICTING_OVERLOADS")
internal class ComposeWebViewDelegate(
    private val client: ComposeWebViewClient,
) : NSObject(), WKNavigationDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?,
    ) {
        client.onPageStarted(webView, webView.URL?.absoluteString, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?,
    ) {
        client.onPageFinished(webView, webView.URL?.absoluteString)
    }

    @OptIn(ExperimentalForeignApi::class)
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val requestUrl = decidePolicyForNavigationAction.request.URL
        val scheme = requestUrl?.scheme

        if (scheme != null && !scheme.startsWith("http") && !scheme.startsWith("file")) {
            if (platform.UIKit.UIApplication.sharedApplication.canOpenURL(requestUrl)) {
                platform.UIKit.UIApplication.sharedApplication.openURL(requestUrl)
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                return
            }
        }

        val request =
            PlatformWebResourceRequest(
                decidePolicyForNavigationAction.request,
                decidePolicyForNavigationAction.targetFrame?.mainFrame ?: false,
            )
        if (client.shouldOverrideUrlLoading(webView, request)) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
        } else {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        client.onReceivedError(webView, null, PlatformWebResourceError(withError))
    }

    @OptIn(ExperimentalForeignApi::class)
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError,
    ) {
        client.onReceivedError(webView, null, PlatformWebResourceError(withError))
    }
}

@Suppress("CONFLICTING_OVERLOADS")
internal class ComposeWKURLSchemeHandler(
    private val client: ComposeWebViewClient,
) : NSObject(), platform.WebKit.WKURLSchemeHandlerProtocol {
    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    override fun webView(
        webView: WKWebView,
        startURLSchemeTask: platform.WebKit.WKURLSchemeTaskProtocol,
    ) {
        val request = PlatformWebResourceRequest(startURLSchemeTask.request, false)
        val response = client.shouldInterceptRequest(webView, request)

        if (response != null) {
            val nsUrlResponse =
                platform.Foundation.NSHTTPURLResponse(
                    uRL = startURLSchemeTask.request.URL!!,
                    statusCode = response.statusCode.toLong(),
                    HTTPVersion = "HTTP/1.1",
                    headerFields = response.responseHeaders?.let { it as Map<Any?, *> },
                )
            startURLSchemeTask.didReceiveResponse(nsUrlResponse)

            val data =
                response.data?.let {
                    it.usePinned { pinned ->
                        platform.Foundation.NSData.dataWithBytes(pinned.addressOf(0), it.size.toULong())
                    }
                }
            if (data != null) {
                startURLSchemeTask.didReceiveData(data)
            }
            startURLSchemeTask.didFinish()
        } else {
            val error =
                platform.Foundation.NSError.errorWithDomain(
                    domain = "com.parkwoocheol.composewebview",
                    code = -1,
                    userInfo = null,
                )
            startURLSchemeTask.didFailWithError(error)
        }
    }

    override fun webView(
        webView:
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
            WKWebView,
        stopURLSchemeTask: platform.WebKit.WKURLSchemeTaskProtocol,
    ) {
        // No-op
    }
}
