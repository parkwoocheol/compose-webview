package com.parkwoocheol.sample.composewebview.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.PlatformCookieManager
import com.parkwoocheol.composewebview.WebViewSettings
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import com.parkwoocheol.composewebview.client.shouldInterceptRequest
import com.parkwoocheol.composewebview.createPlatformWebResourceResponse
import com.parkwoocheol.composewebview.platformDomStorageEnabled
import com.parkwoocheol.composewebview.platformJavaScriptEnabled
import com.parkwoocheol.composewebview.platformSupportZoom
import com.parkwoocheol.composewebview.rememberSaveableWebViewState
import com.parkwoocheol.sample.composewebview.ui.components.AppTopBar
import kotlinx.coroutines.launch

@Composable
fun CustomClientScreen(onBack: () -> Unit) {
    val state = rememberSaveableWebViewState(url = "https://whatismybrowser.com")
    var jsEnabled by remember { mutableStateOf(true) }
    var domStorageEnabled by remember { mutableStateOf(true) }
    var zoomEnabled by remember { mutableStateOf(true) }
    var interceptionEnabled by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Custom Client that logs page starts
    // Custom Client with Request Interception
    val client =
        remember(interceptionEnabled) {
            object : ComposeWebViewClient() {
                override fun shouldInterceptRequest(
                    view: com.parkwoocheol.composewebview.WebView?,
                    request: com.parkwoocheol.composewebview.PlatformWebResourceRequest?,
                ): com.parkwoocheol.composewebview.PlatformWebResourceResponse? {
                    if (interceptionEnabled && request?.url?.contains("intercept-test") == true) {
                        val responseData = "<html><body><h1>Intercepted!</h1><p>This content was served from native code.</p></body></html>"
                        return createPlatformWebResourceResponse(
                            mimeType = "text/html",
                            encoding = "UTF-8",
                            data = responseData.encodeToByteArray(),
                        )
                    }
                    return null
                }
            }.apply {
                // For iOS, we need to register the scheme in settings.interceptedSchemes
                // This example also demonstrates the DSL extension
                shouldInterceptRequest { webView, request ->
                    if (interceptionEnabled && request?.url?.contains("intercept-test") == true) {
                        createPlatformWebResourceResponse(
                            mimeType = "text/html",
                            encoding = "UTF-8",
                            data = "<html><body><h1>Intercepted (iOS)!</h1></body></html>".encodeToByteArray(),
                        )
                    } else {
                        null
                    }
                }
            }
        }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Custom Configuration",
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Settings Panel
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "WebView Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )

                    SettingSwitch("JavaScript Enabled", jsEnabled) { jsEnabled = it }
                    SettingSwitch("DOM Storage Enabled", domStorageEnabled) { domStorageEnabled = it }
                    SettingSwitch("Zoom Support", zoomEnabled) { zoomEnabled = it }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "New Features",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                    )

                    SettingSwitch("Request Interception", interceptionEnabled) { interceptionEnabled = it }

                    Button(
                        onClick = { state.content = com.parkwoocheol.composewebview.WebContent.Url("https://intercept-test.com") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = interceptionEnabled,
                    ) {
                        Text("Test Interception")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                PlatformCookieManager.removeCookies("https://whatismybrowser.com")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(),
                    ) {
                        Text("Remove Cookies for Current Site")
                    }
                }
            }

            // WebView
            Box(modifier = Modifier.weight(1f)) {
                ComposeWebView(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    client = client,
                    settings =
                        WebViewSettings(
                            // For iOS interception demo
                            interceptedSchemes = listOf("https"),
                        ),
                    onCreated = { webView ->
                        webView.platformJavaScriptEnabled = jsEnabled
                        webView.platformDomStorageEnabled = domStorageEnabled
                        webView.platformSupportZoom = zoomEnabled
                    },
                    onStartActionMode = { webView, callback ->
                        // Android Custom Context Menu Demo (handled by platform-specific cast in sample if needed)
                        // This logic is platform-specific, so we use a helper defined in sample
                        configureAndroidContextMenu(webView, callback)
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
