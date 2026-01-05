# ComposeWebView

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Release](https://jitpack.io/v/parkwoocheol/compose-webview.svg)](https://jitpack.io/#parkwoocheol/compose-webview)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.9.3-blue.svg)](https://github.com/JetBrains/compose-multiplatform)
[![Documentation](https://img.shields.io/badge/docs-website-blueviolet)](https://parkwoocheol.github.io/compose-webview/)

A powerful, flexible, and feature-rich WebView wrapper for **Jetpack Compose** and **Compose Multiplatform** (Android, iOS, Desktop, Web).

 Supports **Android**, **iOS**, **Desktop (JVM)**, and **Web (JS)** with a unified API.

## Table of Contents

- [Documentation](#documentation)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage](#usage)
  - [Basic Usage](#basic-usage)
  - [WebView State and Controller](#webview-state-and-controller)
  - [JSBridge - Kotlin to JavaScript Communication](#jsbridge---kotlin-to-javascript-communication)
  - [Lifecycle Management](#lifecycle-management)
  - [Error Handling](#error-handling)
  - [Custom WebView Configuration](#custom-webview-configuration)
  - [Configuring WebView Clients](#configuring-webview-clients)
- [API Reference](#api-reference)
- [Sample App](#sample-app)
- [Contributing](#contributing)
- [License](#license)

## Documentation

📘 **Full Documentation**: [https://parkwoocheol.github.io/compose-webview/](https://parkwoocheol.github.io/compose-webview/)

Visit the documentation site for comprehensive guides, API references, and advanced usage examples.

## Features

- **Multiplatform Support**: Supports Android, iOS, Desktop (CEF), and Web (JS).
- **Jetpack Compose Integration**: Seamlessly integrates native WebViews with Compose UI.
- **Advanced JSBridge (Mobile Focused)**: A highly productive bridge for Android & iOS.
  - **Promise-based**: JavaScript calls return Promises, allowing `await` syntax (no callback hell).
  - **Type-Safe**: Built-in **Kotlinx Serialization** support automatically converts JSON to Kotlin data classes.
  - **Event Bus**: Bi-directional event system (`emit`/`on`) for real-time communication.
- **State Management**: Reactive state handling for URL, loading progress, and navigation.
- **Flexible API**: Full control over `WebViewClient`, `WebChromeClient`, and WebView settings with convenient composable functions (`rememberWebViewClient`, `rememberWebChromeClient`).
- **Lifecycle Management**: Automatically handles `onResume`, `onPause`, and cleanup.
- **Custom View Support**: Built-in support for fullscreen videos and custom HTML views.
- **Loading & Error States**: Built-in state management for loading indicators and error handling.
- **Back Navigation**: Integrated with Compose `BackHandler` for seamless back navigation (Android) and native swipe gestures (iOS).
- **File Upload**: Full support for file upload functionality (Android & iOS).

## Requirements

- Android API 24+
- iOS 14.0+
- Desktop (JVM) 11+
- Web (JS)
- Jetpack Compose / Compose Multiplatform 1.9.3+
- Kotlin 2.2.0+

## Supported Platforms

 | Platform | Implementation | Status | Note |
 |----------|----------------|--------|------|
 | **Android** | `AndroidView` (WebView) | ✅ Stable | Full feature support |
 | **iOS** | `UIKitView` (WKWebView) | ✅ Stable | Full feature support (Seamless JS Bridge) |
 | **Desktop** | `SwingPanel` (CEF via KCEF) | 🚧 Experimental | **WIP**: KCEF integration is in progress. Basic browsing works, but advanced features are still being tested. |
 | **Web (JS)** | `Iframe` (DOM) | 🚧 Experimental | **WIP**: Basic navigation and JSBridge (via postMessage) are implemented but may have limitations. |

### API Support Matrix

<details>
<summary>Click to expand detailed API support by platform</summary>

#### Core Navigation APIs

| API | Android | iOS | Desktop | Web | Notes |
|-----|:-------:|:---:|:-------:|:---:|-------|
| `loadUrl()` | ✅ | ✅ | ✅ | ✅ | Headers: Android/iOS only |
| `loadHtml()` | ✅ | ✅ | ✅ | ⚠️ | Web: CORS restrictions |
| `postUrl()` | ✅ | ✅ | ❌ | ❌ | Desktop/Web not supported |
| `evaluateJavascript()` | ✅ | ✅ | ✅ | ⚠️ | Web: CORS restricted |
| `navigateBack/Forward` | ✅ | ✅ | ✅ | ⚠️ | Web: CORS restricted |
| `reload() / stopLoading()` | ✅ | ✅ | ✅ | ⚠️ | Web: CORS restricted |

#### Control & Navigation

| API | Android | iOS | Desktop | Web | Notes |
|-----|:-------:|:---:|:-------:|:---:|-------|
| `zoomIn/Out/By()` | ✅ | ❌ | ✅ | ❌ | iOS: Pinch-to-zoom only |
| `scrollTo/By()` | ✅ | ✅ | ❌ | ✅ | Desktop not supported |
| `pageUp/Down()` | ✅ | ⚠️ | ❌ | ⚠️ | Via scrollBy on iOS/Web |
| `findAllAsync()` | ✅ | ❌ | ❌ | ❌ | Android only |
| `clearCache/History()` | ✅ | ❌ | ❌ | ❌ | Android only |

#### State Management

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| `LoadingState` | ✅ | ✅ | ⚠️ | ⚠️ | All states supported on Android/iOS |
| `LoadingState.Loading(progress)` | ✅ | ✅ | ❌ | ❌ | iOS: 100ms polling |
| `ScrollPosition` | ✅ | ✅ | ❌ | ⚠️ | Real-time (Android), Polling (iOS), CORS (Web) |
| `WebViewError` | ✅ | ✅ | ⚠️ | ⚠️ | Typed error categories |
| `jsDialogState` | ✅ | ✅ | ❌ | ❌ | Alert/Confirm/Prompt |
| `customViewState` | ✅ | ⚠️ | ❌ | ❌ | Android: custom view view hierarchy, iOS: native fullscreen signal |

#### Configuration (WebViewSettings)

| Setting | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| `userAgent` | ✅ | ✅ | ✅ | ❌ | Browser controlled on Web |
| `javaScriptEnabled` | ✅ | ✅* | ✅ | ❌ | *iOS: Always enabled |
| `domStorageEnabled` | ✅ | ✅ | ⚠️ | ❌ | Limited on Desktop |
| `cacheMode` | ✅ | ⚠️ | ⚠️ | ❌ | Full support on Android |
| `supportZoom` | ✅ | ⚠️** | ✅ | ❌ | **iOS: Pinch-to-zoom only |
| `mediaPlaybackRequiresUserAction` | ✅ | ✅ | ⚠️ | ❌ | Autoplay control |

#### Callbacks & Events

| Callback | Android | iOS | Desktop | Web | Notes |
|----------|:-------:|:---:|:-------:|:---:|-------|
| `onPageStarted` | ✅ | ✅ | ✅ | ❌ | Navigation started |
| `onPageFinished` | ✅ | ✅ | ✅ | ✅ | Navigation completed |
| `onProgressChanged` | ✅ | ✅ | ❌ | ❌ | iOS: 100ms polling |
| `onReceivedError` | ✅ | ✅ | ⚠️ | ❌ | Typed error information |
| `onConsoleMessage` | ✅ | ✅ | ❌ | ❌ | JavaScript console debugging |
| `shouldOverrideUrlLoading` | ✅ | ✅ | ✅ | ❌ | Custom URL handling |
| JS Dialogs (Alert/Confirm/Prompt) | ✅ | ✅ | ❌ | ❌ | Custom dialog UI |
| Custom View (Fullscreen) | ✅ | ⚠️ | ❌ | ❌ | Android: custom view injection, iOS: native fullscreen events |
| File Upload | ✅ | ✅ | ❌ | ❌ | Native file picker |
| Download Handling | ✅ | ⚠️ | ❌ | ❌ | `onDownloadStart` callback |

#### JSBridge

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| `register()` handler | ✅ | ✅ | ✅ | ✅ | Kotlin ↔ JavaScript calls |
| `emit()` events | ✅ | ✅ | ⚠️ | ⚠️ | Event bus system |
| Promise-based response | ✅ | ✅ | ⚠️ | ⚠️ | Async/await support |
| Type-safe serialization | ✅ | ✅ | ✅ | ✅ | Kotlinx Serialization |

**Legend**: ✅ Full Support | ⚠️ Partial/Limited | ❌ Not Supported

</details>

## 🎯 Project Focus & Comparison

This library has a slightly different focus compared to other WebView libraries (like `KevinnZou/compose-webview-multiplatform`), primarily targeting **Mobile productivity**.

### 1. Mobile-First & Advanced JSBridge

We focused heavily on making the interaction between Kotlin and JavaScript as seamless as possible on **Android & iOS**.

- **Promise-based JSBridge**: Enables using `await` in JavaScript to call Native functions and get results directly, avoiding callback hell.
- **Flexible Serialization**: Supports **Kotlinx Serialization** out-of-the-box, while the `BridgeSerializer` interface allows plugging in Gson, Moshi, or any other JSON library.
- **Type Safety**: Allows passing complex data objects with full type safety.

### 2. Platform Support Status

- **Mobile (Android/iOS)**: Stable and Feature-Rich. Recommended for production.

- **Desktop & Web**: Currently **Experimental (WIP)**.
  - While we have implemented basic controls and JSBridge for these platforms, they are not as battle-tested as the mobile targets.
  - For **stable Desktop or Web requirements**, [KevinnZou's library](https://github.com/KevinnZou/compose-webview-multiplatform) is the **better choice**.

---

## 📦 Installation

Artifacts are published through **JitPack**.

### Setup

1. **Add the JitPack repository** to your build file.

   **Kotlin DSL (`settings.gradle.kts`):**

   ```kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven { url = uri("https://jitpack.io") }
       }
   }
   ```

2. **Add the dependency**.

   **Kotlin DSL (`build.gradle.kts`):**

   ```kotlin
   dependencies {
       implementation("com.github.parkwoocheol:compose-webview:<version>")
   }
   ```

   **Groovy DSL (`build.gradle`):**

   ```groovy
   dependencies {
       implementation 'com.github.parkwoocheol:compose-webview:<version>'
   }
   ```

The dependency declaration is identical for both Kotlin and Groovy DSL examples above.

## Quick Start

```kotlin
@Composable
fun MyWebViewScreen() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    val controller = rememberWebViewController()

    ComposeWebView(
        url = "https://example.com",
        modifier = Modifier.fillMaxSize(),
        onCreated = { webView ->
            webView.settings.javaScriptEnabled = true
        }
    )
}
```

## State Management

The library provides two ways to create and remember the `WebViewState`:

### 1. Persistent State (Recommended)

Uses `rememberSaveable` to preserve the WebView state (URL, scroll position, history, etc.) across configuration changes (e.g., screen rotation).

```kotlin
val state = rememberSaveableWebViewState(url = "https://google.com")
// or for HTML data
val state = rememberSaveableWebViewStateWithData(data = htmlContent)
```

### 2. Transient State (Lightweight)

Uses `remember` to hold the state. The WebView will be reloaded and state lost on configuration changes. Use this if you want to avoid `TransactionTooLargeException` with large HTML data or don't need state persistence.

```kotlin
val state = rememberWebViewState(url = "https://google.com")
// or for HTML data
val state = rememberWebViewStateWithData(data = htmlContent)
```

## Usage

### Basic Usage

Create a simple WebView with minimal configuration:

```kotlin
@Composable
fun SimpleWebView() {
    ComposeWebView(
        url = "https://google.com",
        modifier = Modifier.fillMaxSize(),
        onCreated = { webView ->
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
        }
    )
}
```

### WebView State and Controller

Monitor and control WebView state using `WebViewController`:

```kotlin
@Composable
fun WebViewWithState() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    val controller = rememberWebViewController()

    Column(modifier = Modifier.fillMaxSize()) {
        // Show loading indicator
        val loadingState = state.loadingState
        if (loadingState is LoadingState.Loading) {
            LinearProgressIndicator(
                progress = { loadingState.progress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Display current URL
        Text(text = "Current URL: ${state.lastLoadedUrl}")

        // Navigation controls
        Row {
            Button(
                onClick = { controller.navigateBack() },
                enabled = controller.canGoBack
            ) {
                Text("Back")
            }
            Button(
                onClick = { controller.navigateForward() },
                enabled = controller.canGoForward
            ) {
                Text("Forward")
            }
            Button(onClick = { controller.reload() }) {
                Text("Reload")
            }
        }

        ComposeWebView(
            state = state,
            controller = controller,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onCreated = { it.settings.javaScriptEnabled = true }
        )
    }
}
```

### JSBridge - Kotlin to JavaScript Communication

Communicate between Kotlin and JavaScript using type-safe, promise-based API.

#### Define Data Models

```kotlin
@Serializable
data class User(val name: String, val age: Int)

@Serializable
data class UserResponse(val success: Boolean, val message: String)

@Serializable
data class Location(val latitude: Double, val longitude: Double)
```

#### Register Handlers in Kotlin

```kotlin
@Composable
fun WebViewWithJsBridge() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    val bridge = rememberWebViewJsBridge()

    LaunchedEffect(bridge) {
        // Handle "updateUser" call from JavaScript
        bridge.register<User, UserResponse>("updateUser") { user ->
            println("Updating user: ${user.name}, age: ${user.age}")
            // Perform some operation
            UserResponse(success = true, message = "User updated successfully")
        }

        // Handle "getLocation" call
        bridge.register<Unit, Location>("getLocation") { _ ->
            // Return current location
            Location(latitude = 37.7749, longitude = -122.4194)
        }

        // Handle void calls (no return value)
        bridge.register<String, Unit>("log") { message ->
            println("JavaScript log: $message")
        }
    }

    ComposeWebView(
        state = state,
        jsBridge = bridge,
        modifier = Modifier.fillMaxSize()
    )
}
```

#### Call from JavaScript

```javascript
// The default bridge object is 'window.AppBridge'

// Call with typed response
async function updateUser() {
    try {
        const response = await window.AppBridge.call('updateUser', {
            name: "Park",
            age: 30
        });
        console.log("Success:", response.message);
    } catch (error) {
        console.error("Failed:", error);
    }
}

// Call without parameters
async function getLocation() {
    try {
        const location = await window.AppBridge.call('getLocation', null);
        console.log(`Lat: ${location.latitude}, Lng: ${location.longitude}`);
    } catch (error) {
        console.error("Failed to get location:", error);
    }
}

// Call without return value
async function logMessage() {
    try {
        await window.AppBridge.call('log', "Hello from JavaScript!");
    } catch (error) {
        console.error("Failed to log:", error);
    }
}
```

#### Customizing Bridge Name

Change the global JavaScript object name:

```kotlin
val bridge = rememberWebViewJsBridge(
    jsObjectName = "MyBridge" // Access via window.MyBridge in JavaScript
)
```

### Lifecycle Management

ComposeWebView automatically handles lifecycle events:

```kotlin
@Composable
fun WebViewWithLifecycle() {
    val state = rememberSaveableWebViewState(url = "https://example.com")

    // Lifecycle events are handled automatically:
    // - onResume() when composable enters composition
    // - onPause() when composable leaves composition
    // - Cleanup when disposed

    ComposeWebView(
        state = state,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Error Handling

Handle WebView errors and display custom error UI:

```kotlin
@Composable
fun WebViewWithErrorHandling() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    val controller = rememberWebViewController()

    // Configure client to handle errors
    val client = rememberWebViewClient {
        onReceivedError { view, request, error ->
            // Handle error
            Log.e("WebView", "Error loading ${request?.url}: ${error?.description}")
        }
    }

    ComposeWebView(
        state = state,
        controller = controller,
        client = client,
        modifier = Modifier.fillMaxSize(),
        errorContent = { errors ->
            // Custom error UI
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Failed to load page",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    errors.firstOrNull()?.let { error ->
                        Text(
                            text = error.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(onClick = { controller.reload() }) {
                        Text("Retry")
                    }
                }
            }
        }
    )
}
```

### Custom WebView Configuration

Configure WebView behavior using `WebViewSettings`:

```kotlin
@Composable
fun CustomWebView() {
    val state = rememberSaveableWebViewState(url = "https://example.com")

    val settings = WebViewSettings(
        userAgent = "MyApp/1.0",
        javaScriptEnabled = true,
        domStorageEnabled = true,
        cacheMode = CacheMode.CACHE_ELSE_NETWORK,
        supportZoom = true,
        loadWithOverviewMode = true,
        useWideViewPort = true,
        mediaPlaybackRequiresUserAction = false,
        allowFileAccess = false,
        allowContentAccess = false,
        allowFileAccessFromFileURLs = false,
        allowUniversalAccessFromFileURLs = false
    )

    ComposeWebView(
        state = state,
        settings = settings,
        modifier = Modifier.fillMaxSize(),
        onCreated = { webView ->
            // Additional platform-specific configuration
        },
        onDispose = { webView ->
            // Custom cleanup logic
        }
    )
}
```

### Scroll Position Tracking

Track and respond to scroll position changes:

```kotlin
@Composable
fun ScrollTrackingWebView() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    val controller = rememberWebViewController()
    var showScrollToTop by remember { mutableStateOf(false) }

    // Observe scroll position changes
    LaunchedEffect(state.scrollPosition) {
        val (x, y) = state.scrollPosition
        showScrollToTop = y > 500 // Show button when scrolled down 500px
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ComposeWebView(
            state = state,
            controller = controller,
            modifier = Modifier.fillMaxSize()
        )

        // Floating "Back to Top" button
        if (showScrollToTop) {
            FloatingActionButton(
                onClick = {
                    controller.scrollTo(0, 0)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, "Scroll to top")
            }
        }
    }
}
```

### Console Message Debugging

Capture JavaScript console messages for debugging:

```kotlin
@Composable
fun DebuggableWebView() {
    val state = rememberSaveableWebViewState(url = "https://example.com")

    // Configure chrome client to handle console messages
    val chromeClient = rememberWebChromeClient {
        onConsoleMessage { webView, message ->
            when (message.level) {
                ConsoleMessageLevel.ERROR -> {
                    Log.e("WebView", "[${message.sourceId}:${message.lineNumber}] ${message.message}")
                }
                ConsoleMessageLevel.WARNING -> {
                    Log.w("WebView", message.message)
                }
                ConsoleMessageLevel.LOG -> {
                    Log.d("WebView", message.message)
                }
                else -> {
                    Log.v("WebView", message.message)
                }
            }
            false // Return true to suppress default logging
        }
    }

    ComposeWebView(
        state = state,
        chromeClient = chromeClient,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Configuring WebView Clients

Configure WebView client events using `rememberWebViewClient` and `rememberWebChromeClient`:

#### Basic Usage

```kotlin
@Composable
fun WebViewWithClientConfiguration() {
    val state = rememberSaveableWebViewState(url = "https://example.com")

    // Configure WebViewClient
    val client = rememberWebViewClient {
        onPageStarted { view, url, favicon ->
            println("Page started loading: $url")
        }
        onPageFinished { view, url ->
            println("Page finished loading: $url")
        }
        onReceivedError { view, request, error ->
            println("Error loading page: ${error?.description}")
        }
        shouldOverrideUrlLoading { view, request ->
            // Return true to block navigation
            request?.url?.startsWith("myapp://") == true
        }
    }

    // Configure WebChromeClient
    val chromeClient = rememberWebChromeClient {
        onProgressChanged { view, progress ->
            println("Loading progress: $progress%")
        }
        onConsoleMessage { view, message ->
            println("[Console ${message.level}] ${message.message}")
            false // Return true to suppress default handling
        }
        onPermissionRequest { request ->
            // Handle permission requests (platform-specific)
        }
    }

    ComposeWebView(
        state = state,
        client = client,
        chromeClient = chromeClient,
        modifier = Modifier.fillMaxSize()
    )
}
```

#### Chaining Multiple Handlers

You can also chain handlers:

```kotlin
@Composable
fun ChainedHandlersExample() {
    val client = rememberWebViewClient()
        .onPageStarted { view, url, favicon -> /* ... */ }
        .onPageFinished { view, url -> /* ... */ }
        .onReceivedError { view, request, error -> /* ... */ }

    val chromeClient = rememberWebChromeClient()
        .onProgressChanged { view, progress -> /* ... */ }
        .onConsoleMessage { view, message -> false }

    ComposeWebView(
        state = rememberSaveableWebViewState(url = "https://example.com"),
        client = client,
        chromeClient = chromeClient,
        modifier = Modifier.fillMaxSize()
    )
}
```

#### Advanced Customization

For more advanced scenarios, you can still extend the client classes directly:

```kotlin
@Composable
fun AdvancedClientExample() {
    val client = remember {
        object : ComposeWebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Full control with direct override
                view?.evaluateJavascript("console.log('Custom injection')")
            }
        }
    }

    ComposeWebView(
        state = rememberSaveableWebViewState(url = "https://example.com"),
        client = client,
        modifier = Modifier.fillMaxSize()
    )
}
```

#### Available Client Handlers

**WebViewClient:**

- `onPageStarted(handler: (WebView?, String?, PlatformBitmap?) -> Unit)`
- `onPageFinished(handler: (WebView?, String?) -> Unit)`
- `onReceivedError(handler: (WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit)`
- `shouldOverrideUrlLoading(handler: (WebView?, PlatformWebResourceRequest?) -> Boolean)`

**WebChromeClient:**

- `onProgressChanged(handler: (WebView?, Int) -> Unit)`
- `onConsoleMessage(handler: (WebView?, ConsoleMessage) -> Boolean)`
- `onPermissionRequest(handler: (PlatformPermissionRequest) -> Unit)` (Platform-specific)

## API Reference

### ComposeWebView

Main composable function for displaying WebView.

```kotlin
@Composable
fun ComposeWebView(
    url: String,
    modifier: Modifier = Modifier,
    settings: WebViewSettings = WebViewSettings.Default,
    controller: WebViewController = rememberWebViewController(),
    javaScriptInterfaces: Map<String, Any> = emptyMap(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: ComposeWebViewClient = remember { ComposeWebViewClient() },
    chromeClient: ComposeWebChromeClient = remember { ComposeWebChromeClient() },
    factory: ((PlatformContext) -> WebView)? = null,
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (List<WebViewError>) -> Unit = {},
    jsAlertContent: @Composable (JsDialogState.Alert) -> Unit = {},
    jsConfirmContent: @Composable (JsDialogState.Confirm) -> Unit = {},
    jsPromptContent: @Composable (JsDialogState.Prompt) -> Unit = {},
    customViewContent: (@Composable (CustomViewState) -> Unit)? = null,
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null,
)

// Alternative overload with state
@Composable
fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    settings: WebViewSettings = WebViewSettings.Default,
    controller: WebViewController = rememberWebViewController(),
    javaScriptInterfaces: Map<String, Any> = emptyMap(),
    jsBridge: WebViewJsBridge? = null,
    // ... other parameters same as above
)
```

### WebViewState

Holds the state of the WebView.

```kotlin
class WebViewState(
    webContent: WebContent
) {
    var lastLoadedUrl: String?
    var content: WebContent
    val loadingState: LoadingState
    val isLoading: Boolean
    var pageTitle: String?
    var pageIcon: PlatformBitmap?
    var scrollPosition: ScrollPosition
    val errorsForCurrentRequest: SnapshotStateList<WebViewError>
    var jsDialogState: JsDialogState?
    var customViewState: CustomViewState?
    var webView: WebView?
    var bundle: PlatformBundle?
}
```

### WebViewController

Controls WebView navigation and operations.

```kotlin
class WebViewController {
    val canGoBack: Boolean
    val canGoForward: Boolean

    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String> = emptyMap())
    fun loadHtml(
        html: String,
        baseUrl: String? = null,
        mimeType: String? = null,
        encoding: String? = "UTF-8",
        historyUrl: String? = null
    )
    fun postUrl(url: String, postData: ByteArray)
    fun evaluateJavascript(script: String, callback: ((String) -> Unit)? = null)
    fun navigateBack()
    fun navigateForward()
    fun reload()
    fun stopLoading()
    fun zoomBy(zoomFactor: Float)
    fun zoomIn(): Boolean
    fun zoomOut(): Boolean
    fun findAllAsync(find: String)
    fun findNext(forward: Boolean)
    fun clearMatches()
    fun clearCache(includeDiskFiles: Boolean)
    fun clearHistory()
    fun clearSslPreferences()
    fun clearFormData()
    fun pageUp(top: Boolean): Boolean
    fun pageDown(bottom: Boolean): Boolean
    fun scrollTo(x: Int, y: Int)
    fun scrollBy(x: Int, y: Int)
    fun saveWebArchive(filename: String)
}
```

### WebViewJsBridge

Manages JavaScript-Kotlin communication.

```kotlin
class WebViewJsBridge(
    serializer: BridgeSerializer? = null,
    val jsObjectName: String = "AppBridge",
    private val nativeInterfaceName: String = "AppBridgeNative"
) {
    // Register a handler for calls from JavaScript
    inline fun <reified T, reified R> register(
        method: String,
        noinline handler: (T) -> R
    )

    // Register a handler with custom serializer
    fun register(
        name: String,
        handler: suspend (String) -> String
    )

    // Emit an event to JavaScript
    fun emit(eventName: String, data: Any)

    // Unregister a handler
    fun unregister(name: String)
}
```

### ComposeWebViewClient

Handles navigation and page lifecycle events. Can be configured using extension functions.

```kotlin
expect open class ComposeWebViewClient() {
    open var webViewState: WebViewState?
    open var webViewController: WebViewController?

    open fun onPageStarted(view: WebView?, url: String?, favicon: PlatformBitmap?)
    open fun onPageFinished(view: WebView?, url: String?)
    open fun onReceivedError(view: WebView?, request: PlatformWebResourceRequest?, error: PlatformWebResourceError?)
    open fun shouldOverrideUrlLoading(view: WebView?, request: PlatformWebResourceRequest?): Boolean
}
```

**Extension Functions for Configuration:**

```kotlin
// Configure callbacks using chainable extension functions
fun ComposeWebViewClient.onPageStarted(
    handler: (WebView?, String?, PlatformBitmap?) -> Unit
): ComposeWebViewClient

fun ComposeWebViewClient.onPageFinished(
    handler: (WebView?, String?) -> Unit
): ComposeWebViewClient

fun ComposeWebViewClient.onReceivedError(
    handler: (WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?) -> Unit
): ComposeWebViewClient

fun ComposeWebViewClient.shouldOverrideUrlLoading(
    handler: (WebView?, PlatformWebResourceRequest?) -> Boolean
): ComposeWebViewClient
```

### ComposeWebChromeClient

Handles UI events like progress updates and console messages. Can be configured using extension functions.

```kotlin
expect open class ComposeWebChromeClient() {
    open fun onProgressChanged(view: WebView?, newProgress: Int)
    open fun onConsoleMessage(view: WebView?, message: ConsoleMessage): Boolean
}
```

**Extension Functions for Configuration:**

```kotlin
// Configure callbacks using chainable extension functions
fun ComposeWebChromeClient.onProgressChanged(
    handler: (WebView?, Int) -> Unit
): ComposeWebChromeClient

fun ComposeWebChromeClient.onConsoleMessage(
    handler: (WebView?, ConsoleMessage) -> Boolean
): ComposeWebChromeClient

fun ComposeWebChromeClient.onPermissionRequest(
    handler: (PlatformPermissionRequest) -> Unit
): ComposeWebChromeClient
```

### Remember Functions

```kotlin
@Composable
fun rememberWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap()
): WebViewState

@Composable
fun rememberSaveableWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap()
): WebViewState

@Composable
fun rememberWebViewStateWithData(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String = "text/html",
    historyUrl: String? = null
): WebViewState

@Composable
fun rememberSaveableWebViewStateWithData(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String = "text/html",
    historyUrl: String? = null
): WebViewState

@Composable
fun rememberWebViewController(): WebViewController

@Composable
fun rememberWebViewClient(
    block: (ComposeWebViewClient.() -> Unit)? = null
): ComposeWebViewClient

@Composable
fun rememberWebChromeClient(
    block: (ComposeWebChromeClient.() -> Unit)? = null
): ComposeWebChromeClient

@Composable
fun rememberWebViewJsBridge(
    serializer: BridgeSerializer? = null,
    jsObjectName: String = "AppBridge",
    nativeInterfaceName: String = "AppBridgeNative"
): WebViewJsBridge
```

### Custom Serializer

Implement `BridgeSerializer` to use custom JSON libraries like Gson or Moshi:

```kotlin
interface BridgeSerializer {
    fun <T> encode(value: T, clazz: Class<T>): String
    fun <T> decode(json: String, clazz: Class<T>): T
}

// Example with Gson
class GsonBridgeSerializer(private val gson: Gson) : BridgeSerializer {
    override fun <T> encode(value: T, clazz: Class<T>): String {
        return gson.toJson(value, clazz)
    }

    override fun <T> decode(json: String, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }
}

// Usage
val bridge = rememberWebViewJsBridge(
    serializer = GsonBridgeSerializer(Gson())
)
```

## Sample Apps

The `sample/` directory contains runnable targets for every platform:

- `sample/shared`: the Compose Multiplatform UI/features showcased below.
- `sample/androidApp`: Android wrapper (Gradle module).
- `sample/iosApp`: Xcode project that embeds the shared UI.
- `sample/desktopApp`: Compose Desktop entry point.
- `sample/wasmApp`: Compose WASM/browser runner.

### Features Demonstrated

All sample targets use the same feature screens:

1. **Basic Browser** (`BasicBrowserScreen`)
    - Standard WebView with navigation controls (Back, Forward, Reload).
    - URL input field with loading progress indicator.

2. **Transient vs Saved State** (`TransientBrowserScreen`)
    - Demonstrates the difference between `rememberWebViewState` (transient) and `rememberSaveableWebViewState` (persisted).
    - Rotate the device to see the transient state reset while the saved state persists.

3. **HTML & JS Interaction** (`HtmlJsScreen`)
    - **Bi-directional Communication**: Send data from Kotlin to JS and vice-versa.
    - **Command Center**: Trigger Native events from UI and see JS logs in real-time.
    - **Promise-based API**: Call Native functions from JS using `await`.

4. **Fullscreen Video** (`FullscreenVideoScreen`)
    - Native fullscreen video support (e.g., YouTube).
    - Handles orientation changes and UI overlay automatically.
    - iOS uses the system fullscreen player; the Compose state lets you hide your chrome when it appears.

5. **Custom Client** (`CustomClientScreen`)
    - Configure WebView settings (JS, DOM Storage, Zoom) dynamically.
    - Inject custom `WebViewClient` to intercept URLs (e.g., blocking specific domains).

### Running the Samples

- **Android**: `./gradlew :sample:androidApp:installDebug`
- **Desktop**: `./gradlew :sample:desktopApp:run`
- **Web/Wasm**: `./gradlew :sample:wasmApp:wasmJsBrowserDevelopmentRun` (open the printed URL in a browser).
- **iOS**: Open `sample/iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme.

## Contributing

We welcome contributions from the community! Whether it's reporting bugs, suggesting features, improving docs, or submitting code.

### Contributing Quick Start

1. Fork the repository
2. Create your branch (`git checkout -b your-branch-name`)
3. Make your changes
4. Test with the sample app
5. Push and open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
