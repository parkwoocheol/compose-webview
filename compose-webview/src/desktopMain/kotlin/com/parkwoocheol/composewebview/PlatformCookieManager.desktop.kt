package com.parkwoocheol.composewebview

/**
 * Desktop implementation of [PlatformCookieManager] using JCEF.
 */
actual object PlatformCookieManager {
    actual suspend fun getCookies(url: String): List<PlatformCookie> {
        // JCEF's Cookie Manager is async and callback based.
        // Full cookie support requires dedicated CefCookieManager lifecycle handling.
        return emptyList()
    }

    actual suspend fun setCookie(
        url: String,
        cookie: PlatformCookie,
    ) {
        // Placeholder
    }

    actual suspend fun removeAllCookies() {
        // Placeholder
    }

    actual suspend fun removeCookies(url: String) {
        // Placeholder
    }

    actual suspend fun flush() {
        // Placeholder
    }
}
