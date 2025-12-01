package com.parkwoocheol.composewebview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

/**
 * A bridge for communication between a WebView's JavaScript and the native Android application.
 *
 * This class handles the serialization and deserialization of data using a [BridgeSerializer], allowing for
 * library-agnostic communication. It supports registering handlers for specific method names and
 * emitting events to JavaScript.
 *
 * @property serializer The [BridgeSerializer] instance used for serialization. Defaults to [KotlinxBridgeSerializer].
 * @property jsObjectName The name of the JavaScript object injected into the window (e.g., window.AppBridge).
 * @property nativeInterfaceName The name of the native interface injected into the WebView.
 */
class WebViewJsBridge(
    serializer: BridgeSerializer? = null,
    @PublishedApi internal val jsObjectName: String = "AppBridge",
    private val nativeInterfaceName: String = "AppBridgeNative"
) {
    @PublishedApi internal val serializer: BridgeSerializer = serializer ?: defaultSerializer()

    // Handler stores a function that takes a JSON string and returns a JSON string (or null)
    @PublishedApi
    internal val handlers = mutableMapOf<String, (String?) -> String?>()
    
    @PublishedApi
    internal var webView: WebView? = null
    
    @PublishedApi
    internal val scope = CoroutineScope(Dispatchers.Main)

    /**
     * The JavaScript code to inject into the WebView.
     * This script sets up the global window object (e.g., window.AppBridge)
     * that proxies calls to the native interface (e.g., window.AppBridgeNative).
     */
    val jsScript: String
        get() = """
            (() => {
                if (window.$jsObjectName) return;
                
                const callbacks = {};
                const listeners = {};
                
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
                    on: (event, callback) => {
                        if (!listeners[event]) listeners[event] = [];
                        listeners[event].push(callback);
                    },
                    off: (event, callback) => {
                        if (!listeners[event]) return;
                        listeners[event] = listeners[event].filter(cb => cb !== callback);
                    },
                    trigger: (event, data) => {
                        if (listeners[event]) {
                            listeners[event].forEach(cb => cb(data));
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
                    }
                };
                
                window.dispatchEvent(new Event('AppBridgeReady'));
            })();
        """

    /**
     * Registers a handler for the given method name.
     * The handler receives the data as type [T] and returns a result of type [R].
     *
     * @param method The name of the method to register.
     * @param handler The function to handle the method call.
     */
    inline fun <reified T : Any, reified R : Any> register(
        method: String,
        noinline handler: (T) -> R
    ) {
        handlers[method] = { jsonStr ->
            val input = if (jsonStr != null) {
                serializer.decode<T>(jsonStr, typeOf<T>())
            } else {
                // If T is nullable, we can pass null. But here we enforce T : Any for simplicity in serializer
                // If we want to support nullable T, we need to handle it.
                // For now, let's assume T is non-null or handle nulls in serializer if needed.
                // Actually, typeOf<T>() captures nullability.
                // But our interface says encode(data: Any).
                // Let's adjust: if jsonStr is null, and T is nullable, pass null.
                if (typeOf<T>().isMarkedNullable) {
                    null as T
                } else {
                    throw IllegalArgumentException("Input data is null but type is not nullable")
                }
            }
            
            val result = handler(input)
            serializer.encode(result, typeOf<R>())
        }
    }

    /**
     * Registers a handler that takes no arguments.
     *
     * @param method The name of the method to register.
     * @param handler The function to handle the method call.
     */
    inline fun <reified R : Any> register(
        method: String,
        noinline handler: () -> R
    ) {
        handlers[method] = { _ ->
            val result = handler()
            serializer.encode(result, typeOf<R>())
        }
    }

    /**
     * Emits an event to JavaScript.
     * The [data] will be serialized to JSON and passed to the JS event listeners.
     *
     * @param event The name of the event to emit.
     * @param data The data to pass to the event listener.
     */
    inline fun <reified T> emit(event: String, data: T) {
        val jsonStr = serializer.encode(data, typeOf<T>())
        // We need to run this on the main thread because it interacts with WebView
        scope.launch {
            val script = "window.$jsObjectName.trigger('$event', $jsonStr);"
            webView?.evaluateJavascript(script, null)
        }
    }

    /**
     * @param method The name of the method to unregister.
     */
    fun unregister(method: String) {
        handlers.remove(method)
    }

    internal fun attach(webView: WebView) {
        this.webView = webView
        webView.addJavascriptInterface(this, nativeInterfaceName)
    }
    
    /**
     * Disposes of the bridge, cancelling any active coroutines and clearing references.
     * This should be called when the bridge is no longer needed to prevent memory leaks.
     */
    fun dispose() {
        scope.cancel()
        webView = null
    }

    /**
     * The method called by JavaScript to invoke a native handler.
     *
     * @param methodName The name of the method to call.
     * @param data The JSON string containing the data for the handler.
     * @param callbackId The ID of the callback to invoke with the result.
     */
    @JavascriptInterface
    fun call(methodName: String, data: String?, callbackId: String?) {
        val handler = handlers[methodName]
        if (handler == null) {
            if (callbackId != null) {
                sendError(callbackId, "No handler found for method: $methodName")
            }
            return
        }

        scope.launch {
            try {
                val resultJson = handler(data)
                
                if (callbackId != null) {
                    sendSuccess(callbackId, resultJson)
                }
            } catch (e: Exception) {
                if (callbackId != null) {
                    sendError(callbackId, e.message ?: "Unknown error")
                }
            }
        }
    }

    private fun sendSuccess(callbackId: String, resultJson: String?) {
        val safeResult = resultJson ?: "null"
        val script = "window.$jsObjectName.onSuccess('$callbackId', $safeResult);"
        webView?.evaluateJavascript(script, null)
    }

    private fun sendError(callbackId: String, errorMessage: String) {
        // We use the serializer to encode the error message string to ensure it's a valid JSON string
        // But since it's just a string, we can manually quote it or use serializer.
        // Using serializer is safer.
        val escapedError = try {
            serializer.encode(errorMessage, typeOf<String>())
        } catch (e: Exception) {
            "\"${errorMessage.replace("\"", "\\\"")}\""
        }
        val script = "window.$jsObjectName.onError('$callbackId', $escapedError);"
        webView?.evaluateJavascript(script, null)
    }
}

