package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composer
import androidx.compose.ui.Modifier
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Stand-ins for the `SwingPanel_desktopKt` shapes shipped by the Compose versions this library
 * supports. Only the JVM signatures matter, so most bodies are empty.
 */
@Suppress("unused", "UNUSED_PARAMETER", "FunctionName", "ktlint:standard:function-naming")
private object ComposeUpTo1x11 {
    @JvmStatic
    fun `SwingPanel-euL9pac`(
        background: Long,
        factory: Function0<*>,
        modifier: Modifier,
        update: Function1<*, *>,
        composer: Composer?,
        changed: Int,
        default: Int,
    ) = Unit
}

@Suppress("unused", "UNUSED_PARAMETER", "FunctionName", "ktlint:standard:function-naming")
private object Compose1x12 {
    @JvmStatic
    fun `SwingPanel-iRkQZW0`(
        background: Any?,
        factory: Function0<*>,
        modifier: Modifier,
        update: Function1<*, *>,
        composer: Composer?,
        changed: Int,
        default: Int,
    ) = Unit

    @JvmStatic
    fun SwingPanel(
        factory: Function0<*>,
        modifier: Modifier,
        update: Function1<*, *>,
        composer: Composer?,
        changed: Int,
        default: Int,
    ) = Unit
}

@Suppress("unused", "UNUSED_PARAMETER", "FunctionName", "ktlint:standard:function-naming")
private object BackgroundOverloadOnly {
    @JvmStatic
    var received: List<Any?>? = null

    @JvmStatic
    fun `SwingPanel-iRkQZW0`(
        background: Any?,
        factory: Function0<*>,
        modifier: Modifier,
        update: Function1<*, *>,
        composer: Composer?,
        changed: Int,
        default: Int,
    ) {
        received = listOf(background, factory, modifier, update, composer, changed, default)
    }
}

@Suppress("unused", "UNUSED_PARAMETER", "FunctionName", "ktlint:standard:function-naming")
private object RecordingOverload {
    @JvmStatic
    var received: List<Any?>? = null

    @JvmStatic
    fun SwingPanel(
        factory: Function0<*>,
        modifier: Modifier,
        update: Function1<*, *>,
        composer: Composer?,
        changed: Int,
        default: Int,
    ) {
        received = listOf(factory, modifier, update, composer, changed, default)
    }
}

@Suppress("unused", "UNUSED_PARAMETER", "FunctionName", "ktlint:standard:function-naming")
private object ThrowingOverload {
    @JvmStatic
    fun SwingPanel(
        factory: Function0<*>,
        modifier: Modifier,
        update: Function1<*, *>,
        composer: Composer?,
        changed: Int,
        default: Int,
    ): Unit = throw IllegalStateException("boom")
}

@Suppress("unused")
private object NoSwingPanelAtAll

class SwingPanelFallbackTest {
    private val composer =
        Proxy.newProxyInstance(
            Composer::class.java.classLoader,
            arrayOf(Composer::class.java),
        ) { _, _, _ -> null } as Composer

    private val factory: Function0<String> = { "component" }
    private val update: Function1<String, Unit> = { }

    @Test
    fun `keeps the compiled call site while the legacy symbol exists`() {
        assertNull(SwingPanelFallback.resolveIn(ComposeUpTo1x11::class.java))
    }

    @Test
    fun `prefers the overload without background once the legacy symbol is gone`() {
        val fallback = SwingPanelFallback.resolveIn(Compose1x12::class.java)
        assertTrue(fallback != null, "expected a fallback for the Compose 1.12 shape")
        assertEquals("SwingPanel", fallback.methodName)
        assertEquals(false, fallback.usesBackgroundParameter)
    }

    @Test
    fun `falls back to the background overload when it is the only replacement`() {
        val fallback = SwingPanelFallback.resolveIn(BackgroundOverloadOnly::class.java)
        assertTrue(fallback != null, "expected a fallback for the background-only shape")
        assertEquals("SwingPanel-iRkQZW0", fallback.methodName)
        assertEquals(true, fallback.usesBackgroundParameter)
    }

    @Test
    fun `resolves to nothing when no usable overload is present`() {
        assertNull(SwingPanelFallback.resolveIn(NoSwingPanelAtAll::class.java))
        assertNull(SwingPanelFallback.resolveIn(null))
    }

    @Test
    fun `forwards arguments with no changed bits and no defaults`() {
        RecordingOverload.received = null
        val fallback = SwingPanelFallback.resolveIn(RecordingOverload::class.java)
        assertTrue(fallback != null, "expected a fallback for the recording shape")

        fallback.invoke(composer = composer, factory = factory, modifier = Modifier, update = update)

        val received = RecordingOverload.received
        assertTrue(received != null, "SwingPanel was not invoked")
        assertSame(factory, received[0])
        assertSame(Modifier, received[1])
        assertSame(update, received[2])
        assertSame(composer, received[3])
        assertEquals(0, received[4], "\$changed must be 0 so Compose compares the arguments itself")
        assertEquals(0, received[5], "\$default must be 0 when every parameter is supplied")
    }

    @Test
    fun `defaults the background parameter instead of guessing a color`() {
        BackgroundOverloadOnly.received = null
        val fallback = SwingPanelFallback.resolveIn(BackgroundOverloadOnly::class.java)
        assertTrue(fallback != null, "expected a fallback for the background-only shape")

        fallback.invoke(composer = composer, factory = factory, modifier = Modifier, update = update)

        val received = BackgroundOverloadOnly.received
        assertTrue(received != null, "SwingPanel was not invoked")
        assertNull(received[0], "the background placeholder must not be read")
        assertSame(factory, received[1])
        assertSame(Modifier, received[2])
        assertSame(update, received[3])
        assertSame(composer, received[4])
        assertEquals(0, received[5], "\$changed must be 0 so Compose compares the arguments itself")
        assertEquals(0b1, received[6], "\$default must mark background as defaulted")
    }

    @Test
    fun `unwraps failures raised inside SwingPanel`() {
        val fallback = SwingPanelFallback.resolveIn(ThrowingOverload::class.java)
        assertTrue(fallback != null, "expected a fallback for the throwing shape")

        val error =
            assertFailsWith<IllegalStateException> {
                fallback.invoke(composer = composer, factory = factory, modifier = Modifier, update = update)
            }
        assertEquals("boom", error.message)
    }

    @Test
    fun `uses the direct call site on the Compose version this build targets`() {
        assertNull(SwingPanelFallback.resolved)
    }
}
