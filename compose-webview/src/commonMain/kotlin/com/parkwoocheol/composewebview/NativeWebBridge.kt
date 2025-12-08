package com.parkwoocheol.composewebview

/**
 * Interface for the native side of the JS Bridge.
 * This allows platform-specific implementations to invoke methods on the bridge
 * in a standardized way, regardless of the underlying mechanism (direct injection vs message passing).
 */
interface NativeWebBridge {
    /**
     * The method called by JavaScript to invoke a native handler.
     *
     * @param methodName The name of the method to call.
     * @param data The JSON string containing the data for the handler.
     * @param callbackId The ID of the callback to invoke with the result.
     */
    fun call(methodName: String, data: String?, callbackId: String?)
}
