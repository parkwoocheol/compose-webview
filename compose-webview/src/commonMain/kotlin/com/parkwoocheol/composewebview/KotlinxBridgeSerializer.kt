package com.parkwoocheol.composewebview

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

/**
 * Default implementation of [BridgeSerializer] using kotlinx.serialization.
 *
 * @property json The [Json] instance to use.
 */
class KotlinxBridgeSerializer(
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) : BridgeSerializer {
    
    override fun encode(data: Any?, type: KType): String {
        val serializer = json.serializersModule.serializer(type)
        return json.encodeToString(serializer, data)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(jsonString: String, type: KType): T {
        val serializer = json.serializersModule.serializer(type)
        return json.decodeFromString(serializer, jsonString) as T
    }
}
