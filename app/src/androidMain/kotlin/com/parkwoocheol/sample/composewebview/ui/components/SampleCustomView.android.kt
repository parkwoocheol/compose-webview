package com.parkwoocheol.sample.composewebview.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.parkwoocheol.composewebview.PlatformCustomView

@Composable
actual fun SampleCustomView(
    view: PlatformCustomView,
    modifier: Modifier,
    onRelease: () -> Unit
) {
    AndroidView(
        factory = { view },
        modifier = modifier,
        onRelease = { onRelease() }
    )
}
