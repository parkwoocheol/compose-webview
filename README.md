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
- **Flexible API**: Full control over `WebViewClient`, `WebChromeClient`, and WebView settings.
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

    ComposeWebView(
        state = state,
        controller = controller,
        modifier = Modifier.fillMaxSize(),
        onReceivedError = { webView, request, error ->
            // Handle error
            Log.e("WebView", "Error loading ${request?.url}: ${error?.description}")
        },
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

Full control over WebView settings and clients:

```kotlin
@Composable
fun CustomWebView() {
    val state = rememberSaveableWebViewState(url = "https://example.com")

    ComposeWebView(
        state = state,
        modifier = Modifier.fillMaxSize(),
        onCreated = { webView ->
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                javaScriptCanOpenWindowsAutomatically = true
                mediaPlaybackRequiresUserGesture = false
                allowFileAccess = true
                allowContentAccess = true
            }
        },
        client = { /* Custom WebViewClient */ },
        chromeClient = { /* Custom WebChromeClient */ },
        onDispose = { webView ->
            // Custom cleanup logic
            webView.clearCache(true)
            webView.clearHistory()
        }
    )
}
```

## API Reference

### ComposeWebView

Main composable function for displaying WebView.

```kotlin
@Composable
fun ComposeWebView(
    url: String,
    modifier: Modifier = Modifier,
    controller: WebViewController = rememberWebViewController(),
    javaScriptInterfaces: Map<String, Any> = emptyMap(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: ComposeWebViewClient = remember { ComposeWebViewClient() },
    chromeClient: ComposeWebChromeClient = remember { ComposeWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable (List<WebViewError>) -> Unit = {},
    jsAlertContent: @Composable (JsDialogState.Alert) -> Unit = {},
    jsConfirmContent: @Composable (JsDialogState.Confirm) -> Unit = {},
    jsPromptContent: @Composable (JsDialogState.Prompt) -> Unit = {},
    customViewContent: (@Composable (CustomViewState) -> Unit)? = null,
    onPageStarted: (WebView, String?, Bitmap?) -> Unit = { _, _, _ -> },
    onPageFinished: (WebView, String?) -> Unit = { _, _ -> },
    onReceivedError: (WebView, WebResourceRequest?, WebResourceError?) -> Unit = { _, _, _ -> },
    onProgressChanged: (WebView, Int) -> Unit = { _, _ -> },
    onDownloadStart: ((String, String, String, String, Long) -> Unit)? = null,
    onFindResultReceived: ((Int, Int, Boolean) -> Unit)? = null
)

// Alternative overload with state
@Composable
fun ComposeWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
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
    var pageIcon: Bitmap?
    val errorsForCurrentRequest: List<WebViewError>
    var jsDialogState: JsDialogState?
    var customViewState: CustomViewState?
    var webView: WebView?
    var bundle: Bundle?
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
    suspend inline fun <reified T, reified R> register(
        name: String,
        crossinline handler: suspend (T) -> R
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

## Sample App

Check out the sample app in the `app` module for complete working examples:

### Features Demonstrated

The sample app showcases the library's capabilities with a modern, beautiful UI:

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

5. **Custom Client** (`CustomClientScreen`)
    - Configure WebView settings (JS, DOM Storage, Zoom) dynamically.
    - Inject custom `WebViewClient` to intercept URLs (e.g., blocking specific domains).

### Running the Sample App

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` module.

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
