package com.parkwoocheol.composewebview

/**
 * Represents a cookie with standard properties.
 */
data class PlatformCookie(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    // Seconds
    val maxAge: Long? = null,
)

/**
 * Manages cookies across different platforms.
 */
expect object PlatformCookieManager {
    /**
     * Gets all cookies for a specific URL.
     */
    suspend fun getCookies(url: String): List<PlatformCookie>

    /**
     * Sets a cookie for a specific URL.
     */
    suspend fun setCookie(
        url: String,
        cookie: PlatformCookie,
    )

    /**
     * Removes all cookies for a specific URL.
     */
    suspend fun removeCookies(url: String)

    /**
     * Removes all cookies.
     */
    suspend fun removeAllCookies()

    /**
     * Ensures all cookies are written to persistent storage.
     */
    suspend fun flush()
}
