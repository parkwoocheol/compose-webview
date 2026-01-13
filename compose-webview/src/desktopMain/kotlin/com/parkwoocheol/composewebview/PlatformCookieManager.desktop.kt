package com.parkwoocheol.composewebview

/**
 * Desktop implementation of [PlatformCookieManager] using KCEF.
 */
actual object PlatformCookieManager {
    actual suspend fun getCookies(url: String): List<PlatformCookie> {
        // KCEF's Cookie Manager is async and callback based.
        // Implementing full KCEF cookie support requires careful setup.
        // Placeholder for now as KCEF implementation requires a running CefClient/Manager.
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
