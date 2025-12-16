package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.JsDialogState
import com.parkwoocheol.composewebview.WebViewState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKFrameInfo
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKUIDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWindowFeatures
import platform.darwin.NSObject

@Suppress("CONFLICTING_OVERLOADS")
internal class ComposeWebViewUIDelegate(
    private val state: WebViewState,
) : NSObject(), WKUIDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun webView(
        webView: WKWebView,
        createWebViewWithConfiguration: WKWebViewConfiguration,
        forNavigationAction: WKNavigationAction,
        windowFeatures: WKWindowFeatures,
    ): WKWebView? {
        // Handle target="_blank"
        if (forNavigationAction.targetFrame == null) {
            webView.loadRequest(forNavigationAction.request)
        }
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun webView(
        webView: WKWebView,
        runJavaScriptAlertPanelWithMessage: String,
        initiatedByFrame: WKFrameInfo,
        completionHandler: () -> Unit,
    ) {
        if (state.jsDialogState == null) {
            state.jsDialogState =
                JsDialogState.Alert(runJavaScriptAlertPanelWithMessage) {
                    completionHandler()
                    state.jsDialogState = null
                }
        } else {
            completionHandler()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun webView(
        webView: WKWebView,
        runJavaScriptConfirmPanelWithMessage: String,
        initiatedByFrame: WKFrameInfo,
        completionHandler: (Boolean) -> Unit,
    ) {
        if (state.jsDialogState == null) {
            state.jsDialogState =
                JsDialogState.Confirm(runJavaScriptConfirmPanelWithMessage) { result ->
                    completionHandler(result)
                    state.jsDialogState = null
                }
        } else {
            completionHandler(false)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun webView(
        webView: WKWebView,
        runJavaScriptTextInputPanelWithPrompt: String,
        defaultText: String?,
        initiatedByFrame: WKFrameInfo,
        completionHandler: (String?) -> Unit,
    ) {
        if (state.jsDialogState == null) {
            state.jsDialogState =
                JsDialogState.Prompt(runJavaScriptTextInputPanelWithPrompt, defaultText ?: "") { result ->
                    completionHandler(result)
                    state.jsDialogState = null
                }
        } else {
            completionHandler(null)
        }
    }
}
