package com.parkwoocheol.composewebview

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Serializable
data class TestData(val id: Int, val name: String)

@Serializable
data class TestResponse(val success: Boolean)

class WebViewJsBridgeTest {
    @Test
    fun testRegisterAndCall() {
        val bridge = WebViewJsBridge()
        var called = false

        bridge.register<TestData, TestResponse>("testMethod") { data ->
            assertEquals(1, data.id)
            assertEquals("Test", data.name)
            called = true
            TestResponse(true)
        }

        val inputJson = """{"id":1,"name":"Test"}"""
        val handler = bridge.handlers["testMethod"]
        assertNotNull(handler)

        val resultJson = handler(inputJson)
        assertTrue(called)
        assertEquals("""{"success":true}""", resultJson)
    }

    @Test
    fun testRegisterNoArg() {
        val bridge = WebViewJsBridge()
        var called = false

        bridge.register<TestResponse>("testNoArg") {
            called = true
            TestResponse(true)
        }

        val handler = bridge.handlers["testNoArg"]
        assertNotNull(handler)

        val resultJson = handler(null) // No arg call passes null (or empty string depending on impl, but handler wrapper handles it)
        assertTrue(called)
        assertEquals("""{"success":true}""", resultJson)
    }
}
