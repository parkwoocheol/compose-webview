package com.parkwoocheol.composewebview

/**
 * Describes which bridge transport delivered a JavaScript call.
 */
enum class BridgeTransport {
    /**
     * Compatibility transport backed by the platform's classic bridge mechanism.
     */
    Compatibility,

    /**
     * Origin-aware transport backed by `addWebMessageListener`.
     */
    OriginAwareListener,

    /**
     * Dedicated message-channel transport backed by transferable ports.
     */
    MessageChannel,
}

/**
 * Metadata about the JavaScript frame that invoked a bridge handler.
 *
 * @property sourceOrigin Origin string reported by the platform, or `null` when unavailable.
 * @property isMainFrame Whether the call originated from the main frame.
 * @property transport Which transport delivered this invocation.
 */
data class BridgeInvocationContext(
    val sourceOrigin: String?,
    val isMainFrame: Boolean,
    val transport: BridgeTransport,
)

/**
 * Runtime bridge capabilities exposed by the current platform transport.
 *
 * These values are transport-dependent and may change after a bridge attaches to a platform WebView.
 *
 * @property supportsOriginAwareInbound Whether inbound calls include origin/frame metadata from the platform.
 * @property supportsBinaryMessages Whether binary bridge messaging is available.
 * @property supportsPersistentReplyChannel Whether a frame-bound reply channel can stay active after a handler returns.
 * @property supportsMainFrameOutboundMessaging Whether the bridge can post messages to the main frame without
 * evaluating JavaScript strings.
 */
data class BridgeCapabilities(
    val supportsOriginAwareInbound: Boolean = false,
    val supportsBinaryMessages: Boolean = false,
    val supportsPersistentReplyChannel: Boolean = false,
    val supportsMainFrameOutboundMessaging: Boolean = false,
)

/**
 * Marks Android bridge APIs whose transport contract may evolve in future minor releases.
 */
@RequiresOptIn(
    message = "This Compose WebView API is experimental and may change without notice.",
    level = RequiresOptIn.Level.ERROR,
)
annotation class ExperimentalComposeWebViewApi
