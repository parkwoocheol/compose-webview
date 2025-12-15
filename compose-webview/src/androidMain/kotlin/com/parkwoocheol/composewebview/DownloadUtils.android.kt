package com.parkwoocheol.composewebview

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import java.lang.ref.WeakReference

actual object DownloadUtils {
    private var contextRef: WeakReference<Context>? = null

    fun setContext(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }

    actual fun download(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
        description: String?,
        title: String?,
    ) {
        val context = contextRef?.get() ?: return
        val finalFileName = title ?: URLUtil.guessFileName(url, contentDisposition, mimeType)
        val finalDescription = description ?: "Downloading file..."
        
        val request =
            DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                setDescription(finalDescription)
                setTitle(finalFileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, finalFileName)
            }
        
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}
