package com.parkwoocheol.composewebview

import androidx.compose.runtime.Immutable

/**
 * Defines the cache mode for WebView.
 *
 * **Platform Support:**
 * | Platform | Support | Notes |
 * |----------|---------|-------|
 * | Android  | ✅ Full | All modes supported |
 * | iOS      | ⚠️ Partial | Limited cache control |
 * | Desktop  | ⚠️ Partial | CEF cache settings |
 * | Web      | ❌ None | Browser controlled |
 */
enum class CacheMode {
    /**
     * Default cache mode. Uses cache when available.
     */
    DEFAULT,

    /**
     * Use cache if content is available, otherwise load from network.
     * Android: LOAD_CACHE_ELSE_NETWORK
     */
    CACHE_ELSE_NETWORK,

    /**
     * Don't use cache, always load from network.
     * Android: LOAD_NO_CACHE
     */
    NO_CACHE,

    /**
     * Only use cache, don't load from network.
     * Android: LOAD_CACHE_ONLY
     */
    CACHE_ONLY,
}

/**
 * Configuration settings for WebView behavior across all platforms.
 *
 * This class provides a unified interface for configuring WebView settings.
 * Note that not all settings are supported on all platforms - refer to the
 * platform support documentation for each property.
 *
 * **Example:**
 * ```kotlin
 * val settings = WebViewSettings(
 *     userAgent = "MyApp/1.0",
 *     javaScriptEnabled = true,
 *     cacheMode = CacheMode.CACHE_ELSE_NETWORK
 * )
 *
 * ComposeWebView(
 *     state = webViewState,
 *     settings = settings
 * )
 * ```
 *
 * @property userAgent Custom user agent string. If null, platform default is used.
 * @property javaScriptEnabled Enable JavaScript execution. Default: true.
 * @property domStorageEnabled Enable DOM storage API. Default: true.
 * @property cacheMode Cache behavior mode. Default: CacheMode.DEFAULT.
 * @property allowFileAccess Allow access to file:// URLs. Default: false (security).
 * @property allowContentAccess Allow access to content:// URLs (Android). Default: false.
 * @property supportZoom Enable zoom controls. Default: true.
 * @property loadWithOverviewMode Load page with overview mode (Android). Default: true.
 * @property useWideViewPort Enable viewport meta tag support. Default: true.
 * @property allowFileAccessFromFileURLs Allow file access from file URLs. Default: false (security).
 * @property allowUniversalAccessFromFileURLs Allow universal access from file URLs. Default: false (security).
 * @property mediaPlaybackRequiresUserAction Require user gesture for media playback. Default: false.
 */
@Immutable
data class WebViewSettings(
    val userAgent: String? = null,
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val cacheMode: CacheMode = CacheMode.DEFAULT,
    val allowFileAccess: Boolean = false,
    val allowContentAccess: Boolean = false,
    val supportZoom: Boolean = true,
    val loadWithOverviewMode: Boolean = true,
    val useWideViewPort: Boolean = true,
    val allowFileAccessFromFileURLs: Boolean = false,
    val allowUniversalAccessFromFileURLs: Boolean = false,
    val mediaPlaybackRequiresUserAction: Boolean = false,
) {
    companion object {
        /**
         * Default WebViewSettings with recommended secure defaults.
         */
        val Default = WebViewSettings()
    }
}
