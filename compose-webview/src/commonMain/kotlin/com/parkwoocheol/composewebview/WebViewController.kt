package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Controller for the [ComposeWebView].
 *
 * This class allows you to control the WebView imperatively, such as loading URLs,
 * navigating back/forward, reloading, evaluating JavaScript, and more.
 *
 * @property coroutineScope The scope used for dispatching navigation events.
 */
@Stable
class WebViewController(private val coroutineScope: CoroutineScope) {
    private sealed interface NavigationEvent {
        data object Back : NavigationEvent

        data object Forward : NavigationEvent

        data object Reload : NavigationEvent

        data object StopLoading : NavigationEvent

        data class EvaluateJavascript(
            val script: String,
            val callback: ((String) -> Unit)?,
        ) : NavigationEvent

        data class ZoomBy(val zoomFactor: Float) : NavigationEvent

        data object ZoomIn : NavigationEvent

        data object ZoomOut : NavigationEvent

        data class FindAllAsync(val find: String) : NavigationEvent

        data class FindNext(val forward: Boolean) : NavigationEvent

        data object ClearMatches : NavigationEvent

        data object ClearCache : NavigationEvent

        data object ClearHistory : NavigationEvent

        data object ClearSslPreferences : NavigationEvent

        data object ClearFormData : NavigationEvent

        data class PageUp(val top: Boolean) : NavigationEvent

        data class PageDown(val bottom: Boolean) : NavigationEvent

        data class ScrollTo(val x: Int, val y: Int) : NavigationEvent

        data class ScrollBy(val x: Int, val y: Int) : NavigationEvent

        data class SaveWebArchive(val filename: String) : NavigationEvent
    }

    private val navigationEvents = ReplayOnceEventFlow<NavigationEvent>()
    private var webViewState: WebViewState? = null
    private var pendingTopLevelContent: WebContent? = null

    /**
     * Whether the WebView can navigate back.
     */
    var canGoBack: Boolean by mutableStateOf(false)
        internal set

    /**
     * Whether the WebView can navigate forward.
     */
    var canGoForward: Boolean by mutableStateOf(false)
        internal set

