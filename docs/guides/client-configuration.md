# Configuring WebView Clients

This guide covers how to configure WebView clients to handle navigation events, progress updates, console messages, and more using the convenient `rememberWebViewClient` and `rememberWebChromeClient` composables.

---

## Overview

ComposeWebView provides two main client types:

- **`ComposeWebViewClient`**: Handles navigation events (page started, finished, errors, URL loading)
- **`ComposeWebChromeClient`**: Handles UI events (progress, console messages, JavaScript dialogs)

Starting from version 1.4.0, you can configure these clients using convenient composable functions.

---

## Basic Usage

### WebViewClient Configuration

Configure navigation and page lifecycle events:

```kotlin
@Composable
fun MyWebView() {
    val state = rememberSaveableWebViewState(url = "https://example.com")

    val client = rememberWebViewClient {
        onPageStarted { view, url, favicon ->
            println("Started loading: $url")
        }

        onPageFinished { view, url ->
            println("Finished loading: $url")
        }

        onReceivedError { view, request, error ->
            println("Error: ${error?.description}")
        }

        shouldOverrideUrlLoading { view, request ->
            // Return true to prevent loading
            val url = request?.url ?: return@shouldOverrideUrlLoading false

            // Handle custom URL schemes
            if (url.startsWith("myapp://")) {
                // Handle custom scheme
                return@shouldOverrideUrlLoading true
            }

            false // Allow normal navigation
        }
    }

    ComposeWebView(
        state = state,
        client = client,
        modifier = Modifier.fillMaxSize()
    )
}
```

### WebChromeClient Configuration

Configure progress tracking and console messages:

```kotlin
@Composable
fun MyWebView() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    var loadingProgress by remember { mutableStateOf(0) }

    val chromeClient = rememberWebChromeClient {
        onProgressChanged { view, progress ->
            loadingProgress = progress
            println("Loading: $progress%")
        }

        onConsoleMessage { view, message ->
            when (message.level) {
                ConsoleMessageLevel.ERROR -> {
                    Log.e("WebView", "[JS Error] ${message.message}")
                }
                ConsoleMessageLevel.WARNING -> {
                    Log.w("WebView", "[JS Warning] ${message.message}")
                }
                else -> {
                    Log.d("WebView", "[JS] ${message.message}")
                }
            }
            false // Return true to suppress default console logging
        }

        onPermissionRequest { request ->
            // Handle permission requests (platform-specific)
            // e.g., camera, microphone, location
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (loadingProgress < 100) {
            LinearProgressIndicator(
                progress = { loadingProgress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }

        ComposeWebView(
            state = state,
            chromeClient = chromeClient,
            modifier = Modifier.weight(1f)
        )
    }
}
```

---

## Method Chaining

You can also configure clients using method chaining:

```kotlin
@Composable
fun ChainedExample() {
    val client = rememberWebViewClient()
        .onPageStarted { view, url, favicon ->
            println("Started: $url")
        }
        .onPageFinished { view, url ->
            println("Finished: $url")
        }
        .onReceivedError { view, request, error ->
            println("Error: $error")
        }

    val chromeClient = rememberWebChromeClient()
        .onProgressChanged { view, progress ->
            println("Progress: $progress%")
        }
        .onConsoleMessage { view, message ->
            println("[Console] ${message.message}")
            false
        }

    ComposeWebView(
        state = rememberSaveableWebViewState(url = "https://example.com"),
        client = client,
        chromeClient = chromeClient,
        modifier = Modifier.fillMaxSize()
    )
}
```

---

## Advanced Customization

For advanced scenarios requiring full control, you can still extend the client classes directly:

