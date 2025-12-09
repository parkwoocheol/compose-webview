package com.parkwoocheol.sample.composewebview.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.PlatformCustomView

@Composable
expect fun SampleCustomView(
    view: PlatformCustomView,
    modifier: Modifier = Modifier,
    onRelease: () -> Unit = {},
)
