package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Sealed class for constraining possible loading states.
 *
 * Represents the current state of the WebView's content loading process.
 * This provides a more granular view of the loading lifecycle compared to
 * a simple boolean flag.
 */
sealed class LoadingState {
    /**
     * Describes a WebView that has not yet loaded for the first time.
     */
    data object Initializing : LoadingState()

    /**
     * Describes a webview between `onPageStarted` and `onPageFinished` events, contains a
     * [progress] property which is updated by the webview.
     *
     * @property progress The current loading progress, from 0.0 to 1.0.
     */
    data class Loading(val progress: Float) : LoadingState()

    /**
     * Describes a webview that has finished loading content successfully.
     */
    data object Finished : LoadingState()

    /**
     * Describes a webview that failed to load content due to an error.
     *
     * @property error The error that caused the loading to fail.
     */
    data class Failed(val error: WebViewError) : LoadingState()

    /**
     * Describes a webview whose loading was cancelled by the user or programmatically.
     * This occurs when [WebViewController.stopLoading] is called during a page load.
     */
    data object Cancelled : LoadingState()
}

/**
 * Represents the scroll position of the WebView.
 *
 * **Platform Support:**
 * | Platform | Support | Implementation |
 * |----------|---------|----------------|
 * | Android  | ✅ Full | setOnScrollChangeListener - real-time tracking |
 * | iOS      | ✅ Full | scrollView.contentOffset polling (100ms intervals) |
 * | Desktop  | ❌ Not supported | CEF limitations - requires JavaScript injection |
 * | Web      | ⚠️ Limited | onscroll event - same-origin only (CORS restrictions) |
 *
 * @property x Horizontal scroll position in pixels.
 * @property y Vertical scroll position in pixels.
 */
@Immutable
data class ScrollPosition(
    val x: Int = 0,
    val y: Int = 0,
)

/**
 * State holder for the [ComposeWebView].
 *
 * This class maintains the state of the WebView, including the current URL, loading state,
 * page title, icon, errors, and active dialogs or custom views.
 *
 * @property content The content to be loaded into the WebView.
 */
@Stable
class WebViewState(webContent: WebContent) {
    /**
     * The last URL that was loaded by the WebView.
     */
    var lastLoadedUrl: String? by mutableStateOf(null)
        internal set

    /**
     * The content currently being displayed or to be displayed.
     */
    var content: WebContent by mutableStateOf(webContent)

    /**
     * The current loading state of the WebView.
     *
     * **Platform Support:**
     * - **Initializing**: All platforms
     * - **Loading(progress)**: Android (real-time), iOS (100ms polling), Desktop/Web (limited)
     * - **Finished**: All platforms
     * - **Failed**: Android, iOS, Desktop (partial), Web (limited)
     * - **Cancelled**: All platforms
     *
     * See [LoadingState] for detailed state descriptions.
     */
    var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    /**
     * Whether the WebView is currently loading content.
     */
    val isLoading: Boolean
        get() = loadingState !is LoadingState.Finished

    /**
     * The title of the current page.
     */
    var pageTitle: String? by mutableStateOf(null)
        internal set

    /**
     * The favicon of the current page.
     *
     * **Platform Support:**
     * | Platform | Support | Notes |
     * |----------|---------|-------|
     * | Android  | ✅ Full | WebChromeClient.onReceivedIcon |
     * | iOS      | ⚠️ Limited | Not directly available, requires custom handling |
     * | Desktop  | ⚠️ Limited | KCEF favicon handling limited |
     * | Web      | ⚠️ Limited | CORS restrictions apply |
     */
    var pageIcon: PlatformBitmap? by mutableStateOf(null)

        internal set

    /**
     * A list of errors encountered during the current request.
     */
    var errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()
        internal set

    /**
     * The state of any active JavaScript dialog (Alert, Confirm, Prompt).
     * Null if no dialog is active.
     *
     * **Platform Support:**
     * | Platform | Support | Notes |
     * |----------|---------|-------|
     * | Android  | ✅ Full | WebChromeClient callbacks |
     * | iOS      | ✅ Full | WKUIDelegate callbacks |
     * | Desktop  | ❌ Not supported | KCEF dialog handling different |
     * | Web      | ❌ Not supported | Browser handles dialogs natively |
     */
    var jsDialogState: JsDialogState? by mutableStateOf(null)
        internal set

