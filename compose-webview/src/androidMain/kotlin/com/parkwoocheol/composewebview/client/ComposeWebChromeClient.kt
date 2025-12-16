package com.parkwoocheol.composewebview.client

import android.graphics.Bitmap
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.parkwoocheol.composewebview.CustomViewState
import com.parkwoocheol.composewebview.JsDialogState
import com.parkwoocheol.composewebview.LoadingState
import com.parkwoocheol.composewebview.PlatformCustomViewCallback
import com.parkwoocheol.composewebview.WebViewState

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class ComposeWebChromeClient : WebChromeClient() {
    var state: WebViewState? = null
    internal var onProgressChangedCallback: (WebView, Int) -> Unit = { _, _ -> }
    internal var onShowFileChooserCallback: (
        (WebView, android.webkit.ValueCallback<Array<android.net.Uri>>, android.webkit.WebChromeClient.FileChooserParams) -> Boolean
    )? = null
    internal var onPermissionRequestCallback: ((android.webkit.PermissionRequest) -> Unit)? = null

    override fun onPermissionRequest(request: android.webkit.PermissionRequest) {
        onPermissionRequestCallback?.invoke(request) ?: super.onPermissionRequest(request)
    }

    actual override fun onProgressChanged(
        view: WebView?,
        newProgress: Int,
    ) {
        super.onProgressChanged(view, newProgress)
        state?.loadingState =
            if (newProgress == 100) {
                LoadingState.Finished
            } else {
                LoadingState.Loading(newProgress / 100f)
            }
        view?.let {
            onProgressChangedCallback(it, newProgress)
        }
    }

    override fun onReceivedTitle(
        view: WebView,
        title: String?,
    ) {
        super.onReceivedTitle(view, title)
        state?.pageTitle = title
    }

    override fun onReceivedIcon(
        view: WebView,
        icon: Bitmap?,
    ) {
        super.onReceivedIcon(view, icon)
        state?.pageIcon = icon
    }

    override fun onJsAlert(
        view: WebView,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        if (state?.jsDialogState == null) {
            state?.jsDialogState =
                JsDialogState.Alert(message ?: "") {
                    result?.confirm()
                    state?.jsDialogState = null
                }
            return true
        }
        return super.onJsAlert(view, url, message, result)
    }

    override fun onJsConfirm(
        view: WebView,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        if (state?.jsDialogState == null) {
            state?.jsDialogState =
                JsDialogState.Confirm(message ?: "") { confirmed ->
                    if (confirmed) result?.confirm() else result?.cancel()
                    state?.jsDialogState = null
                }
            return true
        }
        return super.onJsConfirm(view, url, message, result)
    }

    override fun onJsPrompt(
        view: WebView,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?,
    ): Boolean {
        if (state?.jsDialogState == null) {
            state?.jsDialogState =
                JsDialogState.Prompt(message ?: "", defaultValue ?: "") { input ->
                    if (input != null) result?.confirm(input) else result?.cancel()
                    state?.jsDialogState = null
                }
            return true
        }
        return super.onJsPrompt(view, url, message, defaultValue, result)
    }

    override fun onShowCustomView(
        view: android.view.View,
        callback: CustomViewCallback,
    ) {
        state?.customViewState = CustomViewState(view, PlatformCustomViewCallback(callback))
    }

    override fun onHideCustomView() {
        state?.customViewState?.callback?.onCustomViewHidden()
        state?.customViewState = null
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: android.webkit.ValueCallback<Array<android.net.Uri>>,
        fileChooserParams: FileChooserParams,
    ): Boolean {
        return onShowFileChooserCallback?.invoke(
            webView,
            filePathCallback,
            fileChooserParams,
        ) ?: super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }
}

private typealias UriArray = Array<android.net.Uri>
