package com.parkwoocheol.sample.composewebview.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.parkwoocheol.composewebview.PlatformCustomView

@Composable
actual fun SampleCustomView(
    view: PlatformCustomView,
    modifier: Modifier,
    onRelease: () -> Unit,
) {
    SwingPanel(
        factory = { view },
        modifier = modifier,
    )
}
