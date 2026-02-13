package com.parkwoocheol.composewebview

/**
 * Controls how the underlying platform WebView instance is handled when the Composable leaves composition.
 */
enum class WebViewReleaseStrategy {
    /**
     * Default behavior. Release and destroy the platform WebView when it leaves composition.
     */
    DestroyOnRelease,

    /**
     * Keep the platform WebView instance alive across composition exits and re-entries.
     *
     * This is currently supported on Android and iOS.
     */
    KeepAlive,
}
