package com.parkwoocheol.sample.composewebview

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.parkwoocheol.sample.composewebview.ui.MainScreen
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        MainScreen()
    }
}
