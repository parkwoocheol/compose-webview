package com.parkwoocheol.sample.composewebview

import androidx.compose.ui.window.ComposeUIViewController
import com.parkwoocheol.sample.composewebview.ui.MainScreen
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName")
fun MainViewController(): UIViewController = ComposeUIViewController {
    MainScreen()
}
