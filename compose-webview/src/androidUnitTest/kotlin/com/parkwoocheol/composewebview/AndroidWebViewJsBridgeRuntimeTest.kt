package com.parkwoocheol.composewebview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
