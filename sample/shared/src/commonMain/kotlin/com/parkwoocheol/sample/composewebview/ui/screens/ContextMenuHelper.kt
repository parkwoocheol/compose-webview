package com.parkwoocheol.sample.composewebview.ui.screens

import com.parkwoocheol.composewebview.PlatformActionModeCallback
import com.parkwoocheol.composewebview.WebView

expect fun configureAndroidContextMenu(
    webView: WebView,
    callback: PlatformActionModeCallback?,
): PlatformActionModeCallback?
