package com.parkwoocheol.composewebview

import kotlinx.browser.window

/**
 * JS implementation of [DownloadUtils].
 * Opens the download URL in a new browser tab/window.
 */
actual object DownloadUtils {
    actual fun download(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
        description: String?,
        title: String?,
    ) {
        // In JS/browser, we can trigger a download by opening the URL
        // or creating an anchor element with download attribute
        window.open(url, "_blank")
    }
}