    /**
     * The state of any active custom view (e.g., fullscreen video).
     * Null if no custom view is active.
     *
     * **Platform Support:**
     * | Platform | Support | Notes |
     * |----------|---------|-------|
     * | Android  | ✅ Full | WebChromeClient.onShowCustomView |
     * | iOS      | ⚠️ Limited | Emits state when WKWebView enters native fullscreen |
     * | Desktop  | ❌ Not supported | KCEF handles fullscreen separately |
     * | Web      | ❌ Not supported | Browser handles fullscreen natively |
     */
    var customViewState: CustomViewState? by mutableStateOf(null)
        internal set

    /**
     * The current scroll position of the WebView.
     *
     * Note: Not all platforms support real-time scroll position tracking.
     * Refer to [ScrollPosition] documentation for platform support details.
     */
    var scrollPosition: ScrollPosition by mutableStateOf(ScrollPosition())
        internal set

    /**
     * The underlying [WebView] instance.
     */
    var webView: WebView? by mutableStateOf(null)
        internal set

    /**
     * The saved state bundle of the WebView, used for restoration.
     */
    var bundle: PlatformBundle? = null

        internal set
}

/**
 * Represents the state of a JavaScript dialog request.
 */
sealed class JsDialogState {
    /**
     * Represents a JavaScript Alert dialog.
     * @property message The message to display.
     * @property callback The callback to invoke when the alert is dismissed.
     */
    data class Alert(val message: String, val callback: () -> Unit) : JsDialogState()

    /**
     * Represents a JavaScript Confirm dialog.
     * @property message The message to display.
     * @property callback The callback to invoke with the user's choice (true for confirmed, false for cancelled).
     */
    data class Confirm(val message: String, val callback: (Boolean) -> Unit) : JsDialogState()

    /**
     * Represents a JavaScript Prompt dialog.
     * @property message The message to display.
     * @property defaultValue The default value for the prompt input.
     * @property callback The callback to invoke with the user's input (or null if cancelled).
     */
    data class Prompt(val message: String, val defaultValue: String, val callback: (String?) -> Unit) : JsDialogState()
}

/**
 * Represents the state of a custom view (e.g., fullscreen video) requested by the WebView.
 *
 * @property view The view to be displayed.
 * @property callback The callback to invoke when the custom view is dismissed.
 */
data class CustomViewState(
    val view: PlatformCustomView,
    val callback: PlatformCustomViewCallback,
)

/**
 * Creates and remembers a [WebViewState] for a specific URL.
 * This state is transient and will be lost on configuration changes.
 * Use [rememberSaveableWebViewState] for state that survives configuration changes.
 *
 * @param url The initial URL to load.
 * @param additionalHttpHeaders Optional additional HTTP headers.
 * @return A [WebViewState] instance.
 */
@Composable
fun rememberWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap(),
): WebViewState =
    remember {
        WebViewState(
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders,
            ),
        )
    }

/**
 * Creates and remembers a [WebViewState] for a specific URL.
 * This state survives configuration changes (saved in Bundle).
 *
 * @param url The initial URL to load.
 * @param additionalHttpHeaders Optional additional HTTP headers.
 * @return A [WebViewState] instance that survives configuration changes.
 */
@Composable
fun rememberSaveableWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap(),
): WebViewState =
    rememberSaveable(saver = WebViewStateSaver) {
        WebViewState(
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders,
            ),
        )
    }

/**
 * Creates and remembers a [WebViewState] for raw data (HTML).
 * This state is transient and will be lost on configuration changes.
 * Use [rememberSaveableWebViewStateWithData] for state that survives configuration changes.
 *
 * @param data The data (HTML) to load.
 * @param baseUrl The base URL.
 * @param encoding The encoding of the data.
 * @param mimeType The MIME type of the data.
 * @param historyUrl The history URL.
 * @return A [WebViewState] instance.
 */
