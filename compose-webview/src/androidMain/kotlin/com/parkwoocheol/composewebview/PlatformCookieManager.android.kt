package com.parkwoocheol.composewebview

import android.webkit.CookieManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of [PlatformCookieManager].
 */
actual object PlatformCookieManager {
    actual suspend fun getCookies(url: String): List<PlatformCookie> =
        withContext(Dispatchers.IO) {
            val cookieManager = CookieManager.getInstance()
            val cookieStr = cookieManager.getCookie(url) ?: return@withContext emptyList()
            // Android CookieManager returns a single string like "name=value; name2=value2"
            // It loses some metadata (domain, path, etc.) when reading back.
            // We parse what we can.
            cookieStr.split(";").mapNotNull { part ->
                val eqIndex = part.indexOf('=')
                if (eqIndex > 0) {
                    val name = part.substring(0, eqIndex).trim()
                    val value = part.substring(eqIndex + 1).trim()
                    PlatformCookie(name, value)
                } else {
                    null
                }
            }
        }

    actual suspend fun setCookie(
        url: String,
        cookie: PlatformCookie,
    ) = withContext(Dispatchers.IO) {
        val cookieManager = CookieManager.getInstance()
        val cookieValue = buildString {
            append("${cookie.name}=${cookie.value}")
            cookie.domain?.let { append("; Domain=$it") }
            cookie.path?.let { append("; Path=$it") }
            if (cookie.secure) append("; Secure")
            if (cookie.httpOnly) append("; HttpOnly")
            cookie.maxAge?.let { append("; Max-Age=$it") }
        }
        cookieManager.setCookie(url, cookieValue)
    }

    actual suspend fun removeAllCookies() =
        withContext(Dispatchers.IO) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()
        }

    actual suspend fun flush() =
        withContext(Dispatchers.IO) {
            CookieManager.getInstance().flush()
        }
}
