package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.ConsoleMessage
import com.parkwoocheol.composewebview.PlatformPermissionRequest
import com.parkwoocheol.composewebview.WebView

/**
 * A WebView chrome client implementation that handles UI-related events.
 *
 * This client manages progress updates, console messages, JavaScript dialogs, and more.
 * You can extend this class to provide custom behavior, or use the extension functions
 * for convenient configuration.
 *
 * @see rememberWebChromeClient
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class ComposeWebChromeClient() {
    open fun onProgressChanged(
        view: WebView?,
        newProgress: Int,
    )

    open fun onConsoleMessage(
        view: WebView?,
        message: ConsoleMessage,
    ): Boolean

    // Internal setters for extension functions
    internal fun setOnProgressChangedHandler(handler: (WebView?, Int) -> Unit)

    internal fun setOnConsoleMessageHandler(handler: (WebView?, ConsoleMessage) -> Boolean)

    internal fun setOnPermissionRequestHandler(handler: (PlatformPermissionRequest) -> Unit)
}
