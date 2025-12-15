package com.parkwoocheol.composewebview

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

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
        val nsUrl = NSURL.URLWithString(url) ?: return
        if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}
