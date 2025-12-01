package com.parkwoocheol.sample.composewebview

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.BottomAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.CustomViewState
import com.parkwoocheol.composewebview.rememberWebViewController
import com.parkwoocheol.composewebview.rememberWebViewState
import com.parkwoocheol.composewebview.rememberSaveableWebViewState
import com.parkwoocheol.composewebview.rememberWebViewStateWithData
import com.parkwoocheol.composewebview.rememberSaveableWebViewStateWithData
import com.parkwoocheol.composewebview.rememberWebViewJsBridge
import com.parkwoocheol.sample.composewebview.ui.theme.ComposewebviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposewebviewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SampleApp()
                }
            }
        }
    }
}

enum class Screen {
    Home,
    BasicBrowser,
    TransientBrowser,
    HtmlJs,
    Video,
    CustomClient
}

@Composable
fun SampleApp() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = Screen.Home
    }

    when (currentScreen) {
        Screen.Home -> SampleHome(onNavigate = { currentScreen = it })
        Screen.BasicBrowser -> BrowserScreen(onBack = { currentScreen = Screen.Home })
        Screen.TransientBrowser -> TransientBrowserScreen(onBack = { currentScreen = Screen.Home })
        Screen.HtmlJs -> HtmlJsScreen(onBack = { currentScreen = Screen.Home })
        Screen.Video -> VideoScreen(onBack = { currentScreen = Screen.Home })
        Screen.CustomClient -> CustomClientScreen(onBack = { currentScreen = Screen.Home })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleHome(onNavigate: (Screen) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ComposeWebView")
                        Text(
                            "Sample Demos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            // Header description
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        "Explore different WebView integration examples",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Sample cards
            SampleCard(
                title = "Basic Browser (Persistent)",
                description = "State survives rotation (rememberSaveable)",
                icon = Icons.Filled.Search,
                onClick = { onNavigate(Screen.BasicBrowser) }
            )

            SampleCard(
                title = "Transient Browser",
                description = "State lost on rotation (remember)",
                icon = Icons.Filled.Refresh,
                onClick = { onNavigate(Screen.TransientBrowser) }
            )

            SampleCard(
                title = "HTML & JS Interaction",
                description = "JSBridge communication between Kotlin and JavaScript",
                icon = Icons.Filled.Build,
                onClick = { onNavigate(Screen.HtmlJs) }
            )

            SampleCard(
                title = "Fullscreen Video",
                description = "Custom view handling for fullscreen video playback",
                icon = Icons.Filled.PlayArrow,
                onClick = { onNavigate(Screen.Video) }
            )

            SampleCard(
                title = "Custom Client (Block URL)",
                description = "Custom WebViewClient with URL filtering",
                icon = Icons.Filled.Settings,
                onClick = { onNavigate(Screen.CustomClient) }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SampleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(onBack: () -> Unit) {
    val url = "https://google.com"
    val state = rememberSaveableWebViewState(url = url)
    val controller = rememberWebViewController()
    var textFieldValue by remember(state.lastLoadedUrl) {
        mutableStateOf(TextFieldValue(state.lastLoadedUrl ?: url))
    }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        TextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Enter URL") },
                            trailingIcon = {
                                IconButton(onClick = { controller.loadUrl(textFieldValue.text) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Go")
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home")
                        }
                    },
                    actions = {
                        IconButton(onClick = { controller.reload() }) {
                            Icon(Icons.Filled.Refresh, "Reload")
                        }
                    }
                )
                val loadingState = state.loadingState
                if (loadingState is com.parkwoocheol.composewebview.LoadingState.Loading) {
                    val progress = loadingState.progress
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = { controller.navigateBack() },
                    enabled = controller.canGoBack
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                IconButton(
                    onClick = { controller.navigateForward() },
                    enabled = controller.canGoForward
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Forward")
                }
            }
        }
    ) { paddingValues ->
        ComposeWebView(
            state = state,
            controller = controller,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onCreated = { webView ->
                webView.settings.javaScriptEnabled = true
            },
            loadingContent = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            errorContent = { errors ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error Occurred", color = MaterialTheme.colorScheme.onErrorContainer)
                        Button(onClick = { controller.reload() }) { Text("Retry") }
                    }
                }
            }
        )
    }
}