    /**
     * Loads the given URL.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.loadUrl | HTTP headers supported |
     * | iOS      | ✅ Full | WKWebView.load | HTTP headers supported |
     * | Desktop  | ✅ Partial | JCEF loadURL | HTTP headers not supported |
     * | Web      | ✅ Partial | iframe src | HTTP headers not supported |
     *
     * @param url The URL to load.
     * @param additionalHttpHeaders Optional additional HTTP headers (Android/iOS only).
     */
    fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) {
        updateTopLevelContent(
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders,
            ),
        )
    }

    /**
     * Loads the given HTML content.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.loadDataWithBaseURL | Supports all parameters |
     * | iOS      | ✅ Full | WKWebView.loadHTMLString | Supports baseURL |
     * | Desktop  | ✅ Full | JCEF loadHTML | Supports HTML loading |
     * | Web      | ⚠️ Limited | iframe srcdoc/data URI | Limited baseURL support, CORS restrictions |
     *
     * @param html The HTML content string.
     * @param baseUrl The base URL for resolving relative URLs.
     * @param mimeType The MIME type (default "text/html").
     * @param encoding The encoding (default "utf-8").
     * @param historyUrl The URL to use for history (Android only).
     */
    fun loadHtml(
        html: String,
        baseUrl: String? = null,
        mimeType: String? = "text/html",
        encoding: String? = "utf-8",
        historyUrl: String? = null,
    ) {
        updateTopLevelContent(
            WebContent.Data(
                data = html,
                baseUrl = baseUrl,
                encoding = encoding ?: "utf-8",
                mimeType = mimeType,
                historyUrl = historyUrl,
            ),
        )
    }

    /**
     * Posts data to the given URL.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.postUrl | HTTP POST with data |
     * | iOS      | ✅ Full | WKWebView.load(URLRequest) | HTTP POST with data |
     * | Desktop  | ❌ Not supported | - | No-op, logs warning |
     * | Web      | ❌ Not supported | - | No-op, logs warning |
     *
     * @param url The URL to post to.
     * @param postData The data to post.
     */
    fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        updateTopLevelContent(WebContent.Post(url, postData))
    }

    /**
     * Evaluates the given JavaScript in the context of the currently displayed page.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.evaluateJavascript | Async with callback |
     * | iOS      | ✅ Full | WKWebView.evaluateJavaScript | Async with callback |
     * | Desktop  | ✅ Full | JCEF executeJavaScript | Async with callback |
     * | Web      | ⚠️ Limited | contentWindow.eval | CORS restricted - same-origin only |
     *
     * @param script The JavaScript to evaluate.
     * @param callback Optional callback to receive the result of the evaluation.
     */
    fun evaluateJavascript(
        script: String,
        callback: ((String) -> Unit)? = null,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(NavigationEvent.EvaluateJavascript(script, callback))
        }
    }

    /**
     * Navigates back in the history of the WebView.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.goBack | Native history navigation |
     * | iOS      | ✅ Full | WKWebView.goBack | Native history navigation |
     * | Desktop  | ✅ Full | JCEF goBack | Native history navigation |
     * | Web      | ⚠️ Limited | history.back | CORS restricted - same-origin only |
     */
    fun navigateBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Back) }
    }

    /**
     * Navigates forward in the history of the WebView.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.goForward | Native history navigation |
     * | iOS      | ✅ Full | WKWebView.goForward | Native history navigation |
     * | Desktop  | ✅ Full | JCEF goForward | Native history navigation |
     * | Web      | ⚠️ Limited | history.forward | CORS restricted - same-origin only |
     */
    fun navigateForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Forward) }
    }

    /**
     * Reloads the current page.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.reload | Reloads from network or cache |
     * | iOS      | ✅ Full | WKWebView.reload | Reloads from network or cache |
     * | Desktop  | ✅ Full | JCEF reload | Reloads from network or cache |
     * | Web      | ⚠️ Limited | location.reload | CORS restricted - same-origin only |
     */
    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Reload) }
    }

    /**
     * Stops the current load.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.stopLoading | Stops page loading immediately |
     * | iOS      | ✅ Full | WKWebView.stopLoading | Stops page loading immediately |
     * | Desktop  | ✅ Full | JCEF stop | Stops page loading immediately |
     * | Web      | ⚠️ Limited | window.stop | CORS restricted - same-origin only |
     */
    fun stopLoading() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.StopLoading) }
    }

    /**
     * Zooms the WebView content by the specified factor.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.zoomBy | Multiplies current zoom level |
     * | iOS      | ❌ Not supported | - | WKWebView doesn't support programmatic zoom |
     * | Desktop  | ✅ Full | JCEF setZoomLevel | Sets absolute zoom level |
     * | Web      | ❌ Not supported | - | Browser controlled |
     *
     * @param zoomFactor The zoom factor to apply (e.g., 1.5 for 150% zoom).
     */
    fun zoomBy(zoomFactor: Float) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ZoomBy(zoomFactor)) }
    }

    /**
     * Zooms in the WebView content.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.zoomIn | Native zoom controls |
     * | iOS      | ❌ Not supported | - | WKWebView doesn't support programmatic zoom |
     * | Desktop  | ✅ Full | JCEF zoomIn | Native zoom controls |
     * | Web      | ❌ Not supported | - | Browser controlled |
     */
    fun zoomIn() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ZoomIn) }
    }

    /**
     * Zooms out the WebView content.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.zoomOut | Native zoom controls |
     * | iOS      | ❌ Not supported | - | WKWebView doesn't support programmatic zoom |
     * | Desktop  | ✅ Full | JCEF zoomOut | Native zoom controls |
     * | Web      | ❌ Not supported | - | Browser controlled |
     */
    fun zoomOut() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ZoomOut) }
    }

    /**
     * Finds all instances of the string asynchronously.
     *
     * Use with [onFindResultReceived] callback to get match counts.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.findAllAsync | Highlights all matches |
     * | iOS      | ❌ Not supported | - | WKWebView lacks find API |
     * | Desktop  | ❌ Not supported | - | JCEF find API not exposed |
     * | Web      | ❌ Not supported | - | No cross-browser standard |
     *
     * @param find The string to find.
     */
    fun findAllAsync(find: String) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.FindAllAsync(find)) }
    }

    /**
     * Finds the next instance of the string.
     *
     * Must be called after [findAllAsync].
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.findNext | Navigates to next match |
     * | iOS      | ❌ Not supported | - | WKWebView lacks find API |
     * | Desktop  | ❌ Not supported | - | JCEF find API not exposed |
     * | Web      | ❌ Not supported | - | No cross-browser standard |
     *
     * @param forward Whether to search forward (true) or backward (false).
     */
    fun findNext(forward: Boolean) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.FindNext(forward)) }
    }

    /**
     * Clears the matches found by [findAllAsync].
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.clearMatches | Removes highlighting |
     * | iOS      | ❌ Not supported | - | WKWebView lacks find API |
     * | Desktop  | ❌ Not supported | - | JCEF find API not exposed |
     * | Web      | ❌ Not supported | - | No cross-browser standard |
     */
    fun clearMatches() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearMatches) }
    }

    /**
     * Clears the resource cache.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.clearCache | Clears disk and memory cache |
     * | iOS      | ❌ Not supported | - | Use WKWebsiteDataStore separately |
     * | Desktop  | ❌ Not supported | - | JCEF cache management separate |
     * | Web      | ❌ Not supported | - | Browser controlled |
     */
    fun clearCache() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearCache) }
    }

    /**
     * Clears the internal back/forward list.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.clearHistory | Clears navigation history |
     * | iOS      | ❌ Not supported | - | WKWebView history is read-only |
     * | Desktop  | ❌ Not supported | - | JCEF history not exposed |
     * | Web      | ❌ Not supported | - | Browser controlled |
     */
    fun clearHistory() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearHistory) }
    }

    /**
     * Clears the SSL preferences table stored in response to proceeding with SSL certificate errors.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.clearSslPreferences | Clears SSL error exceptions |
     * | iOS      | ❌ Not supported | - | WKWebView doesn't store SSL preferences |
     * | Desktop  | ❌ Not supported | - | JCEF SSL handling different |
     * | Web      | ❌ Not supported | - | Browser controlled |
     */
    fun clearSslPreferences() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearSslPreferences) }
    }

    /**
     * Clears the auto-fill form data stored by the WebView.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.clearFormData | Clears autofill data |
     * | iOS      | ❌ Not supported | - | WKWebView form data separate |
     * | Desktop  | ❌ Not supported | - | JCEF form data separate |
     * | Web      | ❌ Not supported | - | Browser controlled |
     */
    fun clearFormData() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearFormData) }
    }

    /**
     * Scrolls the page up by one screen height.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.pageUp | Scrolls up by viewport height |
     * | iOS      | ⚠️ Partial | scrollBy(viewportHeight) | Implemented via scrollBy |
     * | Desktop  | ❌ Not supported | - | JCEF scroll API not exposed |
     * | Web      | ⚠️ Partial | scrollBy(viewportHeight) | Implemented via scrollBy |
     *
     * @param top If true, scrolls to the top of the page.
     */
    fun pageUp(top: Boolean) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.PageUp(top)) }
    }

    /**
     * Scrolls the page down by one screen height.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.pageDown | Scrolls down by viewport height |
     * | iOS      | ⚠️ Partial | scrollBy(viewportHeight) | Implemented via scrollBy |
     * | Desktop  | ❌ Not supported | - | JCEF scroll API not exposed |
     * | Web      | ⚠️ Partial | scrollBy(viewportHeight) | Implemented via scrollBy |
     *
     * @param bottom If true, scrolls to the bottom of the page.
     */
    fun pageDown(bottom: Boolean) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.PageDown(bottom)) }
    }

    /**
     * Scrolls to the given absolute position.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.scrollTo | Scrolls to absolute position |
     * | iOS      | ✅ Full | scrollView.setContentOffset | Scrolls to absolute position |
     * | Desktop  | ❌ Not supported | - | JCEF scroll API not exposed |
     * | Web      | ✅ Full | scrollTo() | Scrolls to absolute position |
     *
     * @param x The x-coordinate in pixels.
     * @param y The y-coordinate in pixels.
     */
    fun scrollTo(
        x: Int,
        y: Int,
    ) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ScrollTo(x, y)) }
    }

    /**
     * Scrolls by the given relative amount.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.scrollBy | Scrolls relative to current position |
     * | iOS      | ✅ Full | scrollView.contentOffset | Scrolls relative to current position |
     * | Desktop  | ❌ Not supported | - | JCEF scroll API not exposed |
     * | Web      | ✅ Full | scrollBy() | Scrolls relative to current position |
     *
     * @param x The x-offset in pixels.
     * @param y The y-offset in pixels.
     */
    fun scrollBy(
        x: Int,
        y: Int,
    ) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ScrollBy(x, y)) }
    }

    /**
     * Saves the current page as a web archive.
     *
     * **Platform Support:**
     * | Platform | Support | Implementation | Notes |
     * |----------|---------|----------------|-------|
     * | Android  | ✅ Full | WebView.saveWebArchive | Saves as .mht file |
     * | iOS      | ❌ Not supported | - | WKWebView doesn't support web archives |
     * | Desktop  | ❌ Not supported | - | JCEF archive API not exposed |
     * | Web      | ❌ Not supported | - | Browser controlled |
     *
     * @param filename The filename to save the archive to (Android only).
     */
    fun saveWebArchive(filename: String) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.SaveWebArchive(filename)) }
    }

    internal fun bindState(state: WebViewState) {
        webViewState = state
        pendingTopLevelContent?.let { pendingContent ->
            state.updateContentRequest(pendingContent)
            pendingTopLevelContent = null
        }
    }

    internal fun unbindState(state: WebViewState) {
        if (webViewState === state) {
            webViewState = null
        }
    }

    internal suspend fun handleNavigationEvents(webView: WebView) {
        withContext(Dispatchers.Main) {
            navigationEvents.collect { event ->
                when (event) {
                    is NavigationEvent.Back -> webView.platformGoBack()
                    is NavigationEvent.Forward -> webView.platformGoForward()
                    is NavigationEvent.Reload -> webView.platformReload()
                    is NavigationEvent.StopLoading -> webView.platformStopLoading()
                    is NavigationEvent.EvaluateJavascript -> webView.platformEvaluateJavascript(event.script, event.callback)
                    is NavigationEvent.ZoomBy -> webView.platformZoomBy(event.zoomFactor)
                    is NavigationEvent.ZoomIn -> webView.platformZoomIn()
                    is NavigationEvent.ZoomOut -> webView.platformZoomOut()
                    is NavigationEvent.FindAllAsync -> webView.platformFindAllAsync(event.find)
                    is NavigationEvent.FindNext -> webView.platformFindNext(event.forward)
                    is NavigationEvent.ClearMatches -> webView.platformClearMatches()
                    is NavigationEvent.ClearCache -> webView.platformClearCache(true)
                    is NavigationEvent.ClearHistory -> webView.platformClearHistory()
                    is NavigationEvent.ClearSslPreferences -> webView.platformClearSslPreferences()
                    is NavigationEvent.ClearFormData -> webView.platformClearFormData()
                    is NavigationEvent.PageUp -> webView.platformPageUp(event.top)
                    is NavigationEvent.PageDown -> webView.platformPageDown(event.bottom)
                    is NavigationEvent.ScrollTo -> webView.platformScrollTo(event.x, event.y)
                    is NavigationEvent.ScrollBy -> webView.platformScrollBy(event.x, event.y)
                    is NavigationEvent.SaveWebArchive -> webView.platformSaveWebArchive(event.filename)
                }
            }
        }
    }

    private fun updateTopLevelContent(content: WebContent) {
        webViewState?.updateContentRequest(content) ?: run {
            pendingTopLevelContent = content
        }
    }
}

/**
 * Creates and remembers a [WebViewController].
 *
 * @param coroutineScope The coroutine scope to be used by the controller.
 * @return A [WebViewController] instance.
 */
@Composable
fun rememberWebViewController(coroutineScope: CoroutineScope = rememberCoroutineScope()): WebViewController =
    remember(coroutineScope) {
        WebViewController(coroutineScope)
    }
