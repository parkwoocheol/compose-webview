package com.parkwoocheol.composewebview

internal interface WebViewJsBridgeRuntime {
    val capabilities: BridgeCapabilities

    fun attach(
        bridge: WebViewJsBridge,
        webView: WebView,
    )

    fun onPageStarted(bridge: WebViewJsBridge)

    fun pageFinishedBootstrapScript(bridge: WebViewJsBridge): String?

    fun emit(
        bridge: WebViewJsBridge,
        event: String,
        payloadJson: String,
    )

    fun dispose(bridge: WebViewJsBridge)
}

internal object DefaultWebViewJsBridgeRuntime : WebViewJsBridgeRuntime {
    override val capabilities: BridgeCapabilities = BridgeCapabilities()

    override fun attach(
        bridge: WebViewJsBridge,
        webView: WebView,
    ) {
        bridge.webView = webView
        webView.platformAddJavascriptInterface(bridge, bridge.nativeInterfaceName)
    }

    override fun onPageStarted(bridge: WebViewJsBridge) = Unit

    override fun pageFinishedBootstrapScript(bridge: WebViewJsBridge): String? = bridge.jsScript

    override fun emit(
        bridge: WebViewJsBridge,
        event: String,
        payloadJson: String,
    ) {
        val script = "window.${bridge.jsObjectName}.trigger('$event', $payloadJson);"
        bridge.webView?.platformEvaluateJavascript(script, null)
    }

    override fun dispose(bridge: WebViewJsBridge) = Unit
}