@kotlinx.serialization.Serializable
data class ToastData(val message: String)

@kotlinx.serialization.Serializable
data class AppInfo(val version: String, val build: Int, val name: String)

@kotlinx.serialization.Serializable
data class CalculateRequest(val a: Int, val b: Int, val operation: String)

@kotlinx.serialization.Serializable
data class CalculateResponse(val result: Int, val operation: String)

@kotlinx.serialization.Serializable
data class UserData(val name: String, val age: Int, val email: String)

@kotlinx.serialization.Serializable
data class NativeEvent(val type: String, val timestamp: Long, val data: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HtmlJsScreen(onBack: () -> Unit) {
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: sans-serif;
                    padding: 20px;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                }
                .container {
                    background: rgba(255,255,255,0.1);
                    backdrop-filter: blur(10px);
                    padding: 20px;
                    border-radius: 12px;
                    margin-bottom: 20px;
                }
                button {
                    padding: 12px 24px;
                    font-size: 16px;
                    margin: 8px 5px;
                    border: none;
                    border-radius: 8px;
                    background: white;
                    color: #667eea;
                    font-weight: bold;
                    cursor: pointer;
                    transition: transform 0.2s;
                }
                button:active { transform: scale(0.95); }
                input {
                    padding: 10px;
                    font-size: 16px;
                    border-radius: 8px;
                    border: 2px solid white;
                    margin: 5px;
                    width: 60px;
                }
                .result {
                    margin-top: 10px;
                    padding: 15px;
                    background: rgba(255,255,255,0.2);
                    border-radius: 8px;
                    min-height: 20px;
                }
                #eventLog {
                    margin-top: 20px;
                    padding: 15px;
                    background: rgba(0,0,0,0.3);
                    border-radius: 8px;
                    min-height: 50px;
                }
                .success { background-color: rgba(76, 175, 80, 0.3) !important; }
                h2 { margin-top: 0; }
            </style>
        </head>
        <body>
            <h1>🌉 JSBridge Demo</h1>

            <div class="container">
                <h2>1️⃣ Simple Call (Toast)</h2>
                <button onclick="callToast()">Show Toast</button>
                <p style="font-size: 14px; opacity: 0.8;">Calls Native without return value</p>
            </div>

            <div class="container">
                <h2>2️⃣ Get Data from Native</h2>
                <button onclick="getAppInfo()">Get App Info</button>
                <div class="result" id="appInfoResult">Click button to get app info</div>
            </div>

            <div class="container">
                <h2>3️⃣ Calculator (Input/Output)</h2>
                <input type="number" id="num1" value="10" />
                <select id="operation">
                    <option value="add">+</option>
                    <option value="subtract">-</option>
                    <option value="multiply">×</option>
                    <option value="divide">÷</option>
                </select>
                <input type="number" id="num2" value="5" />
                <button onclick="calculate()">Calculate</button>
                <div class="result" id="calcResult">Result will appear here</div>
            </div>

            <div class="container">
                <h2>4️⃣ Submit User Data</h2>
                <input type="text" id="userName" placeholder="Name" style="width: 120px;" />
                <input type="number" id="userAge" placeholder="Age" />
                <input type="email" id="userEmail" placeholder="Email" style="width: 150px;" />
                <button onclick="submitUser()">Submit</button>
                <div class="result" id="userResult">Fill form and submit</div>
            </div>

            <div class="container">
                <h2>5️⃣ Native Events (Kotlin → JS)</h2>
                <div id="eventLog">Waiting for events from Native... (Press 'Emit Event' button in Top Bar)</div>
                <p style="font-size: 14px; opacity: 0.8;">Press the button in the top bar to receive events</p>
            </div>

            <script>
                // 1. Simple toast call
                async function callToast() {
                    if (!window.AppBridge) { alert('Bridge not ready'); return; }
                    try {
                        await window.AppBridge.call('showToast', { message: 'Hello from Web! 👋' });
                    } catch (e) { alert('Error: ' + e); }
                }

                // 2. Get app info
                async function getAppInfo() {
                    if (!window.AppBridge) { alert('Bridge not ready'); return; }
                    const result = document.getElementById('appInfoResult');
                    try {
                        result.innerText = '⏳ Loading...';
                        const info = await window.AppBridge.call('getAppInfo');
                        result.innerText = '✅ Name: ' + info.name + '\\nVersion: ' + info.version + '\\nBuild: ' + info.build;
                        result.classList.add('success');
                    } catch (e) {
                        result.innerText = '❌ Error: ' + e;
                    }
                }

                // 3. Calculator
                async function calculate() {
                    if (!window.AppBridge) { alert('Bridge not ready'); return; }
                    const result = document.getElementById('calcResult');
                    try {
                        const a = parseInt(document.getElementById('num1').value);
                        const b = parseInt(document.getElementById('num2').value);
                        const op = document.getElementById('operation').value;

                        result.innerText = '⏳ Calculating...';
                        const response = await window.AppBridge.call('calculate', {
                            a: a,
                            b: b,
                            operation: op
                        });
                        result.innerText = '✅ Result: ' + response.result + ' (operation: ' + response.operation + ')';
                        result.classList.add('success');
                    } catch (e) {
                        result.innerText = '❌ Error: ' + e;
                    }
                }

                // 4. Submit user data
                async function submitUser() {
                    if (!window.AppBridge) { alert('Bridge not ready'); return; }
                    const result = document.getElementById('userResult');
                    try {
                        const name = document.getElementById('userName').value;
                        const age = parseInt(document.getElementById('userAge').value);
                        const email = document.getElementById('userEmail').value;

                        if (!name || !age || !email) {
                            result.innerText = '⚠️ Please fill all fields';
                            return;
                        }

                        result.innerText = '⏳ Submitting...';
                        const response = await window.AppBridge.call('submitUser', {
                            name: name,
                            age: age,
                            email: email
                        });
                        result.innerText = '✅ ' + response;
                        result.classList.add('success');
                    } catch (e) {
                        result.innerText = '❌ Error: ' + e;
                    }
                }

                // 5. Listen for events from Native
                if (window.AppBridge) {
                    window.AppBridge.on('nativeEvent', (data) => {
                        const log = document.getElementById('eventLog');
                        log.innerText = '📨 Event Received!\\n' + JSON.stringify(data, null, 2);
                        log.classList.add('success');
                        setTimeout(() => log.classList.remove('success'), 1000);
                    });
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    val state = rememberSaveableWebViewStateWithData(
        data = htmlContent,
        mimeType = "text/html",
        encoding = "UTF-8"
    )
    val controller = rememberWebViewController()
    val context = LocalContext.current
    val bridge = rememberWebViewJsBridge()

    LaunchedEffect(bridge) {
        // 1. Toast handler
        bridge.register<ToastData, String>("showToast") { data ->
            Toast.makeText(context, data.message, Toast.LENGTH_SHORT).show()
            "Success"
        }

        // 2. Get app info handler
        bridge.register<AppInfo>("getAppInfo") {
            AppInfo(
                version = "1.0.0",
                build = 100,
                name = "ComposeWebView Sample"
            )
        }

        // 3. Calculator handler
        bridge.register<CalculateRequest, CalculateResponse>("calculate") { req ->
            val result = when (req.operation) {
                "add" -> req.a + req.b
                "subtract" -> req.a - req.b
                "multiply" -> req.a * req.b
                "divide" -> if (req.b != 0) req.a / req.b else 0
                else -> 0
            }
            CalculateResponse(result = result, operation = req.operation)
        }

        // 4. User data handler
        bridge.register<UserData, String>("submitUser") { userData ->
            Log.d("JSBridge", "User submitted: $userData")
            "User ${userData.name} (${userData.age}) registered successfully!"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HTML & JS") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            bridge.emit("nativeEvent", NativeEvent(
                                type = "button_click",
                                timestamp = System.currentTimeMillis(),
                                data = "Hello from Native!"
                            ))
                        },
                        enabled = state.loadingState is com.parkwoocheol.composewebview.LoadingState.Finished
                    ) {
                        Text("Emit Event")
                    }
                }
            )
        }
    ) { padding ->
        ComposeWebView(
            state = state,
            controller = controller,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            jsBridge = bridge,
            onCreated = {
                it.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransientBrowserScreen(onBack: () -> Unit) {
    val url = "https://google.com"
    val state = rememberWebViewState(url = url)
    val controller = rememberWebViewController()
    var textFieldValue by remember(state.lastLoadedUrl) {
        mutableStateOf(TextFieldValue(state.lastLoadedUrl ?: url))
    }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        TextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Enter URL") },
                            trailingIcon = {
                                IconButton(onClick = { controller.loadUrl(textFieldValue.text) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Go")
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home")
                        }
                    },
                    actions = {
                        IconButton(onClick = { controller.reload() }) {
                            Icon(Icons.Filled.Refresh, "Reload")
                        }
                    }
                )
                val loadingState = state.loadingState
                if (loadingState is com.parkwoocheol.composewebview.LoadingState.Loading) {
                    val progress = loadingState.progress
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = { controller.navigateBack() },
                    enabled = controller.canGoBack
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                IconButton(
                    onClick = { controller.navigateForward() },
                    enabled = controller.canGoForward
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Forward")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp)
            ) {
                Text(
                    "Transient State: Rotate screen to see reload",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            ComposeWebView(
                state = state,
                controller = controller,
                modifier = Modifier.fillMaxSize(),
                onCreated = { webView ->
                    webView.settings.javaScriptEnabled = true
                },
                loadingContent = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                errorContent = { errors ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error Occurred", color = MaterialTheme.colorScheme.onErrorContainer)
                            Button(onClick = { controller.reload() }) { Text("Retry") }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(onBack: () -> Unit) {
    // YouTube video for testing fullscreen
    val url = "https://m.youtube.com/watch?v=dQw4w9WgXcQ"
    val state = rememberSaveableWebViewState(url = url)
    val controller = rememberWebViewController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fullscreen Video") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home")
                    }
                }
            )
        }
    ) { padding ->
        ComposeWebView(
            state = state,
            controller = controller,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            onCreated = {
                it.settings.javaScriptEnabled = true
                it.settings.domStorageEnabled = true // Required for some video players
            },
            customViewContent = { customView ->
                SampleCustomViewContent(customView)
            }
        )
    }
}

@Composable
fun SampleCustomViewContent(customViewState: CustomViewState) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { customViewState.callback.onCustomViewHidden() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black)
        ) {
            val layoutParams = android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            AndroidView(
                factory = { _ ->
                    customViewState.view.apply {
                        this.layoutParams = layoutParams
                    }
                },
                modifier = Modifier,
                update = { _ -> },
                onRelease = { }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomClientScreen(onBack: () -> Unit) {
    val url = "https://google.com"
    val state = rememberSaveableWebViewState(url = url)
    val controller = rememberWebViewController()

    // Custom Client that blocks "example.com"
    val client = remember {
        object : com.parkwoocheol.composewebview.client.ComposeWebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): Boolean {
                if (request?.url?.host?.contains("example.com") == true) {
                    return true // Block example.com
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Client") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home")
                    }
                },
                actions = {
                    Button(onClick = { controller.loadUrl("https://example.com") }) {
                        Text("Try Blocked")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "This client blocks 'example.com'. Try clicking 'Try Blocked'.",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            ComposeWebView(
                state = state,
                controller = controller,
                client = client,
                modifier = Modifier.fillMaxSize(),
                onCreated = { it.settings.javaScriptEnabled = true }
            )
        }
    }
}