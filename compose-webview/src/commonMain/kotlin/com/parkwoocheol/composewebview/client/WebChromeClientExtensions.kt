package com.parkwoocheol.composewebview.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.parkwoocheol.composewebview.ConsoleMessage
import com.parkwoocheol.composewebview.PlatformPermissionRequest
import com.parkwoocheol.composewebview.WebView

/**
 * Sets a handler to be called when the loading progress changes.
 *
 * This is a convenience extension that allows fluent configuration of the chrome client.
 *
 * @param handler Callback invoked with (view, progress) parameters where progress is 0-100.
 * @return This chrome client instance for chaining.
 */
fun ComposeWebChromeClient.onProgressChanged(handler: (WebView?, Int) -> Unit): ComposeWebChromeClient =
    apply {
        setOnProgressChangedHandler(handler)
    }

/**
 * Sets a handler to be called when the WebView receives a console message from JavaScript.
 *
 * This is a convenience extension that allows fluent configuration of the chrome client.
 *
 * @param handler Callback invoked with (view, message) parameters. Return true to suppress default handling.
 * @return This chrome client instance for chaining.
 */
fun ComposeWebChromeClient.onConsoleMessage(handler: (WebView?, ConsoleMessage) -> Boolean): ComposeWebChromeClient =
    apply {
        setOnConsoleMessageHandler(handler)
    }

/**
 * Sets a handler to be called when the WebView requests a permission.
 *
 * Note: This is platform-specific and may not be available on all platforms.
 *
 * This is a convenience extension that allows fluent configuration of the chrome client.
 *
 * @param handler Callback invoked with the permission request.
 * @return This chrome client instance for chaining.
 */
fun ComposeWebChromeClient.onPermissionRequest(handler: (PlatformPermissionRequest) -> Unit): ComposeWebChromeClient =
    apply {
        setOnPermissionRequestHandler(handler)
    }

/**
 * Remembers a [ComposeWebChromeClient] with optional configuration.
 *
 * This provides a Kotlin-idiomatic way to configure WebView chrome client behavior using
 * extension functions. The client will be remembered across recompositions.
 *
 * Example:
 * ```kotlin
 * val chromeClient = rememberWebChromeClient {
 *     onProgressChanged { view, progress ->
 *         println("Loading: $progress%")
 *     }
 *     onConsoleMessage { view, message ->
 *         println("[Console] ${message.message}")
 *         true // Suppress default console logging
 *     }
 *     onPermissionRequest { request ->
 *         // Handle permission request
 *     }
 * }
 *
 * ComposeWebView(
 *     state = state,
 *     chromeClient = chromeClient
 * )
 * ```
 *
 * For advanced customization requiring full override control, you can
 * still extend [ComposeWebChromeClient] directly:
 * ```kotlin
 * val chromeClient = remember {
 *     object : ComposeWebChromeClient() {
 *         override fun onProgressChanged(view: WebView?, newProgress: Int) {
 *             super.onProgressChanged(view, newProgress)
 *             // Custom logic with full control
 *         }
 *     }
 * }
 * ```
 *
 * @param block Optional configuration block for setting up handlers using extension functions.
 * @return A remembered [ComposeWebChromeClient] instance.
 */
@Composable
fun rememberWebChromeClient(block: (ComposeWebChromeClient.() -> Unit)? = null): ComposeWebChromeClient =
    remember {
        ComposeWebChromeClient().apply {
            block?.invoke(this)
        }
    }
