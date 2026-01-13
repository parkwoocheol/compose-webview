package com.parkwoocheol.composewebview

import kotlinx.browser.document
import org.w3c.dom.get

/**
 * JS implementation of [PlatformCookieManager].
 * Uses document.cookie for browser cookie management.
 */
actual object PlatformCookieManager {
    actual suspend fun getCookies(url: String): List<PlatformCookie> {
        val cookieStr = document.cookie
        if (cookieStr.isBlank()) return emptyList()

        return cookieStr.split(";").mapNotNull { part ->
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
    ) {
        val cookieValue =
            buildString {
                append("${cookie.name}=${cookie.value}")
                cookie.domain?.let { append("; Domain=$it") }
                cookie.path?.let { append("; Path=$it") }
                if (cookie.secure) append("; Secure")
                if (cookie.httpOnly) append("; HttpOnly")
                cookie.maxAge?.let { append("; Max-Age=$it") }
            }
        document.cookie = cookieValue
    }

    actual suspend fun removeAllCookies() {
        // In JS, we need to set each cookie's expiry to the past
        val cookies = getCookies("")
        cookies.forEach { cookie ->
            document.cookie = "${cookie.name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/"
        }
    }

    actual suspend fun removeCookies(url: String) {
        // Simple implementation for JS - removes all for now as same-origin policy
        // usually limits JS access to cookies of the current domain anyway.
        removeAllCookies()
    }

    actual suspend fun flush() {
        // No-op for JS - cookies are written immediately
    }
}
