package com.parkwoocheol.composewebview

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
        // Desktop can use java.awt.Desktop or KCEF download handler.
        // For now, stub or print.
        println("Download requested: $url (Not implemented for Desktop)")
    }
}
