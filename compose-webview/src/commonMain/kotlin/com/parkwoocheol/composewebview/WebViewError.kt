package com.parkwoocheol.composewebview

import androidx.compose.runtime.Immutable


/**
 * Represents an error that occurred in the WebView.
 *
 * @property request The request that initiated the error, if available.
 * @property error The error details provided by the WebView.
 */
@Immutable
data class WebViewError(
    val request: PlatformWebResourceRequest?,
    val error: PlatformWebResourceError?
)