@Composable
fun rememberWebViewStateWithData(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String? = null,
    historyUrl: String? = null,
): WebViewState =
    remember {
        WebViewState(WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl))
    }

/**
 * Creates and remembers a [WebViewState] for raw data (HTML).
 * This state survives configuration changes (saved in Bundle).
 *
 * @param data The data (HTML) to load.
 * @param baseUrl The base URL.
 * @param encoding The encoding of the data.
 * @param mimeType The MIME type of the data.
 * @param historyUrl The history URL.
 * @return A [WebViewState] instance that survives configuration changes.
 */
@Composable
fun rememberSaveableWebViewStateWithData(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String? = null,
    historyUrl: String? = null,
): WebViewState =
    rememberSaveable(saver = WebViewStateSaver) {
        WebViewState(WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl))
    }

/**
 * A [Saver] for [WebViewState] to handle saving and restoring state across configuration changes.
 */
val WebViewStateSaver: Saver<WebViewState, Any> =
    run {
        val pageTitleKey = "pagetitle"
        val lastLoadedUrlKey = "lastloaded"
        val stateBundleKey = "bundle"
        val contentKey = "content"

        mapSaver(
            save = { state ->
                val bundle = createPlatformBundle()
                state.webView?.platformSaveState(bundle)

                mapOf(
                    pageTitleKey to state.pageTitle,
                    lastLoadedUrlKey to state.lastLoadedUrl,
                    stateBundleKey to bundle,
                    contentKey to state.content.toSaveableContent(),
                )
            },
            restore = { map ->
                val restoredContent = map[contentKey].toRestoredWebContent() ?: WebContent.NavigatorOnly
                val webViewState = WebViewState(restoredContent)
                webViewState.pageTitle = map[pageTitleKey] as String?
                webViewState.lastLoadedUrl = map[lastLoadedUrlKey] as String?
                webViewState.bundle = map[stateBundleKey] as PlatformBundle?
                webViewState
            },
        )
    }

private fun WebContent.toSaveableContent(): Map<String, Any?> =
    when (this) {
        is WebContent.Url ->
            mapOf(
                "type" to "url",
                "url" to url,
                "headers" to additionalHttpHeaders,
            )
        is WebContent.Data ->
            mapOf(
                "type" to "data",
                "data" to data,
                "baseUrl" to baseUrl,
                "encoding" to encoding,
                "mimeType" to mimeType,
                "historyUrl" to historyUrl,
            )
        is WebContent.Post ->
            mapOf(
                "type" to "post",
                "url" to url,
                "postData" to postData,
            )
        WebContent.NavigatorOnly ->
            mapOf("type" to "navigator")
    }

@Suppress("UNCHECKED_CAST")
private fun Any?.toRestoredWebContent(): WebContent? {
    val contentMap = this as? Map<String, Any?> ?: return null
    return when (contentMap["type"] as? String) {
        "url" ->
            WebContent.Url(
                url = contentMap["url"] as? String ?: "",
                additionalHttpHeaders = contentMap["headers"].toStringMap(),
            )
        "data" ->
            WebContent.Data(
                data = contentMap["data"] as? String ?: "",
                baseUrl = contentMap["baseUrl"] as? String?,
                encoding = contentMap["encoding"] as? String ?: "utf-8",
                mimeType = contentMap["mimeType"] as? String?,
                historyUrl = contentMap["historyUrl"] as? String?,
            )
        "post" ->
            WebContent.Post(
                url = contentMap["url"] as? String ?: "",
                postData = (contentMap["postData"] as? ByteArray) ?: ByteArray(0),
            )
        "navigator" -> WebContent.NavigatorOnly
        else -> null
    }
}

private fun Any?.toStringMap(): Map<String, String> {
    val map = this as? Map<*, *> ?: return emptyMap()
    val result = mutableMapOf<String, String>()
    map.forEach { (key, value) ->
        val stringKey = key as? String ?: return@forEach
        val stringValue = value as? String ?: return@forEach
        result[stringKey] = stringValue
    }
    return result
}
