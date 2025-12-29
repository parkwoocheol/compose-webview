package com.parkwoocheol.composewebview

import androidx.compose.runtime.Immutable

/**
 * Represents the severity level of a console message.
 */
enum class ConsoleMessageLevel {
    /**
     * Informational message.
     */
    LOG,

    /**
     * Debug message.
     */
    DEBUG,

    /**
     * Warning message.
     */
    WARNING,

    /**
     * Error message.
     */
    ERROR,

    /**
     * Tip message (primarily for iOS).
     */
    TIP,
}

/**
 * Represents a console message from the WebView's JavaScript context.
 *
 * This class provides a platform-agnostic way to capture console output
 * from web pages, useful for debugging and monitoring JavaScript execution.
 *
 * **Platform Support:**
 * | Platform | Support | Notes |
 * |----------|---------|-------|
 * | Android  | ✅ Full | WebChromeClient.onConsoleMessage |
 * | iOS      | ✅ Full | WKUserContentController script message handler |
 * | Desktop  | ⚠️ Partial | CEF console handler (requires setup) |
 * | Web      | ❌ Limited | Cannot intercept iframe console (CORS) |
 *
 * @property message The console message text.
 * @property sourceId The source file or URL where the message originated.
 * @property lineNumber The line number in the source file (0 if unknown).
 * @property level The severity level of the message.
 */
@Immutable
data class ConsoleMessage(
    val message: String,
    val sourceId: String = "",
    val lineNumber: Int = 0,
    val level: ConsoleMessageLevel = ConsoleMessageLevel.LOG,
)
