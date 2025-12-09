package com.parkwoocheol.sample.composewebview.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.platformDomStorageEnabled
import com.parkwoocheol.composewebview.platformJavaScriptEnabled
import com.parkwoocheol.composewebview.rememberSaveableWebViewStateWithData
import com.parkwoocheol.composewebview.rememberWebViewJsBridge
import com.parkwoocheol.sample.composewebview.ui.components.AppTopBar
import androidx.compose.material3.ButtonDefaults
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, val age: Int)

@Serializable
data class UserResponse(val success: Boolean, val message: String)

@Composable
fun HtmlJsScreen(onBack: () -> Unit) {
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { 
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    background-color: #f8fafc;
                    color: #0f172a;
                    padding: 20px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    height: 100vh;
                    margin: 0;
                }
                .card {
                    background: white;
                    padding: 24px;
                    border-radius: 16px;
                    box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
                    text-align: center;
                    max-width: 300px;
                    width: 100%;
                }
                h2 { color: #4f46e5; margin-top: 0; }
                button {
                    background-color: #4f46e5;
                    color: white;
                    border: none;
                    padding: 12px 24px;
                    border-radius: 8px;
                    font-size: 16px;
                    font-weight: 600;
                    margin: 8px 0;
                    cursor: pointer;
                    width: 100%;
                    transition: background-color 0.2s;
                }
                button:active { background-color: #4338ca; }
                #message {
                    margin-top: 16px;
                    padding: 12px;
                    background-color: #f1f5f9;
                    border-radius: 8px;
                    font-size: 14px;
                    color: #475569;
                }
            </style>
        </head>
        <body>
            <div class="card">
                <h2>JS Bridge Demo</h2>
                <p>This is HTML content.</p>
                <button onclick="callNative()">Call Native (User)</button>
                <button onclick="callNativeNoReturn()">Call Native (Log)</button>
                <div id="message">Waiting for events...</div>
            </div>

            <script>
                function callNative() {
                    window.AppBridge.call('updateUser', { name: 'Web User', age: 25 })
                        .then(response => {
                            document.getElementById('message').innerText = 'Native response: ' + response.message;
                        })
                        .catch(error => {
                            document.getElementById('message').innerText = 'Error: ' + error;
                        });
                }
                
                function callNativeNoReturn() {
                     window.AppBridge.call('log', 'Hello from Web!');
                }

                window.AppBridge.on('nativeEvent', (data) => {
                    document.getElementById('message').innerText = 'Received event: ' + data.message;
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    val state = rememberSaveableWebViewStateWithData(data = htmlContent)
    val bridge = rememberWebViewJsBridge()
    val logs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(bridge) {
        bridge.register<User, UserResponse>("updateUser") { user ->
            logs.add("JS called 'updateUser': $user")
            UserResponse(true, "Updated ${user.name} (${user.age})")
        }

        bridge.register<String, Unit>("log") { message ->
            logs.add("JS called 'log': $message")
        }
    }

    // Auto-scroll logs
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "HTML & JS Bridge",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // WebView Area (Top Half)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                ComposeWebView(
                    state = state,
                    jsBridge = bridge,
                    modifier = Modifier.fillMaxSize(),
                    onCreated = {
                        it.platformJavaScriptEnabled = true
                        it.platformDomStorageEnabled = true
                    }
                )
            }

            // Command Center (Bottom Half)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Command Center",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                bridge.emit("nativeEvent", mapOf("message" to "Hello from Native!"))
                                logs.add("Sent event 'nativeEvent' to JS")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send Event to JS")
                        }
                        Button(
                            onClick = { logs.clear() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text("Clear Logs")
                        }
                    }

                    Text(
                        text = "Communication Log",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs) { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
