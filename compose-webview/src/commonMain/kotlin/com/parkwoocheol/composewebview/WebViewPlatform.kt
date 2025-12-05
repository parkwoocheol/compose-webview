package com.parkwoocheol.composewebview

expect class WebView


expect class PlatformBitmap
expect class PlatformBundle
expect fun createPlatformBundle(): PlatformBundle

expect class PlatformCustomView

expect class PlatformCustomViewCallback {
    fun onCustomViewHidden()
}

expect class PlatformWebResourceError
expect class PlatformWebResourceRequest {
    val url: String
    val method: String
    val headers: Map<String, String>
    val isForMainFrame: Boolean
}

expect fun WebView.platformSaveState(bundle: PlatformBundle): Any?
expect fun WebView.platformRestoreState(bundle: PlatformBundle): Any?

expect fun WebView.platformGoBack()
expect fun WebView.platformGoForward()
expect fun WebView.platformReload()
expect fun WebView.platformStopLoading()
expect fun WebView.platformLoadUrl(url: String, additionalHttpHeaders: Map<String, String>)
expect fun WebView.platformLoadDataWithBaseURL(
    baseUrl: String?,
    data: String,
    mimeType: String?,
    encoding: String?,
    historyUrl: String?
)
expect fun WebView.platformPostUrl(url: String, postData: ByteArray)
expect fun WebView.platformEvaluateJavascript(script: String, callback: ((String) -> Unit)?)
expect fun WebView.platformZoomBy(zoomFactor: Float)
expect fun WebView.platformZoomIn(): Boolean
expect fun WebView.platformZoomOut(): Boolean
expect fun WebView.platformFindAllAsync(find: String)
expect fun WebView.platformFindNext(forward: Boolean)
expect fun WebView.platformClearMatches()
expect fun WebView.platformClearCache(includeDiskFiles: Boolean)
expect fun WebView.platformClearHistory()
expect fun WebView.platformClearSslPreferences()
expect fun WebView.platformClearFormData()
expect fun WebView.platformPageUp(top: Boolean): Boolean
expect fun WebView.platformPageDown(bottom: Boolean): Boolean
expect fun WebView.platformScrollTo(x: Int, y: Int)
expect fun WebView.platformScrollBy(x: Int, y: Int)
expect fun WebView.platformSaveWebArchive(filename: String)

expect fun WebView.platformAddJavascriptInterface(obj: Any, name: String)

@Target(AnnotationTarget.FUNCTION)
expect annotation class PlatformJavascriptInterface()

expect abstract class PlatformContext


expect open class ComposeWebViewClient()
expect open class ComposeWebChromeClient()






