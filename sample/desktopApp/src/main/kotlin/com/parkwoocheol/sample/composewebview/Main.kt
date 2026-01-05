package com.parkwoocheol.sample.composewebview

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.parkwoocheol.sample.composewebview.ui.MainScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose WebView Sample"
    ) {
        MainScreen()
    }
}
