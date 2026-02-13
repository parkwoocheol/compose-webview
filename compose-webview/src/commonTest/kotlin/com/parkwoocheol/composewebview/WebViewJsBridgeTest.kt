package com.parkwoocheol.composewebview

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun testRegisterUnitInputAcceptsNull() {
        val bridge = WebViewJsBridge()
        var called = false

        bridge.register<Unit, TestResponse>("refreshSession") {
            called = true
            TestResponse(true)
        }

        val handler = bridge.handlers["refreshSession"]
        assertNotNull(handler)

        val resultJson = handler(null)
        assertTrue(called)
        assertEquals("""{"success":true}""", resultJson)
    }

    @Test
    fun testRegisterNonNullInputRejectsNull() {
        val bridge = WebViewJsBridge()

        bridge.register<TestData, TestResponse>("testMethod") { data ->
            TestResponse(data.id > 0)
        }

        val handler = bridge.handlers["testMethod"]
        assertNotNull(handler)

        val exception =
            assertFailsWith<IllegalArgumentException> {
            handler(null)
        }
        assertTrue(exception.message?.contains("registerNullable") == true)
    }

    @Test
    fun testRegisterNullableInputAcceptsNull() {
        val bridge = WebViewJsBridge()

        bridge.registerNullable<TestData, TestResponse>("updateUserMaybe") { data ->
            TestResponse(success = data == null)
        }

        val handler = bridge.handlers["updateUserMaybe"]
        assertNotNull(handler)

        val resultJson = handler(null)
        assertEquals("""{"success":true}""", resultJson)
    }

    @Test
    fun testRegisterNullableInputAcceptsValue() {
        val bridge = WebViewJsBridge()

        bridge.registerNullable<TestData, TestResponse>("updateUserMaybe") { data ->
            TestResponse(success = data?.id == 1 && data.name == "Test")
        }

        val handler = bridge.handlers["updateUserMaybe"]
        assertNotNull(handler)

        val resultJson = handler("""{"id":1,"name":"Test"}""")
        assertEquals("""{"success":true}""", resultJson)
    }
}
