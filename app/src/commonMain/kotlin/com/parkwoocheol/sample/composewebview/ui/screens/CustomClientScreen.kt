package com.parkwoocheol.sample.composewebview.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.client.ComposeWebViewClient
import com.parkwoocheol.composewebview.rememberSaveableWebViewState
import com.parkwoocheol.composewebview.platformBuiltInZoomControls
import com.parkwoocheol.composewebview.platformDisplayZoomControls
import com.parkwoocheol.composewebview.platformDomStorageEnabled
import com.parkwoocheol.composewebview.platformJavaScriptEnabled
import com.parkwoocheol.composewebview.platformSupportZoom
import com.parkwoocheol.sample.composewebview.ui.components.AppTopBar

@Composable
fun CustomClientScreen(onBack: () -> Unit) {
    val state = rememberSaveableWebViewState(url = "https://whatismybrowser.com")
    var jsEnabled by remember { mutableStateOf(true) }
    var domStorageEnabled by remember { mutableStateOf(true) }
    var zoomEnabled by remember { mutableStateOf(true) }

    // Custom Client that logs page starts
    val client = remember {
        object : ComposeWebViewClient() {
            // You can override methods here if needed, or just use the default
            // For this sample, we just use the default but show how to pass it
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Custom Configuration",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Settings Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "WebView Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    SettingSwitch("JavaScript Enabled", jsEnabled) { jsEnabled = it }
                    SettingSwitch("DOM Storage Enabled", domStorageEnabled) { domStorageEnabled = it }
                    SettingSwitch("Zoom Support", zoomEnabled) { zoomEnabled = it }
                }
            }

            // WebView
            Box(modifier = Modifier.weight(1f)) {
                ComposeWebView(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    client = client,
                    onCreated = { webView ->
                        // Apply settings dynamically
                        // Note: In a real app, you might want to trigger a reload or re-creation 
                        // if some settings require it, but most WebView settings can be updated on the fly.
                        webView.platformJavaScriptEnabled = jsEnabled
                        webView.platformDomStorageEnabled = domStorageEnabled
                        webView.platformSupportZoom = zoomEnabled
                        webView.platformBuiltInZoomControls = zoomEnabled
                        webView.platformDisplayZoomControls = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
