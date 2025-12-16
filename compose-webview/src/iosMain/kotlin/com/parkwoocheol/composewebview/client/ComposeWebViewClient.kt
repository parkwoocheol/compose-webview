package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.LoadingState
import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
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
    }

    actual open fun onReceivedError(
        view: WebView?,
        request: PlatformWebResourceRequest?,
        error: PlatformWebResourceError?,
    ) {
        error?.let {
            webViewState?.errorsForCurrentRequest?.add(WebViewError(request, it))
        }
    }

    actual open fun shouldOverrideUrlLoading(
        view: WebView?,
        request: PlatformWebResourceRequest?,
    ): Boolean {
        return false
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