private fun defaultSerializer(): BridgeSerializer {
    try {
        return KotlinxBridgeSerializer()
    } catch (e: NoClassDefFoundError) {
        throw IllegalStateException("Kotlinx Serialization is missing. Please add 'org.jetbrains.kotlinx:kotlinx-serialization-json' dependency or provide a custom serializer.", e)
    } catch (e: ClassNotFoundException) {
        throw IllegalStateException("Kotlinx Serialization is missing. Please add 'org.jetbrains.kotlinx:kotlinx-serialization-json' dependency or provide a custom serializer.", e)
    }
}

/**
 * Creates and remembers a [WebViewJsBridge].
 *
 * This composable manages the lifecycle of the bridge, ensuring it is disposed when the composable leaves the composition.
 *
 * @param serializer The [BridgeSerializer] to use for serialization. Defaults to null, which tries to use [KotlinxBridgeSerializer].
 * @param jsObjectName The name of the JavaScript object to inject (e.g., "AppBridge").
 * @param nativeInterfaceName The name of the native interface to inject (e.g., "AppBridgeNative").
 * @return A [WebViewJsBridge] instance.
 */
@Composable
fun rememberWebViewJsBridge(
    serializer: BridgeSerializer? = null,
    jsObjectName: String = "AppBridge",
    nativeInterfaceName: String = "AppBridgeNative"
): WebViewJsBridge {
    val bridge = remember(serializer, jsObjectName, nativeInterfaceName) { 
        WebViewJsBridge(serializer, jsObjectName, nativeInterfaceName) 
    }
    
    DisposableEffect(bridge) {
        onDispose {
            bridge.dispose()
        }
    }
    
    return bridge
}
