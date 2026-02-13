package com.parkwoocheol.composewebview

import me.friwi.jcefmaven.CefAppBuilder
import org.cef.CefApp
import org.cef.CefClient
import java.io.File

internal object DesktopCefRuntime {
    @Volatile
    private var app: CefApp? = null
    private val lock = Any()

    fun initialize(settings: WebViewSettings) {
        if (app != null) return

        synchronized(lock) {
            if (app != null) return

            val args = mutableListOf<String>()
            if (!settings.javaScriptEnabled) {
                args += "--disable-javascript"
            }
            settings.userAgent?.takeIf { it.isNotBlank() }?.let { userAgent ->
                args += "--user-agent=$userAgent"
            }

            val builder = CefAppBuilder()
            builder.setInstallDir(File(System.getProperty("user.home"), ".compose-webview/jcef"))
            builder.getCefSettings().windowless_rendering_enabled = false
            builder.getCefSettings().root_cache_path =
                File(System.getProperty("user.home"), ".compose-webview/jcef/cache").absolutePath
            if (args.isNotEmpty()) {
                builder.addJcefArgs(*args.toTypedArray())
            }
            app = builder.build()
        }
    }

    fun createClient(): CefClient? = app?.createClient()
}
