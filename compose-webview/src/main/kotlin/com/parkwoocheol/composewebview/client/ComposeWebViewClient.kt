package com.parkwoocheol.composewebview.client

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.parkwoocheol.composewebview.LoadingState
import com.parkwoocheol.composewebview.WebViewController
import com.parkwoocheol.composewebview.WebViewError
import com.parkwoocheol.composewebview.WebViewState

/**
 * A [WebViewClient] implementation that integrates with [WebViewState] and [WebViewController].
 *
 * This client manages the loading state, error handling, and navigation history updates.
 * You can extend this class to provide custom behavior while maintaining the core functionality.
 */
open class ComposeWebViewClient : WebViewClient() {
    /**
     * The state of the WebView.
     */
    open lateinit var state: WebViewState
        internal set
        
    /**
     * The controller for the WebView.
     */
    open lateinit var controller: WebViewController
        internal set
    
    internal var onPageStartedCallback: (WebView, String?, Bitmap?) -> Unit = { _, _, _ -> }
    internal var onPageFinishedCallback: (WebView, String?) -> Unit = { _, _ -> }
    internal var onReceivedErrorCallback: (WebView, WebResourceRequest?, WebResourceError?) -> Unit = { _, _, _ -> }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        state.loadingState = LoadingState.Loading(0.0f)
        state.errorsForCurrentRequest.clear()
        state.pageIcon = null
        state.pageTitle = null
        state.lastLoadedUrl = url
        onPageStartedCallback(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        state.loadingState = LoadingState.Finished
        controller.canGoBack = view.canGoBack()
        controller.canGoForward = view.canGoForward()
        onPageFinishedCallback(view, url)
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (error != null) {
            state.errorsForCurrentRequest.add(WebViewError(request, error))
        }
        onReceivedErrorCallback(view, request, error)
    }
}

/**
 * A [WebChromeClient] implementation that integrates with [WebViewState].
 *
 * This client manages the page title, icon, loading progress, and JavaScript dialogs.
 * You can extend this class to provide custom behavior while maintaining the core functionality.
 */
open class ComposeWebChromeClient : WebChromeClient() {
    /**
     * The state of the WebView.
     */
    open lateinit var state: WebViewState
        internal set
    
    internal var onProgressChangedCallback: (WebView, Int) -> Unit = { _, _ -> }

    override fun onReceivedTitle(view: WebView, title: String?) {
        super.onReceivedTitle(view, title)
        state.pageTitle = title
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        state.pageIcon = icon
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (state.loadingState is LoadingState.Finished) return
        state.loadingState = LoadingState.Loading(newProgress / 100.0f)
        onProgressChangedCallback(view, newProgress)
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: android.webkit.JsResult?
    ): Boolean {
        if (message != null && result != null) {
            state.jsDialogState = com.parkwoocheol.composewebview.JsDialogState.Alert(message) {
                result.confirm()
                state.jsDialogState = null
            }
            return true
        }
        return super.onJsAlert(view, url, message, result)
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: android.webkit.JsResult?
    ): Boolean {
        if (message != null && result != null) {
            state.jsDialogState = com.parkwoocheol.composewebview.JsDialogState.Confirm(message) { confirmed ->
                if (confirmed) result.confirm() else result.cancel()
                state.jsDialogState = null
            }
            return true
        }
        return super.onJsConfirm(view, url, message, result)
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: android.webkit.JsPromptResult?
    ): Boolean {
        if (message != null && result != null) {
            state.jsDialogState = com.parkwoocheol.composewebview.JsDialogState.Prompt(message, defaultValue ?: "") { input ->
                if (input != null) result.confirm(input) else result.cancel()
                state.jsDialogState = null
            }
            return true
        }
        return super.onJsPrompt(view, url, message, defaultValue, result)
    }

    override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
        if (view != null && callback != null) {
            state.customViewState = com.parkwoocheol.composewebview.CustomViewState(view, callback)
        } else {
            super.onShowCustomView(view, callback)
        }
    }

    override fun onHideCustomView() {
        if (state.customViewState != null) {
            state.customViewState?.callback?.onCustomViewHidden()
            state.customViewState = null
        } else {
            super.onHideCustomView()
        }
    }
}
