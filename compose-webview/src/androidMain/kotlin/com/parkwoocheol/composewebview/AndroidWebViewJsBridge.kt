package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Policy for the Android origin-aware JS bridge.
 */
enum class AndroidJsBridgePolicy {
    /**
     * Require the origin-aware bridge path. If it cannot be installed, the bridge fails.
     */
    OriginAwareOnly,

    /**
     * Prefer the origin-aware bridge path, but allow a compatibility fallback for unsupported pages or providers.
     *
     * Compatibility fallback restores `addJavascriptInterface`-style exposure for pages that do not receive the
     * origin-aware bridge. Use this only when your app already accepts the weaker trust model for those pages.
     */
    Compatible,
}

/**
 * Configuration for the Android origin-aware JS bridge.
 *
 * @property allowedOriginRules Allowed origin rules forwarded to `addWebMessageListener` and
 * `addDocumentStartJavaScript`.
 * @property policy Controls whether the bridge must stay on the origin-aware path or may fall back to compatibility mode.
 * @property allowIframes Whether calls from iframes are allowed on the origin-aware path.
 * @property enableBinaryMessages Whether ArrayBuffer messaging is enabled for the experimental Android APIs.
 */
data class AndroidWebViewJsBridgeConfig(
    val allowedOriginRules: Set<String>,
    val policy: AndroidJsBridgePolicy = AndroidJsBridgePolicy.OriginAwareOnly,
    val allowIframes: Boolean = false,
    val enableBinaryMessages: Boolean = false,
) {
    init {
        require(allowedOriginRules.isNotEmpty()) {
            "allowedOriginRules must not be empty for rememberAndroidWebViewJsBridge()."
        }
    }
}

/**
 * Remembers an origin-aware Android JS bridge backed by WebView message APIs when available.
 *
 * This bridge keeps the existing `window.AppBridge.call(...)` model for documents matched by
 * [AndroidWebViewJsBridgeConfig.allowedOriginRules], while optionally falling back to a compatibility bridge for
 * unsupported origins when [AndroidWebViewJsBridgeConfig.policy] is
 * [AndroidJsBridgePolicy.Compatible]. The compatibility path is intended for migration and unsupported providers,
 * not for pages that need origin-aware trust guarantees.
 *
 * @param serializer The serializer used for typed bridge calls.
 * @param jsObjectName The JavaScript bridge object name. Defaults to `AppBridge`.
 * @param nativeInterfaceName The injected native bridge object name. Defaults to `AppBridgeNative`.
 * @param config Android-specific origin-aware bridge configuration.
 */
@Composable
fun rememberAndroidWebViewJsBridge(
    serializer: BridgeSerializer? = null,
    jsObjectName: String = "AppBridge",
    nativeInterfaceName: String = "AppBridgeNative",
    config: AndroidWebViewJsBridgeConfig,
): WebViewJsBridge {
    val bridge =
        remember(serializer, jsObjectName, nativeInterfaceName, config) {
            WebViewJsBridge(
                serializer = serializer,
                jsObjectName = jsObjectName,
                nativeInterfaceName = nativeInterfaceName,
            ).also {
                it.setRuntime(AndroidOriginAwareWebViewJsBridgeRuntime(config))
            }
        }

    DisposableEffect(bridge) {
        onDispose {
            bridge.dispose()
        }
    }

    return bridge
}

/**
 * A string or binary message delivered by the Android origin-aware bridge APIs.
 */
@ExperimentalComposeWebViewApi
sealed interface AndroidBridgeMessage {
    /**
     * String payload.
     */
    data class Text(val value: String) : AndroidBridgeMessage

    /**
     * Binary payload.
     */
    data class Binary(val value: ByteArray) : AndroidBridgeMessage
}

/**
 * A frame-bound reply channel exposed by `addWebMessageListener`.
 */
@ExperimentalComposeWebViewApi
interface AndroidBridgeReplyChannel {
    /**
     * Whether the reply channel still targets a live frame.
     */
    val isActive: Boolean

