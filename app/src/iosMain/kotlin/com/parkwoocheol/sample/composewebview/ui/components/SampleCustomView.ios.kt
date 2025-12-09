package com.parkwoocheol.sample.composewebview.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.parkwoocheol.composewebview.PlatformCustomView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun SampleCustomView(
    view: PlatformCustomView,
    modifier: Modifier,
    onRelease: () -> Unit
) {
    UIKitView(
        factory = { view },
        modifier = modifier,
        onRelease = { onRelease() }
    )
}
