package com.parkwoocheol.composewebview

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Sealed class for constraining possible loading states.
 * See [Loading] and [Finished].
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
     * Describes a webview that has finished loading content.
     */
    data object Finished : LoadingState()
}

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
     */
    var pageIcon: Bitmap? by mutableStateOf(null)
        internal set

    /**
     * A list of errors encountered during the current request.
     */
    var errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()
        internal set

    /**
     * The state of any active JavaScript dialog (Alert, Confirm, Prompt).
     * Null if no dialog is active.
     */
    var jsDialogState: JsDialogState? by mutableStateOf(null)
        internal set

    /**
     * The state of any active custom view (e.g., fullscreen video).
     * Null if no custom view is active.
     */
    var customViewState: CustomViewState? by mutableStateOf(null)
        internal set
    
    /**
     * The underlying [WebView] instance.
     */
    var webView: WebView? by mutableStateOf(null)
        internal set
        
    /**
     * The saved state bundle of the WebView, used for restoration.
     */
    var bundle: Bundle? = null
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
    val view: android.view.View,
    val callback: android.webkit.WebChromeClient.CustomViewCallback
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
fun rememberWebViewState(url: String, additionalHttpHeaders: Map<String, String> = emptyMap()): WebViewState =
    remember {
        WebViewState(
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders
            )
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
fun rememberSaveableWebViewState(url: String, additionalHttpHeaders: Map<String, String> = emptyMap()): WebViewState =
    rememberSaveable(saver = WebViewStateSaver) {
        WebViewState(
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders
            )
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
    historyUrl: String? = null
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
    historyUrl: String? = null
): WebViewState =
    rememberSaveable(saver = WebViewStateSaver) {
        WebViewState(WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl))
    }

/**
 * A [Saver] for [WebViewState] to handle saving and restoring state across configuration changes.
 */
val WebViewStateSaver: Saver<WebViewState, Any> = run {
    val pageTitleKey = "pagetitle"
    val lastLoadedUrlKey = "lastloaded"
    val stateBundleKey = "bundle"

    mapSaver(
        save = { state ->
            val bundle = Bundle()
            state.webView?.saveState(bundle)
            mapOf(
                pageTitleKey to state.pageTitle,
                lastLoadedUrlKey to state.lastLoadedUrl,
                stateBundleKey to bundle
            )
        },
        restore = { map ->
            val webViewState = WebViewState(WebContent.NavigatorOnly)
            webViewState.pageTitle = map[pageTitleKey] as String?
            webViewState.lastLoadedUrl = map[lastLoadedUrlKey] as String?
            webViewState.bundle = map[stateBundleKey] as Bundle?
            webViewState
        }
    )
}
