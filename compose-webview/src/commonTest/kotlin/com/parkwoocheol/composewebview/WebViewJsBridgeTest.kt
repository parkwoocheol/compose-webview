package com.parkwoocheol.composewebview

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
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
    private val defaultContext =
        BridgeInvocationContext(
            sourceOrigin = null,
            isMainFrame = true,
            transport = BridgeTransport.Compatibility,
        )

    @Test
    fun testRegisterAndCall() =
        runTest {
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

            val resultJson = handler(defaultContext, inputJson)
            assertTrue(called)
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterNoArg() =
        runTest {
            val bridge = WebViewJsBridge()
            var called = false

            bridge.register<TestResponse>("testNoArg") {
                called = true
                TestResponse(true)
            }

            val handler = bridge.handlers["testNoArg"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, null)
            assertTrue(called)
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterUnitInputAcceptsNull() =
        runTest {
            val bridge = WebViewJsBridge()
            var called = false

            bridge.register<Unit, TestResponse>("refreshSession") {
                called = true
                TestResponse(true)
            }

            val handler = bridge.handlers["refreshSession"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, null)
            assertTrue(called)
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterNonNullInputRejectsNull() =
        runTest {
            val bridge = WebViewJsBridge()

            bridge.register<TestData, TestResponse>("testMethod") { data ->
                TestResponse(data.id > 0)
            }

            val handler = bridge.handlers["testMethod"]
            assertNotNull(handler)

            val exception =
                assertFailsWith<IllegalArgumentException> {
                    handler(defaultContext, null)
                }
            assertTrue(exception.message?.contains("registerNullable") == true)
        }

    @Test
    fun testRegisterNullableInputAcceptsNull() =
        runTest {
            val bridge = WebViewJsBridge()

            bridge.registerNullable<TestData, TestResponse>("updateUserMaybe") { data ->
                TestResponse(success = data == null)
            }

            val handler = bridge.handlers["updateUserMaybe"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, null)
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterNullableInputAcceptsValue() =
        runTest {
            val bridge = WebViewJsBridge()

            bridge.registerNullable<TestData, TestResponse>("updateUserMaybe") { data ->
                TestResponse(success = data?.id == 1 && data.name == "Test")
            }

            val handler = bridge.handlers["updateUserMaybe"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, """{"id":1,"name":"Test"}""")
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterSuspendAndCall() =
        runTest {
            val bridge = WebViewJsBridge()
            var called = false

            bridge.register<TestData, TestResponse>("suspendMethod") { data ->
                delay(100)
                called = true
                TestResponse(data.id > 0)
            }

            val handler = bridge.handlers["suspendMethod"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, """{"id":1,"name":"Test"}""")
            assertTrue(called)
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterSuspendNoArg() =
        runTest {
            val bridge = WebViewJsBridge()
            var called = false

            bridge.register<TestResponse>("suspendNoArg") {
                delay(100)
                called = true
                TestResponse(true)
            }

            val handler = bridge.handlers["suspendNoArg"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, null)
            assertTrue(called)
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterNullableSuspendAcceptsNull() =
        runTest {
            val bridge = WebViewJsBridge()

            bridge.registerNullable<TestData, TestResponse>("suspendNullable") { data ->
                delay(100)
                TestResponse(success = data == null)
            }

            val handler = bridge.handlers["suspendNullable"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, null)
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterNullableSuspendAcceptsValue() =
        runTest {
            val bridge = WebViewJsBridge()

            bridge.registerNullable<TestData, TestResponse>("suspendNullable") { data ->
                delay(100)
                TestResponse(success = data?.id == 1 && data.name == "Test")
            }

            val handler = bridge.handlers["suspendNullable"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, """{"id":1,"name":"Test"}""")
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterSuspendUnitInputAcceptsNull() =
        runTest {
            val bridge = WebViewJsBridge()
            var called = false

            bridge.register<Unit, TestResponse>("suspendRefresh") {
                delay(50)
                called = true
                TestResponse(true)
            }

            val handler = bridge.handlers["suspendRefresh"]
            assertNotNull(handler)

            val resultJson = handler(defaultContext, null)
            assertTrue(called)
            assertEquals("""{"success":true}""", resultJson)
        }

    @Test
    fun testRegisterSuspendNonNullInputRejectsNull() =
        runTest {
            val bridge = WebViewJsBridge()

            bridge.register<TestData, TestResponse>("suspendStrict") { data ->
                TestResponse(data.id > 0)
            }

            val handler = bridge.handlers["suspendStrict"]
            assertNotNull(handler)

            val exception =
                assertFailsWith<IllegalArgumentException> {
                    handler(defaultContext, null)
                }
            assertTrue(exception.message?.contains("registerNullable") == true)
        }

    @Test
    fun testRegisterWithInvocationContext() =
        runTest {
            val bridge = WebViewJsBridge()
            val invocationContext =
                BridgeInvocationContext(
                    sourceOrigin = "https://example.com",
                    isMainFrame = false,
                    transport = BridgeTransport.OriginAwareListener,
                )

            bridge.registerWithContext<TestData, TestResponse>("contextAware") { data ->
                assertEquals("https://example.com", sourceOrigin)
                assertEquals(false, isMainFrame)
                assertEquals(BridgeTransport.OriginAwareListener, transport)
                TestResponse(success = data.id == 7 && data.name == "Bridge")
            }

            val handler = bridge.handlers["contextAware"]
            assertNotNull(handler)

            val resultJson = handler(invocationContext, """{"id":7,"name":"Bridge"}""")
            assertEquals("""{"success":true}""", resultJson)
        }
}
