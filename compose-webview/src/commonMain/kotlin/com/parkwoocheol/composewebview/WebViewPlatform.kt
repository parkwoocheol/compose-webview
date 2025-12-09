@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

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

/**
 * Saves the state of the WebView to the given bundle.
 */
expect fun WebView.platformSaveState(bundle: PlatformBundle): Any?

/**
 * Restores the state of the WebView from the given bundle.
 */
expect fun WebView.platformRestoreState(bundle: PlatformBundle): Any?

/**
 * Navigates back in the history of the WebView.
 */
expect fun WebView.platformGoBack()

/**
 * Navigates forward in the history of the WebView.
 */
expect fun WebView.platformGoForward()

/**
 * Reloads the current page.
 */
expect fun WebView.platformReload()

/**
 * Stops the current load.
 */
expect fun WebView.platformStopLoading()

/**
 * Loads the given URL.
 */
expect fun WebView.platformLoadUrl(
    url: String,
    additionalHttpHeaders: Map<String, String>,
)

/**
 * Loads the given data with the base URL.
 */
expect fun WebView.platformLoadDataWithBaseURL(
    baseUrl: String?,
    data: String,
    mimeType: String?,
    encoding: String?,
    historyUrl: String?,
)

/**
 * Posts data to the given URL.
 */
expect fun WebView.platformPostUrl(
    url: String,
    postData: ByteArray,
)

/**
 * Evaluates the given JavaScript.
 */
expect fun WebView.platformEvaluateJavascript(
    script: String,
    callback: ((String) -> Unit)?,
)

/**
 * Zooms by the given factor.
 */
expect fun WebView.platformZoomBy(zoomFactor: Float)

/**
 * Zooms in.
 */
expect fun WebView.platformZoomIn(): Boolean

/**
 * Zooms out.
 */
expect fun WebView.platformZoomOut(): Boolean

/**
 * Finds all instances of the string asynchronously.
 */
expect fun WebView.platformFindAllAsync(find: String)

/**
 * Finds the next instance of the string.
 */
expect fun WebView.platformFindNext(forward: Boolean)

/**
 * Clears the matches found by [platformFindAllAsync].
 */
expect fun WebView.platformClearMatches()

/**
 * Clears the resource cache.
 */
expect fun WebView.platformClearCache(includeDiskFiles: Boolean)

/**
 * Clears the internal back/forward list.
 */
expect fun WebView.platformClearHistory()

/**
 * Clears the SSL preferences table.
 */
expect fun WebView.platformClearSslPreferences()

/**
 * Clears the auto-fill form data.
 */
expect fun WebView.platformClearFormData()

/**
 * Scrolls the page up.
 */
expect fun WebView.platformPageUp(top: Boolean): Boolean

/**
 * Scrolls the page down.
 */
expect fun WebView.platformPageDown(bottom: Boolean): Boolean

/**
 * Scrolls to the given position.
 */
expect fun WebView.platformScrollTo(
    x: Int,
    y: Int,
)

/**
 * Scrolls by the given amount.
 */
expect fun WebView.platformScrollBy(
    x: Int,
    y: Int,
)

/**
 * Saves the current view as a web archive.
 */
expect fun WebView.platformSaveWebArchive(filename: String)

expect fun WebView.platformAddJavascriptInterface(
    obj: Any,
    name: String,
)

@Target(AnnotationTarget.FUNCTION)
expect annotation class PlatformJavascriptInterface()

expect abstract class PlatformContext



expect var WebView.platformJavaScriptEnabled: Boolean

expect var WebView.platformDomStorageEnabled: Boolean

expect var WebView.platformSupportZoom: Boolean

expect var WebView.platformBuiltInZoomControls: Boolean

expect var WebView.platformDisplayZoomControls: Boolean
