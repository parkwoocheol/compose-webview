@file:OptIn(ExperimentalComposeWebViewApi::class)

package com.parkwoocheol.composewebview

import android.net.Uri
import android.webkit.WebView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.ScriptHandler
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebMessagePortCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal class AndroidOriginAwareWebViewJsBridgeRuntime(
    private val config: AndroidWebViewJsBridgeConfig,
) : WebViewJsBridgeRuntime {
    private val json = Json { ignoreUnknownKeys = true }
    private val compatibilityInterfaceName = "cwvCompat_${hashCode().toUInt().toString(16)}"

    private var attachedWebView: WebView? = null
    private var listenerInstalled: Boolean = false
    private var documentStartHandler: ScriptHandler? = null
    private val pendingBinaryCalls = mutableMapOf<JavaScriptReplyProxy, PendingBinaryCall>()
    private val activeReplyChannels = linkedSetOf<AndroidBridgeReplyChannelImpl>()
    private val activeSessions = linkedSetOf<AndroidBridgeSessionImpl>()
    private val messageHandlers =
        mutableMapOf<String, suspend AndroidBridgeMessageContext.(AndroidBridgeMessage) -> AndroidBridgeMessage?>()

    private var currentCapabilities: BridgeCapabilities = BridgeCapabilities()

    override val capabilities: BridgeCapabilities
        get() = currentCapabilities

    override fun attach(
        bridge: WebViewJsBridge,
        webView: WebView,
    ) {
        if (attachedWebView === webView) {
            bridge.webView = webView
            return
        }

        detachCurrent(bridge)

        attachedWebView = webView
        bridge.webView = webView

        if (config.policy == AndroidJsBridgePolicy.Compatible) {
            webView.platformAddJavascriptInterface(bridge, compatibilityInterfaceName(bridge))
        }

        val originAwareSupported = supportsOriginAwareFoundation()
        if (!originAwareSupported) {
            if (config.policy == AndroidJsBridgePolicy.OriginAwareOnly) {
                error("Origin-aware Android JS bridge requires WEB_MESSAGE_LISTENER and DOCUMENT_START_SCRIPT support.")
            }
            installCompatibilityCapabilities()
            return
        }

        WebViewCompat.addWebMessageListener(
            webView,
            bridge.nativeInterfaceName,
            config.allowedOriginRules,
            object : WebViewCompat.WebMessageListener {
                override fun onPostMessage(
                    view: WebView,
                    message: WebMessageCompat,
                    sourceOrigin: Uri,
                    isMainFrame: Boolean,
                    replyProxy: JavaScriptReplyProxy,
                ) {
                    handleOriginAwareMessage(
                        bridge = bridge,
                        message = message,
                        sourceOrigin = normalizeOrigin(sourceOrigin),
                        isMainFrame = isMainFrame,
                        replyProxy = replyProxy,
                    )
                }
            },
        )
        listenerInstalled = true
        documentStartHandler =
            WebViewCompat.addDocumentStartJavaScript(
                webView,
                buildOriginAwareBootstrapScript(bridge),
                config.allowedOriginRules,
            )
        currentCapabilities =
            BridgeCapabilities(
                supportsOriginAwareInbound = true,
                supportsBinaryMessages = config.enableBinaryMessages && supportsBinaryMessages(),
                supportsPersistentReplyChannel = true,
                supportsMainFrameOutboundMessaging = supportsMainFrameOutboundMessaging(),
            )
    }

    override fun onPageStarted(bridge: WebViewJsBridge) {
        invalidateTransientState()
    }

    override fun pageFinishedBootstrapScript(bridge: WebViewJsBridge): String? =
        if (config.policy == AndroidJsBridgePolicy.Compatible) {
            buildOriginAwareBootstrapScript(bridge) +
                "\n" +
                buildCompatibilityBootstrapScript(
                    jsObjectName = bridge.jsObjectName,
                    nativeInterfaceName = compatibilityInterfaceName(bridge),
                )
        } else {
            buildOriginAwareBootstrapScript(bridge)
        }

    override fun emit(
        bridge: WebViewJsBridge,
        event: String,
        payloadJson: String,
    ) {
        val script = "window.${bridge.jsObjectName}.trigger('$event', $payloadJson);"
        bridge.webView?.platformEvaluateJavascript(script, null)
    }

    override fun dispose(bridge: WebViewJsBridge) {
        detachCurrent(bridge)
        messageHandlers.clear()
    }

    fun registerMessage(
        method: String,
        handler: suspend AndroidBridgeMessageContext.(AndroidBridgeMessage) -> AndroidBridgeMessage?,
    ) {
        messageHandlers[method] = handler
    }

    fun unregisterMessage(method: String) {
        messageHandlers.remove(method)
    }

    fun postMainFrameMessage(
        bridge: WebViewJsBridge,
        targetOrigin: String,
        message: AndroidBridgeMessage,
    ) {
        ensureOriginAwareOutboundAvailable()
        val webView = attachedWebView ?: error("Bridge is not attached to an Android WebView.")
        val targetUri = parseTargetOrigin(targetOrigin)
        WebViewCompat.postWebMessage(webView, message.toWebMessageCompat(config.enableBinaryMessages), targetUri)
    }

    fun openSession(
        bridge: WebViewJsBridge,
        targetOrigin: String,
    ): AndroidBridgeSession? {
        ensureOriginAwareOutboundAvailable()
        ensurePortFeatures()

        val webView = attachedWebView ?: return null
        val targetUri = parseTargetOrigin(targetOrigin)
        val ports = WebViewCompat.createWebMessageChannel(webView)
        val nativePort = ports[0]
        val jsPort = ports[1]

        val session =
            AndroidBridgeSessionImpl(
                targetOrigin = targetOrigin,
                port = nativePort,
                binaryEnabled = config.enableBinaryMessages && supportsBinaryMessages(),
                onClose = { activeSessions.remove(it) },
            )
        activeSessions += session
        session.installMessageCallback()

        val bootstrapMessage = WebMessageCompat(SESSION_BOOTSTRAP_TOKEN, arrayOf(jsPort))
        WebViewCompat.postWebMessage(webView, bootstrapMessage, targetUri)
        return session
    }

    private fun detachCurrent(bridge: WebViewJsBridge) {
        val webView = attachedWebView ?: return

        invalidateTransientState()

        documentStartHandler?.remove()
        documentStartHandler = null

        if (listenerInstalled && WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.removeWebMessageListener(webView, bridge.nativeInterfaceName)
        }
        if (config.policy == AndroidJsBridgePolicy.Compatible) {
            webView.removeJavascriptInterface(compatibilityInterfaceName(bridge))
        }
        listenerInstalled = false
        attachedWebView = null
        currentCapabilities = BridgeCapabilities()
    }

    private fun invalidateTransientState() {
        activeReplyChannels.toList().forEach { it.invalidate() }
        activeReplyChannels.clear()

        activeSessions.toList().forEach { it.close() }
        activeSessions.clear()

        pendingBinaryCalls.clear()
    }

    private fun handleOriginAwareMessage(
        bridge: WebViewJsBridge,
        message: WebMessageCompat,
        sourceOrigin: String?,
        isMainFrame: Boolean,
        replyProxy: JavaScriptReplyProxy,
    ) {
        if (!config.allowIframes && !isMainFrame) {
            replyWithError(replyProxy, callbackId = null, "Bridge calls from iframes are disabled.")
            return
        }

        val context =
            BridgeInvocationContext(
                sourceOrigin = sourceOrigin,
                isMainFrame = isMainFrame,
                transport = BridgeTransport.OriginAwareListener,
            )
        val replyChannel = AndroidBridgeReplyChannelImpl(replyProxy, config.enableBinaryMessages && supportsBinaryMessages())
        activeReplyChannels += replyChannel

        when (message.type) {
            WebMessageCompat.TYPE_STRING -> handleStringMessage(bridge, message.data, context, replyChannel, replyProxy)
            WebMessageCompat.TYPE_ARRAY_BUFFER -> handleBinaryMessage(message.arrayBuffer, replyProxy)
        }
    }

    private fun handleStringMessage(
        bridge: WebViewJsBridge,
        rawMessage: String?,
        context: BridgeInvocationContext,
        replyChannel: AndroidBridgeReplyChannelImpl,
        replyProxy: JavaScriptReplyProxy,
    ) {
        val raw = rawMessage ?: return
        val envelope = parseEnvelope(raw) ?: return
        val kind = envelope["kind"]?.jsonPrimitive?.contentOrNull ?: return

        when (kind) {
            KIND_TYPED_CALL -> {
                val method = envelope["method"]?.jsonPrimitive?.contentOrNull ?: return
                val callbackId = envelope["callbackId"]?.jsonPrimitive?.contentOrNull
                val data = envelope["data"]?.let(::jsonElementToRawString)

                bridge.scope.launch {
                    try {
                        val resultJson = bridge.invokeHandler(method, data, context)
                        callbackId?.let { replyWithTypedSuccess(replyProxy, it, resultJson) }
                    } catch (t: Throwable) {
                        callbackId?.let { replyWithError(replyProxy, it, t.message ?: "Unknown error") }
                    }
                }
            }

            KIND_MESSAGE_CALL -> {
                val method = envelope["method"]?.jsonPrimitive?.contentOrNull ?: return
                val callbackId = envelope["callbackId"]?.jsonPrimitive?.contentOrNull ?: return
                val value = envelope["value"]?.jsonPrimitive?.content ?: ""
                handleExperimentalMessageCall(
                    method = method,
                    payload = AndroidBridgeMessage.Text(value),
                    callbackId = callbackId,
                    context = context,
                    replyChannel = replyChannel,
                    replyProxy = replyProxy,
                )
            }

            KIND_BINARY_META -> {
                val method = envelope["method"]?.jsonPrimitive?.contentOrNull ?: return
                val callbackId = envelope["callbackId"]?.jsonPrimitive?.contentOrNull ?: return
                pendingBinaryCalls[replyProxy] =
                    PendingBinaryCall(
                        method = method,
                        callbackId = callbackId,
                        context = context,
                        replyChannel = replyChannel,
                    )
            }
        }
    }

    private fun handleBinaryMessage(
        payload: ByteArray,
        replyProxy: JavaScriptReplyProxy,
    ) {
        if (!config.enableBinaryMessages || !supportsBinaryMessages()) {
            replyWithError(replyProxy, null, "Binary bridge messaging is disabled or unsupported.")
            return
        }

        val pending = pendingBinaryCalls.remove(replyProxy)
        if (pending == null) {
            replyWithError(replyProxy, null, "Received unexpected binary bridge message without metadata.")
            return
        }

        handleExperimentalMessageCall(
            method = pending.method,
            payload = AndroidBridgeMessage.Binary(payload),
            callbackId = pending.callbackId,
            context = pending.context,
            replyChannel = pending.replyChannel,
            replyProxy = replyProxy,
        )
    }

    private fun handleExperimentalMessageCall(
        method: String,
        payload: AndroidBridgeMessage,
        callbackId: String,
        context: BridgeInvocationContext,
        replyChannel: AndroidBridgeReplyChannelImpl,
        replyProxy: JavaScriptReplyProxy,
    ) {
        val handler = messageHandlers[method]
        if (handler == null) {
            replyWithError(replyProxy, callbackId, "No message handler found for method: $method")
            return
        }

        val messageContext =
            AndroidBridgeMessageContext(
                sourceOrigin = context.sourceOrigin,
                isMainFrame = context.isMainFrame,
                transport = context.transport,
                replyChannel = replyChannel,
            )

        replyChannel.scope.launch {
            try {
                when (val result = handler(messageContext, payload)) {
                    null -> replyWithTextSuccess(replyProxy, callbackId, null)
                    is AndroidBridgeMessage.Text -> replyWithTextSuccess(replyProxy, callbackId, result.value)
                    is AndroidBridgeMessage.Binary -> replyWithBinarySuccess(replyProxy, callbackId, result.value)
                }
            } catch (t: Throwable) {
                replyWithError(replyProxy, callbackId, t.message ?: "Unknown error")
            }
        }
    }

    private fun replyWithTypedSuccess(
        replyProxy: JavaScriptReplyProxy,
        callbackId: String,
        resultJson: String?,
    ) {
        val envelope =
            buildJsonObject {
                put("__composeWebView", true)
                put("kind", KIND_SUCCESS)
                put("callbackId", callbackId)
                put("result", parseJsonElement(resultJson))
            }
        replyProxy.postMessage(envelope.toString())
    }

    private fun replyWithTextSuccess(
        replyProxy: JavaScriptReplyProxy,
        callbackId: String,
        value: String?,
    ) {
        val envelope =
            buildJsonObject {
                put("__composeWebView", true)
                put("kind", KIND_MESSAGE_SUCCESS)
                put("callbackId", callbackId)
                if (value == null) {
                    put("value", JsonNull)
                } else {
                    put("value", value)
                }
            }
        replyProxy.postMessage(envelope.toString())
    }

    private fun replyWithBinarySuccess(
        replyProxy: JavaScriptReplyProxy,
        callbackId: String,
        value: ByteArray,
    ) {
        ensureBinarySupportedForReplies()
        val envelope =
            buildJsonObject {
                put("__composeWebView", true)
                put("kind", KIND_MESSAGE_SUCCESS_BINARY_META)
                put("callbackId", callbackId)
            }
        replyProxy.postMessage(envelope.toString())
        replyProxy.postMessage(value)
    }

    private fun replyWithError(
        replyProxy: JavaScriptReplyProxy,
        callbackId: String?,
        errorMessage: String,
    ) {
        val envelope =
            buildJsonObject {
                put("__composeWebView", true)
                put("kind", KIND_ERROR)
                callbackId?.let { put("callbackId", it) }
                put("error", errorMessage)
            }
        replyProxy.postMessage(envelope.toString())
    }

    private fun supportsOriginAwareFoundation(): Boolean =
        WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER) &&
            WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)

    private fun supportsBinaryMessages(): Boolean = WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_ARRAY_BUFFER)

    private fun supportsMainFrameOutboundMessaging(): Boolean = WebViewFeature.isFeatureSupported(WebViewFeature.POST_WEB_MESSAGE)

    private fun installCompatibilityCapabilities() {
        currentCapabilities =
            BridgeCapabilities(
                supportsOriginAwareInbound = false,
                supportsBinaryMessages = false,
                supportsPersistentReplyChannel = false,
                supportsMainFrameOutboundMessaging = false,
            )
    }

    private fun ensureOriginAwareOutboundAvailable() {
        check(listenerInstalled) {
            "Origin-aware outbound messaging requires the origin-aware Android bridge to be attached to a document " +
                "matched by allowedOriginRules."
        }
        check(WebViewFeature.isFeatureSupported(WebViewFeature.POST_WEB_MESSAGE)) {
            "This WebView provider does not support postWebMessage."
        }
    }

    private fun ensurePortFeatures() {
        check(WebViewFeature.isFeatureSupported(WebViewFeature.CREATE_WEB_MESSAGE_CHANNEL)) {
            "This WebView provider does not support WebMessageChannel."
        }
        check(WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_PORT_POST_MESSAGE)) {
            "This WebView provider does not support WebMessagePort postMessage."
        }
        check(WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_PORT_SET_MESSAGE_CALLBACK)) {
            "This WebView provider does not support WebMessagePort callbacks."
        }
        check(WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_PORT_CLOSE)) {
            "This WebView provider does not support closing WebMessagePort instances."
        }
    }

    private fun ensureBinarySupportedForReplies() {
        check(config.enableBinaryMessages) {
            "Binary bridge messaging is disabled."
        }
        check(supportsBinaryMessages()) {
            "This WebView provider does not support binary WebMessage payloads."
        }
    }

    private fun parseTargetOrigin(targetOrigin: String): Uri {
        require(targetOrigin != "*") {
            "Wildcard target origins are not supported by ComposeWebView's origin-aware Android bridge."
        }
        val uri = Uri.parse(targetOrigin)
        require(!uri.scheme.isNullOrBlank()) {
            "targetOrigin must be an absolute origin such as https://example.com."
        }
        return uri
    }

    private fun parseEnvelope(raw: String): JsonObject? = runCatching { json.parseToJsonElement(raw) as? JsonObject }.getOrNull()

    private fun parseJsonElement(rawJson: String?): JsonElement =
        if (rawJson == null) {
            JsonNull
        } else {
            runCatching { json.parseToJsonElement(rawJson) }.getOrElse { JsonPrimitive(rawJson) }
        }

    private fun normalizeOrigin(origin: Uri): String? {
        val raw = origin.toString()
        return raw.takeUnless { it == "null" }
    }

    private fun compatibilityInterfaceName(bridge: WebViewJsBridge): String = "${bridge.nativeInterfaceName}_$compatibilityInterfaceName"

    private fun jsonElementToRawString(element: JsonElement): String? =
        when (element) {
            JsonNull -> null
            else -> element.toString()
        }

    private fun AndroidBridgeMessage.toWebMessageCompat(binaryEnabled: Boolean): WebMessageCompat =
        when (this) {
            is AndroidBridgeMessage.Text -> WebMessageCompat(value)
            is AndroidBridgeMessage.Binary -> {
                check(binaryEnabled && supportsBinaryMessages()) {
                    "Binary bridge messaging is disabled or unsupported."
                }
                WebMessageCompat(value)
            }
        }

    private fun buildOriginAwareBootstrapScript(bridge: WebViewJsBridge): String =
        """
        (() => {
            if (window.${bridge.jsObjectName}) return;

            const callbacks = {};
            const listeners = {};
            const rawMessageListeners = [];
            const sessionListeners = [];
            let pendingBinaryReplyCallbackId = null;
            const nativeBridge = window.${bridge.nativeInterfaceName};
            if (!nativeBridge || !nativeBridge.postMessage) return;
            const sessionToken = ${JsonPrimitive(SESSION_BOOTSTRAP_TOKEN)};

            const createCallbackId = () => 'cb_' + Math.random().toString(36).slice(2, 11);
            const cloneList = (list) => list.slice();
            const addListener = (bucket, callback) => {
                bucket.push(callback);
            };
            const removeListener = (bucket, callback) => {
                const index = bucket.indexOf(callback);
                if (index >= 0) bucket.splice(index, 1);
            };
            const trigger = (event, data) => {
                (listeners[event] || []).slice().forEach((callback) => callback(data));
            };
            const deliverRawMessage = (message) => {
                cloneList(rawMessageListeners).forEach((callback) => callback(message));
            };
            const resolveCallback = (callbackId, value, isError) => {
                const callback = callbacks[callbackId];
                if (!callback) return;
                if (isError) {
                    callback.reject(value);
                } else {
                    callback.resolve(value);
                }
                delete callbacks[callbackId];
            };
            const createSession = (port) => {
                const messageListeners = [];
                port.onmessage = (event) => {
                    const payload = event && 'data' in event ? event.data : event;
                    cloneList(messageListeners).forEach((callback) => callback(payload));
                };
                return {
                    postMessage: (message) => port.postMessage(message),
                    onMessage: (callback) => addListener(messageListeners, callback),
                    offMessage: (callback) => removeListener(messageListeners, callback),
                    close: () => port.close(),
                };
            };
            const handleEnvelope = (envelope) => {
                switch (envelope.kind) {
                    case ${JsonPrimitive(KIND_SUCCESS)}:
                        resolveCallback(envelope.callbackId, envelope.result, false);
                        return true;
                    case ${JsonPrimitive(KIND_MESSAGE_SUCCESS)}:
                        resolveCallback(envelope.callbackId, envelope.value ?? null, false);
                        return true;
                    case ${JsonPrimitive(KIND_MESSAGE_SUCCESS_BINARY_META)}:
                        pendingBinaryReplyCallbackId = envelope.callbackId;
                        return true;
                    case ${JsonPrimitive(KIND_ERROR)}:
                        if (envelope.callbackId) {
                            resolveCallback(envelope.callbackId, envelope.error, true);
                        } else {
                            console.error(envelope.error);
                        }
                        return true;
                    case ${JsonPrimitive(KIND_EVENT)}:
                        trigger(envelope.event, envelope.data);
                        return true;
                    default:
                        return false;
                }
            };
            const handleInbound = (payload, ports) => {
                if (ports && payload === sessionToken && ports[0]) {
                    const session = createSession(ports[0]);
                    cloneList(sessionListeners).forEach((callback) => callback(session));
                    return;
                }

                if (payload instanceof ArrayBuffer) {
                    if (pendingBinaryReplyCallbackId) {
                        resolveCallback(pendingBinaryReplyCallbackId, payload, false);
                        pendingBinaryReplyCallbackId = null;
                    } else {
                        deliverRawMessage(payload);
                    }
                    return;
                }

                if (typeof payload === 'string') {
                    try {
                        const envelope = JSON.parse(payload);
                        if (envelope && envelope.__composeWebView === true && handleEnvelope(envelope)) {
                            return;
                        }
                    } catch (_) {
                    }
                    deliverRawMessage(payload);
                    return;
                }

                deliverRawMessage(payload);
            };

            if (nativeBridge && nativeBridge.postMessage) {
                nativeBridge.onmessage = (event) => {
                    const payload = event && 'data' in event ? event.data : event;
                    handleInbound(payload, null);
                };
            }

            window.addEventListener('message', (event) => {
                handleInbound(event.data, event.ports || null);
            });

            window.${bridge.jsObjectName} = {
                call: (method, data) => {
                    return new Promise((resolve, reject) => {
                        const callbackId = createCallbackId();
                        callbacks[callbackId] = { resolve, reject };
                        nativeBridge.postMessage(JSON.stringify({
                            __composeWebView: true,
                            kind: ${JsonPrimitive(KIND_TYPED_CALL)},
                            method,
                            data: data === undefined ? null : JSON.stringify(data),
                            callbackId,
                        }));
                    });
                },
                callMessage: (method, message) => {
                    return new Promise((resolve, reject) => {
                        const callbackId = createCallbackId();
                        callbacks[callbackId] = { resolve, reject };
                        if (message instanceof ArrayBuffer) {
                            nativeBridge.postMessage(JSON.stringify({
                                __composeWebView: true,
                                kind: ${JsonPrimitive(KIND_BINARY_META)},
                                method,
                                callbackId,
                            }));
                            nativeBridge.postMessage(message);
                            return;
                        }
                        nativeBridge.postMessage(JSON.stringify({
                            __composeWebView: true,
                            kind: ${JsonPrimitive(KIND_MESSAGE_CALL)},
                            method,
                            value: String(message ?? ''),
                            callbackId,
                        }));
                    });
                },
                on: (event, callback) => {
                    if (!listeners[event]) listeners[event] = [];
                    listeners[event].push(callback);
                },
                off: (event, callback) => {
                    if (!listeners[event]) return;
                    listeners[event] = listeners[event].filter((existing) => existing !== callback);
                },
                trigger,
                onMessage: (callback) => addListener(rawMessageListeners, callback),
                offMessage: (callback) => removeListener(rawMessageListeners, callback),
                onSession: (callback) => addListener(sessionListeners, callback),
                offSession: (callback) => removeListener(sessionListeners, callback),
            };

            window.dispatchEvent(new Event('AppBridgeReady'));
        })();
        """.trimIndent()

    private fun buildCompatibilityBootstrapScript(
        jsObjectName: String,
        nativeInterfaceName: String,
    ): String =
        """
        (() => {
            if (window.$jsObjectName) return;

            const callbacks = {};
            const listeners = {};
            const rawMessageListeners = [];
            const sessionListeners = [];

            const addListener = (bucket, callback) => bucket.push(callback);
            const removeListener = (bucket, callback) => {
                const index = bucket.indexOf(callback);
                if (index >= 0) bucket.splice(index, 1);
            };

            window.$jsObjectName = {
                call: (method, data) => {
                    return new Promise((resolve, reject) => {
                        const callbackId = 'cb_' + Math.random().toString(36).substr(2, 9);
                        callbacks[callbackId] = { resolve, reject };
                        const dataStr = (data === undefined || data === null) ? null : JSON.stringify(data);

                        if (window.$nativeInterfaceName && window.$nativeInterfaceName.call) {
                            window.$nativeInterfaceName.call(method, dataStr, callbackId);
                        } else {
                            reject("$nativeInterfaceName not found");
                        }
                    });
                },
                callMessage: () => Promise.reject('callMessage() requires the origin-aware Android bridge.'),
                on: (event, callback) => {
                    if (!listeners[event]) listeners[event] = [];
                    listeners[event].push(callback);
                },
                off: (event, callback) => {
                    if (!listeners[event]) return;
                    listeners[event] = listeners[event].filter((existing) => existing !== callback);
                },
                trigger: (event, data) => {
                    if (listeners[event]) {
                        listeners[event].forEach((callback) => callback(data));
                    }
                },
                onSuccess: (id, result) => {
                    const callback = callbacks[id];
                    if (callback) {
                        callback.resolve(result);
                        delete callbacks[id];
                    }
                },
                onError: (id, error) => {
                    const callback = callbacks[id];
                    if (callback) {
                        callback.reject(error);
                        delete callbacks[id];
                    }
                },
                onMessage: (callback) => addListener(rawMessageListeners, callback),
                offMessage: (callback) => removeListener(rawMessageListeners, callback),
                onSession: (callback) => addListener(sessionListeners, callback),
                offSession: (callback) => removeListener(sessionListeners, callback),
            };

            window.dispatchEvent(new Event('AppBridgeReady'));
        })();
        """.trimIndent()

    private data class PendingBinaryCall(
        val method: String,
        val callbackId: String,
        val context: BridgeInvocationContext,
        val replyChannel: AndroidBridgeReplyChannelImpl,
    )

    @ExperimentalComposeWebViewApi
    private class AndroidBridgeReplyChannelImpl(
        private val replyProxy: JavaScriptReplyProxy,
        private val binaryEnabled: Boolean,
    ) : AndroidBridgeReplyChannel {
        val scope = CoroutineScope(Dispatchers.Main)

        override var isActive: Boolean = true
            private set

        override suspend fun send(message: AndroidBridgeMessage) {
            check(isActive) { "Reply channel is no longer active." }
            when (message) {
                is AndroidBridgeMessage.Text -> replyProxy.postMessage(message.value)
                is AndroidBridgeMessage.Binary -> {
                    check(binaryEnabled) { "Binary bridge messaging is disabled or unsupported." }
                    replyProxy.postMessage(message.value)
                }
            }
        }

        override suspend fun sendText(value: String) {
            send(AndroidBridgeMessage.Text(value))
        }

        override suspend fun sendBytes(value: ByteArray) {
            send(AndroidBridgeMessage.Binary(value))
        }

        fun invalidate() {
            isActive = false
            scope.cancel()
        }
    }

    @ExperimentalComposeWebViewApi
    private class AndroidBridgeSessionImpl(
        override val targetOrigin: String,
        private val port: WebMessagePortCompat,
        private val binaryEnabled: Boolean,
        private val onClose: (AndroidBridgeSessionImpl) -> Unit,
    ) : AndroidBridgeSession {
        private var messageHandler: ((AndroidBridgeMessage) -> Unit)? = null

        override val isMainFrame: Boolean = true

        override var isActive: Boolean = true
            private set

        override fun setMessageHandler(handler: ((AndroidBridgeMessage) -> Unit)?) {
            messageHandler = handler
        }

        override suspend fun send(message: AndroidBridgeMessage) {
            check(isActive) { "Bridge session is no longer active." }
            when (message) {
                is AndroidBridgeMessage.Text -> port.postMessage(WebMessageCompat(message.value))
                is AndroidBridgeMessage.Binary -> {
                    check(binaryEnabled) { "Binary bridge messaging is disabled or unsupported." }
                    port.postMessage(WebMessageCompat(message.value))
                }
            }
        }

        override suspend fun sendText(value: String) {
            send(AndroidBridgeMessage.Text(value))
        }

        override suspend fun sendBytes(value: ByteArray) {
            send(AndroidBridgeMessage.Binary(value))
        }

        override fun close() {
            if (!isActive) return
            isActive = false
            port.close()
            onClose(this)
        }

        fun installMessageCallback() {
            port.setWebMessageCallback(
                object : WebMessagePortCompat.WebMessageCallbackCompat() {
                    override fun onMessage(
                        port: WebMessagePortCompat,
                        message: WebMessageCompat?,
                    ) {
                        val payload =
                            if (message?.type == WebMessageCompat.TYPE_ARRAY_BUFFER) {
                                AndroidBridgeMessage.Binary(message.arrayBuffer)
                            } else {
                                AndroidBridgeMessage.Text(message?.data.orEmpty())
                            }
                        messageHandler?.invoke(payload)
                    }
                },
            )
        }
    }

    private companion object {
        private const val KIND_TYPED_CALL = "typedCall"
        private const val KIND_MESSAGE_CALL = "messageCall"
        private const val KIND_BINARY_META = "binaryMeta"
        private const val KIND_SUCCESS = "success"
        private const val KIND_MESSAGE_SUCCESS = "messageSuccess"
        private const val KIND_MESSAGE_SUCCESS_BINARY_META = "messageSuccessBinaryMeta"
        private const val KIND_ERROR = "error"
        private const val KIND_EVENT = "event"
        private const val SESSION_BOOTSTRAP_TOKEN = "__composeWebViewSession__"
    }
}
