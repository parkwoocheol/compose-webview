package com.parkwoocheol.composewebview.client

import com.parkwoocheol.composewebview.WebView

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class ComposeWebChromeClient() {
    open fun onProgressChanged(
        view: WebView?,
        newProgress: Int,
    )
}
