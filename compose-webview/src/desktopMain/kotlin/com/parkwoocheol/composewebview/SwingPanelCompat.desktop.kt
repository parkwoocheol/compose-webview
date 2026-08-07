package com.parkwoocheol.composewebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import java.awt.Component
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier as JavaModifier

/**
 * Calls `androidx.compose.ui.awt.SwingPanel` in a way that survives its JVM signature changes.
 *
 * Compose Multiplatform up to 1.11 only exposed `SwingPanel(background: Color, ...)`, which the
 * Kotlin compiler mangles into the JVM symbol `SwingPanel-euL9pac(long, ...)`. Compose 1.12 turned
 * that parameter into `Color?` (new symbol `SwingPanel-iRkQZW0`) and added an overload without
 * `background`, so the symbol this library was compiled against no longer exists and the direct
 * call fails with `NoSuchMethodError` at runtime. (#63)
 *
 * The compiled call site is kept for the versions where it is still valid, so nothing changes for
 * them. When the symbol is gone, the replacement overload is resolved once and invoked reflectively.
 *
 * @param modifier The modifier applied to the interop layout.
 * @param factory Creates the AWT/Swing component to embed.
 * @param update Invoked on every recomposition to apply state to the component.
 */
@Composable
internal fun <T : Component> CompatSwingPanel(
    modifier: Modifier,
    factory: () -> T,
    update: (T) -> Unit = {},
) {
    val fallback = SwingPanelFallback.resolved
    if (fallback == null) {
        SwingPanel(
            modifier = modifier,
            factory = factory,
            update = update,
        )
    } else {
        fallback.invoke(
            composer = currentComposer,
            factory = factory,
            modifier = modifier,
            update = update,
        )
    }
}

/**
 * A reflective binding to the `SwingPanel` overload that replaced the symbol this library was
 * compiled against.
 *
 * @property method The resolved `SwingPanel` overload.
 * @property usesBackgroundParameter Whether [method] still declares a leading `background`
 *   parameter that has to be filled with a placeholder and defaulted through `$default`.
 */
internal class SwingPanelFallback private constructor(
    private val method: Method,
    val usesBackgroundParameter: Boolean,
) {
    val methodName: String get() = method.name

    /**
     * Invokes the resolved overload as if it were a normal composable call.
     *
     * `$changed` is passed as `0`, which is the "nothing is known to have changed" encoding, so
     * `SwingPanel` compares the arguments itself instead of trusting caller-provided bits.
     */
    fun invoke(
        composer: Composer,
        factory: Function0<*>,
        modifier: Modifier,
        update: Function1<*, *>,
    ) {
        try {
            if (usesBackgroundParameter) {
                val placeholder = if (method.parameterTypes[0] == Long::class.javaPrimitiveType) 0L else null
                method.invoke(null, placeholder, factory, modifier, update, composer, 0, BACKGROUND_IS_DEFAULT)
            } else {
                method.invoke(null, factory, modifier, update, composer, 0, NO_DEFAULTS)
            }
        } catch (error: InvocationTargetException) {
            throw error.cause ?: error
        }
    }

    internal companion object {
        private const val OWNER = "androidx.compose.ui.awt.SwingPanel_desktopKt"
        private const val NAME = "SwingPanel"

        /** The JVM symbol produced by `SwingPanel(background: Color, ...)` on Compose 1.9 - 1.11. */
        private const val COMPILED_SYMBOL = "SwingPanel-euL9pac"

        private const val NO_DEFAULTS = 0
        private const val BACKGROUND_IS_DEFAULT = 0b1

        /** `null` when the compiled call site is still valid on the Compose version in use. */
        val resolved: SwingPanelFallback? by lazy(LazyThreadSafetyMode.PUBLICATION) {
            resolveIn(runCatching { Class.forName(OWNER) }.getOrNull())
        }

        /**
         * Picks the overload to invoke reflectively.
         *
         * @param owner The class holding the `SwingPanel` overloads, or `null` when unavailable.
         * @return `null` when the compiled call site can be used as-is or nothing usable was found.
         */
        internal fun resolveIn(owner: Class<*>?): SwingPanelFallback? {
            if (owner == null) return null
            val candidates = owner.methods.filter { JavaModifier.isStatic(it.modifiers) }
            if (candidates.any { it.name == COMPILED_SYMBOL }) return null

            val overloads = candidates.filter { it.name == NAME || it.name.startsWith("$NAME-") }
            overloads.firstOrNull { it.matchesSwingPanelShape(leadingParameterCount = 0) }
                ?.let { return SwingPanelFallback(it, usesBackgroundParameter = false) }
            overloads.firstOrNull { it.matchesSwingPanelShape(leadingParameterCount = 1) }
                ?.let { return SwingPanelFallback(it, usesBackgroundParameter = true) }

            return null
        }

        /**
         * Matches `(<leading params>, factory, modifier, update, composer, $changed, $default)`,
         * the shape every `SwingPanel` overload compiles to.
         */
        private fun Method.matchesSwingPanelShape(leadingParameterCount: Int): Boolean {
            val types = parameterTypes
            if (types.size != leadingParameterCount + 6) return false
            val offset = leadingParameterCount
            return types[offset] == Function0::class.java &&
                types[offset + 1] == Modifier::class.java &&
                types[offset + 2] == Function1::class.java &&
                types[offset + 3] == Composer::class.java &&
                types[offset + 4] == Int::class.javaPrimitiveType &&
                types[offset + 5] == Int::class.javaPrimitiveType
        }
    }
}
