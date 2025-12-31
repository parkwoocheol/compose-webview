package com.parkwoocheol.composewebview.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.parkwoocheol.composewebview.PlatformBitmap
import com.parkwoocheol.composewebview.PlatformWebResourceError
import com.parkwoocheol.composewebview.PlatformWebResourceRequest
import com.parkwoocheol.composewebview.WebView

/**
 * Sets a handler to be called when a page starts loading.
 *
 * This is a convenience extension that allows fluent configuration of the client.
 *
 * @param handler Callback invoked with (view, url, favicon) parameters.
 * @return This client instance for chaining.
 */
fun ComposeWebViewClient.onPageStarted(handler: (WebView?, String?, PlatformBitmap?) -> Unit): ComposeWebViewClient =
    apply {
        setOnPageStartedHandler(handler)
    }

/**
 * Sets a handler to be called when a page finishes loading.
 *
 * This is a convenience extension that allows fluent configuration of the client.
 *
 * @param handler Callback invoked with (view, url) parameters.
 * @return This client instance for chaining.
 */
fun ComposeWebViewClient.onPageFinished(handler: (WebView?, String?) -> Unit): ComposeWebViewClient =
    apply {
        setOnPageFinishedHandler(handler)
    }

/**
 * Sets a handler to be called when an error occurs while loading a resource.
 *
 * This is a convenience extension that allows fluent configuration of the client.
 *
 * @param handler Callback invoked with (view, request, error) parameters.
 * @return This client instance for chaining.
 */
fun ComposeWebViewClient.onReceivedError(
    handler: (WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit,
): ComposeWebViewClient =
    apply {
        setOnReceivedErrorHandler(handler)
    }

/**
 * Sets a handler to control URL loading behavior.
 *
 * Return true from the handler to cancel the current load, false to continue.
 *
 * This is a convenience extension that allows fluent configuration of the client.
 *
 * @param handler Callback that returns true to override URL loading, false otherwise.
 * @return This client instance for chaining.
 */
fun ComposeWebViewClient.shouldOverrideUrlLoading(handler: (WebView?, PlatformWebResourceRequest?) -> Boolean): ComposeWebViewClient =
    apply {
        setShouldOverrideUrlLoadingHandler(handler)
    }

/**
 * Remembers a [ComposeWebViewClient] with optional configuration.
 *
 * This provides a Kotlin-idiomatic way to configure WebView client behavior using
 * extension functions. The client will be remembered across recompositions.
 *
 * Example:
 * ```kotlin
 * val client = rememberWebViewClient {
 *     onPageStarted { view, url, favicon ->
 *         println("Page started: $url")
 *     }
 *     onPageFinished { view, url ->
 *         println("Page finished: $url")
 *     }
 *     onReceivedError { view, request, error ->
 *         println("Error: ${error?.description}")
 *     }
 *     shouldOverrideUrlLoading { view, request ->
 *         request?.url?.startsWith("myapp://") == true
 *     }
 * }
 *
 * ComposeWebView(
 *     state = state,
 *     client = client
 * )
 * ```
 *
 * For advanced customization requiring full override control, you can
 * still extend [ComposeWebViewClient] directly:
 * ```kotlin
 * val client = remember {
 *     object : ComposeWebViewClient() {
 *         override fun onPageFinished(view: WebView?, url: String?) {
 *             super.onPageFinished(view, url)
 *             // Custom logic with full control
 *         }
 *     }
 * }
 * ```
 *
 * @param block Optional configuration block for setting up handlers using extension functions.
 * @return A remembered [ComposeWebViewClient] instance.
 */
@Composable
fun rememberWebViewClient(block: (ComposeWebViewClient.() -> Unit)? = null): ComposeWebViewClient =
    remember {
        ComposeWebViewClient().apply {
            block?.invoke(this)
        }
    }
