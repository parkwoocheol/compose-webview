package com.parkwoocheol.sample.composewebview.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.parkwoocheol.sample.composewebview.ui.components.AppTopBar
import com.parkwoocheol.sample.composewebview.ui.components.FeatureCard
import com.parkwoocheol.sample.composewebview.ui.screens.BasicBrowserScreen
import com.parkwoocheol.sample.composewebview.ui.screens.CustomClientScreen
import com.parkwoocheol.sample.composewebview.ui.screens.FullscreenVideoScreen
import com.parkwoocheol.sample.composewebview.ui.screens.HtmlJsScreen
import com.parkwoocheol.sample.composewebview.ui.screens.TransientBrowserScreen
import com.parkwoocheol.sample.composewebview.ui.theme.AppTheme
import com.parkwoocheol.sample.composewebview.ui.theme.Primary
import com.parkwoocheol.sample.composewebview.ui.theme.Secondary
import com.parkwoocheol.sample.composewebview.ui.theme.Tertiary

enum class Screen {
    Main,
    BasicBrowser,
    TransientBrowser,
    HtmlJs,
    FullscreenVideo,
    CustomClient,
}

data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val screen: Screen,
)

@Composable
fun MainScreen() {
    val features =
        listOf(
            Feature(
                "Basic Browser",
                "Standard WebView with navigation controls",
                Icons.Default.Public,
                Primary,
                Screen.BasicBrowser,
            ),
            Feature(
                "Transient State",
                "State lost on config change (lightweight)",
                Icons.Default.Save,
                Secondary,
                Screen.TransientBrowser,
            ),
            Feature(
                "HTML & JS Bridge",
                "Two-way communication with JavaScript",
                Icons.Default.Javascript,
                Tertiary,
                Screen.HtmlJs,
            ),
            Feature(
                "Fullscreen Video",
                "Native fullscreen video support",
                Icons.Default.Videocam,
                // Violet
                Color(0xFF8B5CF6),
                Screen.FullscreenVideo,
            ),
            Feature(
                "Custom Client",
                "Custom WebViewClient & Settings",
                Icons.Default.Settings,
                // Emerald
                Color(0xFF10B981),
                Screen.CustomClient,
            ),
        )

    AppTheme {
        var currentScreen by remember { mutableStateOf(Screen.Main) }

        when (currentScreen) {
            Screen.Main -> {
                Scaffold(
                    topBar = { AppTopBar("Compose WebView") },
                ) { paddingValues ->
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                    ) {
                        items(features) { feature ->
                            FeatureCard(
                                title = feature.title,
                                description = feature.description,
                                icon = feature.icon,
                                color = feature.color,
                                onClick = { currentScreen = feature.screen },
                            )
                        }
                    }
                }
            }
            Screen.BasicBrowser -> BasicBrowserScreen { currentScreen = Screen.Main }
            Screen.TransientBrowser -> TransientBrowserScreen { currentScreen = Screen.Main }
            Screen.HtmlJs -> HtmlJsScreen { currentScreen = Screen.Main }
            Screen.FullscreenVideo -> FullscreenVideoScreen { currentScreen = Screen.Main }
            Screen.CustomClient -> CustomClientScreen { currentScreen = Screen.Main }
        }
    }
}
