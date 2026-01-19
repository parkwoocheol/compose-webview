package com.parkwoocheol.sample.composewebview.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.DarkMode
import com.parkwoocheol.composewebview.WebViewSettings
import com.parkwoocheol.composewebview.platformDomStorageEnabled
import com.parkwoocheol.composewebview.platformJavaScriptEnabled
import com.parkwoocheol.composewebview.rememberSaveableWebViewState
import com.parkwoocheol.composewebview.rememberWebViewController
import com.parkwoocheol.sample.composewebview.ui.components.AppTopBar
import com.parkwoocheol.sample.composewebview.ui.components.BrowserControls

@Composable
fun BasicBrowserScreen(onBack: () -> Unit) {
    val state = rememberSaveableWebViewState(url = "https://google.com/")
    val controller = rememberWebViewController()
    var urlInput by remember { mutableStateOf("https://google.com/") }

    // Find on Page state
    var isSearching by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var findMatchCount by remember { mutableIntStateOf(0) }

    // Dark mode state
    var darkMode by remember { mutableStateOf(DarkMode.AUTO) }

    // Update URL input when WebView loads a new page
    androidx.compose.runtime.LaunchedEffect(state.lastLoadedUrl) {
        state.lastLoadedUrl?.let {
            if (it != urlInput) {
                urlInput = it
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Basic Browser",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Find on Page")
                    }
                    IconButton(onClick = {
                        darkMode =
                            when (darkMode) {
                                DarkMode.AUTO -> DarkMode.LIGHT
                                DarkMode.LIGHT -> DarkMode.DARK
                                DarkMode.DARK -> DarkMode.AUTO
                            }
                    }) {
                        Icon(imageVector = Icons.Default.Contrast, contentDescription = "Dark Mode")
                    }
                },
            )
        },
        bottomBar = {
            BrowserControls(
                url = urlInput,
                onUrlChange = { urlInput = it },
                onLoadUrl = { controller.loadUrl(urlInput) },
                canGoBack = controller.canGoBack,
                canGoForward = controller.canGoForward,
                onBack = { controller.navigateBack() },
                onForward = { controller.navigateForward() },
                onReload = { controller.reload() },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (isSearching) {
                FindOnPageToolbar(
                    text = searchText,
                    onTextChange = {
                        searchText = it
                        controller.findAllAsync(it)
                    },
                    matchCount = findMatchCount,
                    onNext = { controller.findNext(true) },
                    onPrevious = { controller.findNext(false) },
                    onClose = {
                        isSearching = false
                        searchText = ""
                        controller.clearMatches()
                    },
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                ComposeWebView(
                    state = state,
                    controller = controller,
                    settings = WebViewSettings(darkMode = darkMode),
                    modifier = Modifier.fillMaxSize(),
                    onCreated = { webView ->
                        webView.platformJavaScriptEnabled = true
                        webView.platformDomStorageEnabled = true
                    },
                    onFindResultReceived = { activeIndex: Int, count: Int, isDone: Boolean ->
                        findMatchCount = count
                    },
                )
            }
        }
    }
}

@Composable
private fun FindOnPageToolbar(
    text: String,
    onTextChange: (String) -> Unit,
    matchCount: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search on page") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
            )

            if (text.isNotEmpty()) {
                Text(
                    text = "$matchCount matches",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }

            IconButton(onClick = onPrevious, enabled = text.isNotEmpty()) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous")
            }

            IconButton(onClick = onNext, enabled = text.isNotEmpty()) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next")
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}
