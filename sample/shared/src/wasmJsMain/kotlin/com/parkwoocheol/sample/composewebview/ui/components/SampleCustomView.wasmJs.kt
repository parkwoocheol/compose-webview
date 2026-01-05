package com.parkwoocheol.sample.composewebview.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.PlatformCustomView

@Composable
actual fun SampleCustomView(
    view: PlatformCustomView,
    modifier: Modifier,
    onRelease: () -> Unit,
) {
    // WASM doesn't have a native view system like Android/iOS/Desktop
    // Custom views are not supported in the same way
    // Using an empty Box as placeholder
    Box(modifier = modifier)
}