```kotlin
@Composable
fun AdvancedClient() {
    val client = remember {
        object : ComposeWebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: PlatformBitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                // Custom logic before default handling
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Inject custom JavaScript
                view?.evaluateJavascript("""
                    console.log('Page loaded!');
                    // Your custom JS code
                """) { result ->
                    println("JS result: $result")
                }
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

---

## Available Handlers

### ComposeWebViewClient

| Handler | Parameters | Return Type | Description |
|---------|-----------|-------------|-------------|
| `onPageStarted` | `(WebView?, String?, PlatformBitmap?)` | `Unit` | Called when page starts loading |
| `onPageFinished` | `(WebView?, String?)` | `Unit` | Called when page finishes loading |
| `onReceivedError` | `(WebView?, PlatformWebResourceRequest?, PlatformWebResourceError?)` | `Unit` | Called when an error occurs |
| `shouldOverrideUrlLoading` | `(WebView?, PlatformWebResourceRequest?)` | `Boolean` | Return `true` to prevent navigation |

### ComposeWebChromeClient

| Handler | Parameters | Return Type | Description |
|---------|-----------|-------------|-------------|
| `onProgressChanged` | `(WebView?, Int)` | `Unit` | Called when loading progress changes (0-100) |
| `onConsoleMessage` | `(WebView?, ConsoleMessage)` | `Boolean` | Called when JS console logs. Return `true` to suppress default |
| `onPermissionRequest` | `(PlatformPermissionRequest)` | `Unit` | Called when permission is requested (platform-specific) |

---

## Platform Support

| Handler | Android | iOS | Desktop | Web |
|---------|:-------:|:---:|:-------:|:---:|
| `onPageStarted` | ✅ | ✅ | ✅ | ❌ |
| `onPageFinished` | ✅ | ✅ | ✅ | ✅ |
| `onReceivedError` | ✅ | ✅ | ⚠️ | ❌ |
| `shouldOverrideUrlLoading` | ✅ | ✅ | ✅ | ❌ |
| `onProgressChanged` | ✅ | ✅ | ❌ | ❌ |
| `onConsoleMessage` | ✅ | ✅ | ❌ | ❌ |
| `onPermissionRequest` | ✅ | ⚠️ | ❌ | ❌ |

**Legend**: ✅ Full Support | ⚠️ Partial/Limited | ❌ Not Supported

---

## Common Use Cases

### Loading Indicator

```kotlin
@Composable
fun WebViewWithProgress() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }

    val client = rememberWebViewClient {
        onPageStarted { _, _, _ -> isLoading = true }
        onPageFinished { _, _ -> isLoading = false }
    }

    val chromeClient = rememberWebChromeClient {
        onProgressChanged { _, p -> progress = p }
    }

    Column(Modifier.fillMaxSize()) {
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }

        ComposeWebView(
            state = state,
            client = client,
            chromeClient = chromeClient,
            modifier = Modifier.weight(1f)
        )
    }
}
```

### Custom URL Handling

```kotlin
@Composable
fun CustomUrlHandling() {
    val context = LocalContext.current

    val client = rememberWebViewClient {
        shouldOverrideUrlLoading { view, request ->
            val url = request?.url ?: return@shouldOverrideUrlLoading false

            when {
                url.startsWith("tel:") -> {
                    // Handle phone number
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    true
                }
                url.startsWith("mailto:") -> {
                    // Handle email
                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                    true
                }
                url.startsWith("myapp://") -> {
                    // Handle custom scheme
                    handleCustomScheme(url)
                    true
                }
                else -> false
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

### Error Handling

```kotlin
@Composable
fun ErrorHandlingWebView() {
    val state = rememberSaveableWebViewState(url = "https://example.com")
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val client = rememberWebViewClient {
        onReceivedError { view, request, error ->
            errorMessage = error?.description ?: "Unknown error"
        }

        onPageFinished { _, _ ->
            errorMessage = null // Clear error on successful load
        }
    }

    Box(Modifier.fillMaxSize()) {
        ComposeWebView(
            state = state,
            client = client,
            modifier = Modifier.fillMaxSize()
        )

        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Error: $error",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
```

---

## Migration from Old API

If you're using the old callback-based API, here's how to migrate:

### Before (Callback Parameters)

```kotlin
ComposeWebView(
    state = state,
    onPageStarted = { view, url, favicon ->
        println("Started: $url")
    },
    onPageFinished = { view, url ->
        println("Finished: $url")
    },
    onProgressChanged = { view, progress ->
        println("Progress: $progress")
    }
)
```

### After (Client Configuration)

```kotlin
val client = rememberWebViewClient {
    onPageStarted { view, url, favicon ->
        println("Started: $url")
    }
    onPageFinished { view, url ->
        println("Finished: $url")
    }
}

val chromeClient = rememberWebChromeClient {
    onProgressChanged { view, progress ->
        println("Progress: $progress")
    }
}

ComposeWebView(
    state = state,
    client = client,
    chromeClient = chromeClient
)
```

**Note**: Both APIs are supported. The callback parameters are still available for backward compatibility.

---

## See Also

- [State Management](state-management.md)
- [Error Handling](errors.md)
- [Lifecycle Management](lifecycle.md)
- [Features Guide](features.md)