    /**
     * Sends a message to the frame that created this reply channel.
     */
    suspend fun send(message: AndroidBridgeMessage)

    /**
     * Sends a string message to the frame that created this reply channel.
     */
    suspend fun sendText(value: String)

    /**
     * Sends a binary message to the frame that created this reply channel.
     */
    suspend fun sendBytes(value: ByteArray)
}

/**
 * Context passed to experimental Android message handlers.
 *
 * @property sourceOrigin The reported source origin, or `null` when unavailable.
 * @property isMainFrame Whether the message came from the main frame.
 * @property transport Which bridge transport delivered the message.
 * @property replyChannel A persistent reply channel bound to the originating frame.
 */
@ExperimentalComposeWebViewApi
data class AndroidBridgeMessageContext(
    val sourceOrigin: String?,
    val isMainFrame: Boolean,
    val transport: BridgeTransport,
    val replyChannel: AndroidBridgeReplyChannel?,
)

/**
 * A native-side view of an Android WebMessageChannel session.
 */
@ExperimentalComposeWebViewApi
interface AndroidBridgeSession {
    /**
     * The target origin used when the session was opened.
     */
    val targetOrigin: String

    /**
     * Always `true` for sessions opened through `postWebMessage`.
     */
    val isMainFrame: Boolean

    /**
     * Whether the session is still usable.
     */
    val isActive: Boolean

    /**
     * Registers a callback for messages posted from JavaScript on this session.
     */
    fun setMessageHandler(handler: ((AndroidBridgeMessage) -> Unit)?)

    /**
     * Sends a message on the session port.
     */
    suspend fun send(message: AndroidBridgeMessage)

    /**
     * Sends a string message on the session port.
     */
    suspend fun sendText(value: String)

    /**
     * Sends a binary message on the session port.
     */
    suspend fun sendBytes(value: ByteArray)

    /**
     * Closes the session.
     */
    fun close()
}

/**
 * Registers an experimental string/binary message handler for the origin-aware Android bridge.
 *
 * The handler is only invoked when the bridge is created via [rememberAndroidWebViewJsBridge].
 */
@ExperimentalComposeWebViewApi
fun WebViewJsBridge.registerMessage(
    method: String,
    handler: suspend AndroidBridgeMessageContext.(AndroidBridgeMessage) -> AndroidBridgeMessage?,
) {
    androidOriginAwareRuntime().registerMessage(method, handler)
}

/**
 * Unregisters an experimental string/binary message handler from the origin-aware Android bridge.
 */
@ExperimentalComposeWebViewApi
fun WebViewJsBridge.unregisterMessage(method: String) {
    androidOriginAwareRuntime().unregisterMessage(method)
}

/**
 * Posts a raw string or binary message to the main frame using Android's `postWebMessage`.
 *
 * The JavaScript page can receive this message through `window.AppBridge.onMessage(...)` or a regular `message`
 * event listener. This API is main-frame focused and maps directly to Android's outbound WebMessage APIs.
 */
@ExperimentalComposeWebViewApi
fun WebViewJsBridge.postMainFrameMessage(
    targetOrigin: String,
    message: AndroidBridgeMessage,
) {
    androidOriginAwareRuntime().postMainFrameMessage(this, targetOrigin, message)
}

/**
 * Opens an experimental `WebMessageChannel` session with the allowed main-frame target.
 *
 * JavaScript can receive newly opened sessions through `window.AppBridge.onSession(...)`.
 */
@ExperimentalComposeWebViewApi
fun WebViewJsBridge.openMainFrameSession(targetOrigin: String): AndroidBridgeSession? =
    androidOriginAwareRuntime().openSession(this, targetOrigin)

internal fun WebViewJsBridge.androidOriginAwareRuntime(): AndroidOriginAwareWebViewJsBridgeRuntime =
    runtime as? AndroidOriginAwareWebViewJsBridgeRuntime
        ?: error("Android origin-aware bridge APIs require rememberAndroidWebViewJsBridge().")
