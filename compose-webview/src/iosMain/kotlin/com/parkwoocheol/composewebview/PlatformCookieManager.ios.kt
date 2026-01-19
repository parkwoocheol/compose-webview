package com.parkwoocheol.composewebview

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSHTTPCookieDomain
import platform.Foundation.NSHTTPCookieName
import platform.Foundation.NSHTTPCookiePath
import platform.Foundation.NSHTTPCookieSecure
import platform.Foundation.NSHTTPCookieValue
import platform.WebKit.WKWebsiteDataStore
import kotlin.coroutines.resume

/**
 * iOS implementation of [PlatformCookieManager].
 */
actual object PlatformCookieManager {
    actual suspend fun getCookies(url: String): List<PlatformCookie> =
        suspendCancellableCoroutine { continuation ->
            val cookieStore = WKWebsiteDataStore.defaultDataStore().httpCookieStore
            cookieStore.getAllCookies { cookies ->
                val nsUrl = platform.Foundation.NSURL.URLWithString(url)
                val host = nsUrl?.host

                val platformCookies =
                    (cookies as? List<NSHTTPCookie>)?.mapNotNull { cookie ->
                        // Basic domain matching:
                        // If cookie domain is ".google.com", it matches "google.com", "www.google.com"
                        // If cookie domain is "google.com", it matches "google.com"
                        // We also accept if specific host is not parsed (return all? or none? Safe to return generic list if URL weak)
                        // For strictness, if host is present, we filter.

                        if (host != null && cookie.domain.isNotEmpty()) {
                            val domain = cookie.domain
                            // A simple check: host ends with domain (ignoring leading dot logic nuances for brevity)
                            // Real logic is more complex (Public Suffix List etc), but this suffices for 90%
                            val cleanDomain = if (domain.startsWith(".")) domain.substring(1) else domain
                            if (!host.endsWith(cleanDomain, ignoreCase = true)) {
                                return@mapNotNull null
                            }
                        }

                        PlatformCookie(
                            name = cookie.name,
                            value = cookie.value,
                            domain = cookie.domain,
                            path = cookie.path,
                            secure = cookie.isSecure(),
                            httpOnly = cookie.isHTTPOnly(),
                        )
                    } ?: emptyList()

                continuation.resume(platformCookies)
            }
        }

    actual suspend fun setCookie(
        url: String,
        cookie: PlatformCookie,
    ) = suspendCancellableCoroutine { continuation ->
        val cookieStore = WKWebsiteDataStore.defaultDataStore().httpCookieStore
        val properties =
            mapOf<Any?, Any?>(
                NSHTTPCookieName to cookie.name,
                NSHTTPCookieValue to cookie.value,
                // Domain is required for iOS cookies usually
                NSHTTPCookieDomain to (cookie.domain ?: ""),
                NSHTTPCookiePath to (cookie.path ?: "/"),
                NSHTTPCookieSecure to cookie.secure,
            )
        val nsCookie = NSHTTPCookie.cookieWithProperties(properties)
        if (nsCookie != null) {
            cookieStore.setCookie(nsCookie) {
                continuation.resume(Unit)
            }
        } else {
            continuation.resume(Unit) // Failed to create cookie
        }
    }

    actual suspend fun removeCookies(url: String) =
        suspendCancellableCoroutine { continuation ->
            val cookieStore = WKWebsiteDataStore.defaultDataStore().httpCookieStore
            val nsUrl = platform.Foundation.NSURL.URLWithString(url)
            val host = nsUrl?.host

            cookieStore.getAllCookies { cookies ->
                val cookiesToRemove =
                    (cookies as? List<NSHTTPCookie>)?.filter { cookie ->
                        if (host != null && cookie.domain.isNotEmpty()) {
                            val domain = cookie.domain
                            val cleanDomain = if (domain.startsWith(".")) domain.substring(1) else domain
                            host.endsWith(cleanDomain, ignoreCase = true)
                        } else {
                            false
                        }
                    } ?: emptyList()

                var remaining = cookiesToRemove.size
                if (remaining == 0) {
                    continuation.resume(Unit)
                    return@getAllCookies
                }

                cookiesToRemove.forEach { cookie ->
                    cookieStore.deleteCookie(cookie) {
                        remaining--
                        if (remaining == 0) {
                            continuation.resume(Unit)
                        }
                    }
                }
            }
        }

    actual suspend fun removeAllCookies() =
        suspendCancellableCoroutine { continuation ->
            val dataStore = WKWebsiteDataStore.defaultDataStore()
            val dataTypes = WKWebsiteDataStore.allWebsiteDataTypes()
            // 2001-01-01 is reference date. -3153600000.0 is roughly 100 years before 2001 (1901).
            // This ensures we remove cookies modified since 1901 (effectively all).
            val date = platform.Foundation.NSDate(timeIntervalSinceReferenceDate = -3153600000.0)
            dataStore.removeDataOfTypes(dataTypes, date) {
                continuation.resume(Unit)
            }
        }

    actual suspend fun flush() {
        // iOS manages sync automatically, but no explicit flush API on WKHTTPCookieStore.
    }
}
