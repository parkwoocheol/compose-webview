package com.parkwoocheol.composewebview

import androidx.compose.runtime.Immutable

/**
 * Categorizes the type of error that occurred in the WebView.
 *
 * This provides a platform-agnostic way to identify common error types
 * across different WebView implementations.
 */
enum class WebViewErrorType {
    /**
     * Network-related errors (connection failed, timeout, etc.)
     */
    NETWORK_ERROR,

    /**
     * SSL/TLS certificate errors
     */
    SSL_ERROR,

    /**
     * Request timeout
     */
    TIMEOUT,

    /**
     * File not found (404) or similar HTTP errors
     */
    FILE_NOT_FOUND,

    /**
     * Too many redirects
     */
    TOO_MANY_REDIRECTS,

    /**
     * Unsupported URL scheme
     */
    UNSUPPORTED_SCHEME,

    /**
     * Authentication error
     */
    AUTHENTICATION,

    /**
     * Proxy authentication error
     */
    PROXY_AUTHENTICATION,

    /**
     * Host lookup failed
     */
    HOST_LOOKUP,

    /**
     * Unsupported authentication scheme
     */
    UNSUPPORTED_AUTH_SCHEME,

    /**
     * Generic I/O error
     */
    IO_ERROR,

    /**
     * File error
     */
    FILE_ERROR,

    /**
     * Unknown or uncategorized error
     */
    UNKNOWN,
}

/**
 * Represents an error that occurred in the WebView.
 *
 * This class provides a unified error representation across all platforms,
 * while preserving platform-specific error details for advanced use cases.
 *
 * **Platform Support:**
 * | Platform | Support | Notes |
 * |----------|---------|-------|
 * | Android  | ✅ Full | WebResourceError |
 * | iOS      | ✅ Full | NSError |
 * | Desktop  | ⚠️ Partial | CEF error codes |
 * | Web      | ❌ Limited | No detailed error info |
 *
 * @property type The categorized error type.
 * @property errorCode Platform-specific error code.
 * @property description Human-readable error description.
 * @property request The request that initiated the error, if available.
 * @property platformError The platform-specific error object for advanced use cases.
 */
@Immutable
data class WebViewError(
    val type: WebViewErrorType = WebViewErrorType.UNKNOWN,
    val errorCode: Int = 0,
    val description: String = "Unknown error",
    val request: PlatformWebResourceRequest? = null,
    val platformError: PlatformWebResourceError? = null,
) {
    /**
     * Legacy constructor for backward compatibility.
     * Automatically infers error type and description from platform error.
     */
    @Deprecated(
        message = "Use primary constructor with type, errorCode, and description",
        replaceWith =
            ReplaceWith(
                "WebViewError(" +
                    "type = inferErrorType(error), " +
                    "errorCode = error?.errorCode ?: 0, " +
                    "description = error?.description ?: \"Unknown error\", " +
                    "request = request, " +
                    "platformError = error" +
                    ")",
            ),
    )
    constructor(
        request: PlatformWebResourceRequest?,
        error: PlatformWebResourceError?,
    ) : this(
        type = WebViewErrorType.UNKNOWN,
        errorCode = 0,
        description = "Unknown error",
        request = request,
        platformError = error,
    )
}
