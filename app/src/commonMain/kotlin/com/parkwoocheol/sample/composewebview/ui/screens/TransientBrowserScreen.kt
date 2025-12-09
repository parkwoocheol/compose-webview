package com.parkwoocheol.sample.composewebview.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.platformDomStorageEnabled
import com.parkwoocheol.composewebview.platformJavaScriptEnabled
import com.parkwoocheol.composewebview.rememberSaveableWebViewStateWithData
import com.parkwoocheol.composewebview.rememberWebViewStateWithData
import com.parkwoocheol.sample.composewebview.ui.components.AppTopBar

@Composable
fun TransientBrowserScreen(onBack: () -> Unit) {
    // HTML content with a counter that increments every second
    val htmlContent = """
        <html>
        <head>
            <style>
                body { 
                    font-family: system-ui, -apple-system, sans-serif; 
                    display: flex; 
                    flex-direction: column;
                    justify-content: center; 
                    align-items: center; 
                    height: 100vh; 
                    margin: 0; 
                    background-color: #f0f9ff;
                    color: #0f172a;
                }
                .counter { font-size: 48px; font-weight: bold; color: #0ea5e9; }
                .label { font-size: 14px; color: #64748b; margin-top: 8px; }
            </style>
        </head>
        <body>
            <div class="counter" id="counter">0</div>
            <div class="label">Seconds elapsed</div>
            <script>
                let count = 0;
                setInterval(() => {
                    count++;
                    document.getElementById('counter').innerText = count;
                }, 1000);
            </script>
        </body>
        </html>
    """.trimIndent()

    // Transient state: Lost on configuration change
    val transientState = rememberWebViewStateWithData(data = htmlContent)

    // Saved state: Persisted across configuration changes
    val savedState = rememberSaveableWebViewStateWithData(data = htmlContent)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Transient vs Saved State",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Rotate your device to see the difference!",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Transient WebView
            WebViewCard(
                title = "Transient State (Resets on Rotate)",
                modifier = Modifier.weight(1f)
            ) {
                ComposeWebView(
                    state = transientState,
                    modifier = Modifier.fillMaxSize(),
                    onCreated = {
                        it.platformJavaScriptEnabled = true
                        it.platformDomStorageEnabled = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved WebView
            WebViewCard(
                title = "Saved State (Persists)",
                modifier = Modifier.weight(1f)
            ) {
                ComposeWebView(
                    state = savedState,
                    modifier = Modifier.fillMaxSize(),
                    onCreated = {
                        it.platformJavaScriptEnabled = true
                        it.platformDomStorageEnabled = true
                    }
                )
            }
        }
    }
}

@Composable
private fun WebViewCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}
