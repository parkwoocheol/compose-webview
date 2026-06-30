package com.parkwoocheol.composewebview

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.typeOf

class AndroidWebViewJsBridgeRuntimeTest {
    @Test
    fun defaultBridge_keepsExistingPageFinishedBootstrap() {
        val bridge = WebViewJsBridge()

        assertEquals(bridge.jsScript, bridge.pageFinishedBootstrapScript())
        assertEquals(BridgeCapabilities(), bridge.capabilities)
    }

    @Test
    fun originAwareOnlyBridge_providesOriginAwarePageFinishedBackfill() {
        val bridge = WebViewJsBridge()
        bridge.setRuntime(
            AndroidOriginAwareWebViewJsBridgeRuntime(
                AndroidWebViewJsBridgeConfig(
                    allowedOriginRules = setOf("https://example.com"),
                    policy = AndroidJsBridgePolicy.OriginAwareOnly,
                ),
            ),
        )

        val script = bridge.pageFinishedBootstrapScript()
        assertNotNull(script)
        assertTrue(script!!.contains("AppBridgeReady"))
        assertTrue(script.contains("nativeBridge.postMessage"))
    }

    @Test
    fun compatibleBridge_providesCompatibilityFallbackScript() {
        val bridge = WebViewJsBridge()
        bridge.setRuntime(
            AndroidOriginAwareWebViewJsBridgeRuntime(
                AndroidWebViewJsBridgeConfig(
                    allowedOriginRules = setOf("https://example.com"),
                    policy = AndroidJsBridgePolicy.Compatible,
                ),
            ),
        )

        val script = bridge.pageFinishedBootstrapScript()
        assertNotNull(script)
        assertTrue(script!!.contains("callMessage"))
        assertTrue(script.contains("AppBridgeReady"))
        assertTrue(script.contains("callMessage"))
    }

    @Test
    fun originAwareBridge_embedsTypedCallPayloadAsRawJson() {
        val bridge = WebViewJsBridge()
        bridge.setRuntime(
            AndroidOriginAwareWebViewJsBridgeRuntime(
                AndroidWebViewJsBridgeConfig(
                    allowedOriginRules = setOf("https://example.com"),
                    policy = AndroidJsBridgePolicy.OriginAwareOnly,
                ),
            ),
        )

        val script = bridge.pageFinishedBootstrapScript()
        assertNotNull(script)
        assertTrue(script!!.contains("data: (data === undefined || data === null) ? null : data,"))
        assertFalse(script.contains("JSON.stringify(data)"))
    }

    @Test
    fun originAwareTypedCallPayload_roundTripsThroughSerializer() {
        val runtime =
            AndroidOriginAwareWebViewJsBridgeRuntime(
                AndroidWebViewJsBridgeConfig(
                    allowedOriginRules = setOf("https://example.com"),
                    policy = AndroidJsBridgePolicy.OriginAwareOnly,
                ),
            )
        val serializer = KotlinxBridgeSerializer()
        val json = Json { ignoreUnknownKeys = true }

        fun extractData(envelope: String): String? =
            (json.parseToJsonElement(envelope) as JsonObject)["data"]?.let(runtime::jsonElementToRawString)

        val objectData =
            extractData(
                """{"__composeWebView":true,"kind":"typedCall","method":"showError",""" +
                    """"data":{"title":"Warning","message":"Something went wrong"},"callbackId":"cb_1"}""",
            )
        assertEquals(
            ErrorInfo("Warning", "Something went wrong"),
            serializer.decode<ErrorInfo>(objectData!!, typeOf<ErrorInfo>()),
        )

        val stringData =
            extractData(
                """{"__composeWebView":true,"kind":"typedCall","method":"log","data":"hello","callbackId":"cb_2"}""",
            )
        assertEquals("hello", serializer.decode<String>(stringData!!, typeOf<String>()))

        val jsonStringData =
            extractData(
                """{"__composeWebView":true,"kind":"typedCall","method":"log","data":"{\"x\":1}","callbackId":"cb_3"}""",
            )
        assertEquals("""{"x":1}""", serializer.decode<String>(jsonStringData!!, typeOf<String>()))

        val nullData =
            extractData(
                """{"__composeWebView":true,"kind":"typedCall","method":"log","data":null,"callbackId":"cb_4"}""",
            )
        assertNull(nullData)

        val doubleEncodedData =
            extractData(
                """{"__composeWebView":true,"kind":"typedCall","method":"showError",""" +
                    """"data":"{\"title\":\"Warning\",\"message\":\"Something went wrong\"}","callbackId":"cb_5"}""",
            )
        assertTrue(
            runCatching { serializer.decode<ErrorInfo>(doubleEncodedData!!, typeOf<ErrorInfo>()) }.isFailure,
        )
    }
}

@Serializable
private data class ErrorInfo(
    val title: String? = null,
    val message: String,
)
