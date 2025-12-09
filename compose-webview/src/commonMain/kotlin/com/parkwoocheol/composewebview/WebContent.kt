package com.parkwoocheol.composewebview

/**
 * Sealed interface representing the content to be loaded into the WebView.
 */
sealed interface WebContent {
    /**
     * Represents a URL to be loaded.
     *
     * @property url The URL to load.
     * @property additionalHttpHeaders Optional map of additional HTTP headers to be sent with the request.
     */
    data class Url(
        val url: String,
        val additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) : WebContent

    /**
     * Represents raw data to be loaded into the WebView.
     *
     * @property data The data to load (e.g., HTML string).
     * @property baseUrl The base URL to use for the content.
     * @property encoding The encoding of the data (default is "utf-8").
     * @property mimeType The MIME type of the data (default is null).
     * @property historyUrl The URL to use for the history entry.
     */
    data class Data(
        val data: String,
        val baseUrl: String? = null,
        val encoding: String = "utf-8",
        val mimeType: String? = null,
        val historyUrl: String? = null,
    ) : WebContent

    /**
     * Represents a POST request to be loaded.
     *
     * @property url The URL to post to.
     * @property postData The data to be posted.
     */
    data class Post(
        val url: String,
        val postData: ByteArray,
    ) : WebContent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Post

            if (url != other.url) return false
            if (!postData.contentEquals(other.postData)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = url.hashCode()
            result = 31 * result + postData.contentHashCode()
            return result
        }
    }

    /**
     * Represents a state where the WebView content is managed solely by the [WebViewController]
     * or internal navigation, without a declarative content source.
     */
    data object NavigatorOnly : WebContent
}

internal fun WebContent.withUrl(url: String) =
    when (this) {
        is WebContent.Url -> copy(url = url)
        else -> WebContent.Url(url)
    }
