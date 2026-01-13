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
 * Defines the dark mode strategy for WebView.
 */
enum class DarkMode {
    /**
     * Follow system/App theme.
     */
    AUTO,

    /**
     * Force light mode.
     */
    LIGHT,

    /**
     * Force dark mode.
     */
    DARK,
}

/**
 * Configuration settings for WebView behavior across all platforms.
 *
 * This class provides a unified interface for configuring WebView settings.
 * Note that not all settings are supported on all platforms - refer to the
 * platform support tables below.
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
 * **Platform Support Summary:**
 * | Setting | Android | iOS | Desktop | Web |
 * |---------|:-------:|:---:|:-------:|:---:|
 * | userAgent | ✅ | ✅ | ✅ | ❌ |
 * | javaScriptEnabled | ✅ | ✅ | ✅ | ❌ |
 * | domStorageEnabled | ✅ | ✅ | ⚠️ | ❌ |
 * | cacheMode | ✅ | ⚠️ | ⚠️ | ❌ |
 * | allowFileAccess | ✅ | ⚠️ | ⚠️ | ❌ |
 * | allowContentAccess | ✅ | ❌ | ❌ | ❌ |
 * | supportZoom | ✅ | ⚠️* | ✅ | ❌ |
 * | loadWithOverviewMode | ✅ | ❌ | ❌ | ❌ |
 * | useWideViewPort | ✅ | ⚠️ | ⚠️ | ❌ |
 * | allowFileAccessFromFileURLs | ✅ | ⚠️ | ⚠️ | ❌ |
 * | allowUniversalAccessFromFileURLs | ✅ | ⚠️ | ⚠️ | ❌ |
 * | mediaPlaybackRequiresUserAction | ✅ | ✅ | ⚠️ | ❌ |
 *
 * *iOS: User pinch-to-zoom only, no programmatic zoom
 *
 * @property userAgent Custom user agent string. If null, platform default is used.
 * @property javaScriptEnabled Enable JavaScript execution. Default: true.
 * @property domStorageEnabled Enable DOM storage API. Default: true.
 * @property cacheMode Cache behavior mode. Default: CacheMode.DEFAULT.
 * @property allowFileAccess Allow access to file:// URLs. Default: false (security).
 * @property allowContentAccess Allow access to content:// URLs (Android only). Default: false.
 * @property supportZoom Enable zoom controls. Default: true. (iOS: pinch-to-zoom only)
 * @property loadWithOverviewMode Load page with overview mode (Android only). Default: true.
 * @property useWideViewPort Enable viewport meta tag support. Default: true.
 * @property allowFileAccessFromFileURLs Allow file access from file URLs. Default: false (security).
 * @property allowUniversalAccessFromFileURLs Allow universal access from file URLs. Default: false (security).
 * @property mediaPlaybackRequiresUserAction Require user gesture for media playback. Default: false.
 * @property darkMode Dark mode strategy for the WebView. Default: DarkMode.AUTO.
 * @property interceptedSchemes List of URL schemes to intercept (iOS only).
 * For example, if you add "my-app", requests to "my-app://" will be handled via [ComposeWebViewClient.shouldInterceptRequest].
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
    val darkMode: DarkMode = DarkMode.AUTO,
    val interceptedSchemes: List<String> = emptyList(),
) {
    companion object {
        /**
         * Default WebViewSettings with recommended secure defaults.
         */
        val Default = WebViewSettings()
    }
}
