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
import kotlinx.coroutines.flow.MutableSharedFlow
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

        data class LoadUrl(
            val url: String,
            val additionalHttpHeaders: Map<String, String> = emptyMap(),
        ) : NavigationEvent

        data class LoadHtml(
            val html: String,
            val baseUrl: String? = null,
            val mimeType: String? = null,
            val encoding: String? = "utf-8",
            val historyUrl: String? = null,
        ) : NavigationEvent

        data class PostUrl(
            val url: String,
            val postData: ByteArray,
        ) : NavigationEvent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as PostUrl

                if (url != other.url) return false
                if (!postData.contentEquals(other.postData)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = url.hashCode()
                result = 31 * result + postData.contentHashCode()
                return result
            }
        }

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

    private val navigationEvents = MutableSharedFlow<NavigationEvent>(replay = 1)

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
     * @param url The URL to load.
     * @param additionalHttpHeaders Optional additional HTTP headers.
     */
    fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) {
        coroutineScope.launch {
            navigationEvents.emit(NavigationEvent.LoadUrl(url, additionalHttpHeaders))
        }
    }

    /**
     * Loads the given HTML content.
     *
     * @param html The HTML content string.
     * @param baseUrl The base URL.
     * @param mimeType The MIME type (default "text/html").
     * @param encoding The encoding (default "utf-8").
     * @param historyUrl The history URL.
     */
    fun loadHtml(
        html: String,
        baseUrl: String? = null,
        mimeType: String? = "text/html",
        encoding: String? = "utf-8",
        historyUrl: String? = null,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadHtml(
                    html,
                    baseUrl,
                    mimeType,
                    encoding,
                    historyUrl,
                ),
            )
        }
    }

    /**
     * Posts data to the given URL.
     *
     * @param url The URL to post to.
     * @param postData The data to post.
     */
    fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(NavigationEvent.PostUrl(url, postData))
        }
    }

    /**
     * Evaluates the given JavaScript in the context of the currently displayed page.
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
     */
    fun navigateBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Back) }
    }

    /**
     * Navigates forward in the history of the WebView.
     */
    fun navigateForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Forward) }
    }

    /**
     * Reloads the current page.
     */
    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Reload) }
    }

    /**
     * Stops the current load.
     */
    fun stopLoading() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.StopLoading) }
    }

    /**
     * Zooms by the given factor.
     */
    fun zoomBy(zoomFactor: Float) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ZoomBy(zoomFactor)) }
    }

    /**
     * Zooms in.
     */
    fun zoomIn() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ZoomIn) }
    }

    /**
     * Zooms out.
     */
    fun zoomOut() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ZoomOut) }
    }

    /**
     * Finds all instances of the string asynchronously.
     */
    fun findAllAsync(find: String) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.FindAllAsync(find)) }
    }

    /**
     * Finds the next instance of the string.
     */
    fun findNext(forward: Boolean) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.FindNext(forward)) }
    }

    /**
     * Clears the matches found by [findAllAsync].
     */
    fun clearMatches() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearMatches) }
    }

    /**
     * Clears the resource cache.
     */
    fun clearCache() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearCache) }
    }

    /**
     * Clears the internal back/forward list.
     */
    fun clearHistory() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearHistory) }
    }

    /**
     * Clears the SSL preferences table.
     */
    fun clearSslPreferences() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearSslPreferences) }
    }

    /**
     * Clears the auto-fill form data.
     */
    fun clearFormData() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ClearFormData) }
    }

    /**
     * Scrolls the page up.
     */
    fun pageUp(top: Boolean) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.PageUp(top)) }
    }

    /**
     * Scrolls the page down.
     */
    fun pageDown(bottom: Boolean) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.PageDown(bottom)) }
    }

    /**
     * Scrolls to the given position.
     */
    fun scrollTo(
        x: Int,
        y: Int,
    ) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ScrollTo(x, y)) }
    }

    /**
     * Scrolls by the given amount.
     */
    fun scrollBy(
        x: Int,
        y: Int,
    ) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ScrollBy(x, y)) }
    }

    /**
     * Saves the current view as a web archive.
     */
    fun saveWebArchive(filename: String) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.SaveWebArchive(filename)) }
    }

    internal suspend fun handleNavigationEvents(webView: WebView) {
        withContext(Dispatchers.Main) {
            navigationEvents.collect { event ->
                when (event) {
                    is NavigationEvent.Back -> webView.platformGoBack()
                    is NavigationEvent.Forward -> webView.platformGoForward()
                    is NavigationEvent.Reload -> webView.platformReload()
                    is NavigationEvent.StopLoading -> webView.platformStopLoading()
                    is NavigationEvent.LoadUrl -> webView.platformLoadUrl(event.url, event.additionalHttpHeaders)
                    is NavigationEvent.LoadHtml ->
                        webView.platformLoadDataWithBaseURL(
                            event.baseUrl,
                            event.html,
                            event.mimeType,
                            event.encoding,
                            event.historyUrl,
                        )
                    is NavigationEvent.PostUrl -> webView.platformPostUrl(event.url, event.postData)
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
