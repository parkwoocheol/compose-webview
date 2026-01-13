package com.parkwoocheol.sample.composewebview.ui.screens

import com.parkwoocheol.composewebview.PlatformActionModeCallback
import com.parkwoocheol.composewebview.WebView

actual fun configureAndroidContextMenu(
    webView: WebView,
    callback: PlatformActionModeCallback?,
): PlatformActionModeCallback? {
    return object : android.view.ActionMode.Callback {
        override fun onCreateActionMode(
            mode: android.view.ActionMode?,
            menu: android.view.Menu?,
        ): Boolean {
            callback?.onCreateActionMode(mode, menu)
            menu?.add("App Search")?.setOnMenuItemClickListener {
                // Handle custom action
                true
            }
            return true
        }

        override fun onPrepareActionMode(
            mode: android.view.ActionMode?,
            menu: android.view.Menu?,
        ): Boolean =
            callback?.onPrepareActionMode(
                mode,
                menu,
            ) ?: true

        override fun onActionItemClicked(
            mode: android.view.ActionMode?,
            item: android.view.MenuItem?,
        ): Boolean =
            callback?.onActionItemClicked(
                mode,
                item,
            ) ?: false

        override fun onDestroyActionMode(mode: android.view.ActionMode?) = callback?.onDestroyActionMode(mode) ?: Unit
    }
}
