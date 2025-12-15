package com.parkwoocheol.composewebview

expect object DownloadUtils {
    fun download(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
        description: String? = null,
        title: String? = null,
    )
}
