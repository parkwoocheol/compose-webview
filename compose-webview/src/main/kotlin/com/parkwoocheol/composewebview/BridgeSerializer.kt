package com.parkwoocheol.composewebview

import kotlin.reflect.KType

/**
 * Interface for abstracting the serialization and deserialization logic used by [WebViewJsBridge].
 * Implement this interface to support different serialization libraries (e.g., Moshi, Gson).
 */
interface BridgeSerializer {
    /**
     * Encodes the given [data] into a JSON string.
     *
     * @param data The object to encode.
     * @param type The [KType] of the data.
     * @return The JSON string representation of the data.
     */
    fun encode(data: Any?, type: KType): String

    /**
     * Decodes the given [json] string into an object of type [T].
     *
     * @param json The JSON string to decode.
     * @param type The [KType] of the target object.
     * @return The decoded object.
     */
    fun <T> decode(json: String, type: KType): T
}
