# Platform-Specific Implementation Guide

Detailed guide for implementing features across ComposeWebView's four supported platforms: Android, iOS, Desktop (JVM), and Web (JS).

## Overview

ComposeWebView uses Kotlin Multiplatform's **expect/actual pattern** to provide a unified API while leveraging platform-specific WebView implementations.

### Platform Summary

| Platform | Implementation | Min Version | Status |
|----------|----------------|-------------|--------|
| **Android** | `android.webkit.WebView` | API 24+ | ✅ Production |
| **iOS** | `WKWebView` | iOS 14.0+ | ✅ Production |
| **Desktop** | CEF via KCEF | JVM 11+ | 🚧 Experimental |
| **Web** | IFrame + postMessage | Modern browsers | 🚧 Experimental |

## Expect/Actual Pattern

### Step 1: Define in Common

File: `compose-webview/src/commonMain/kotlin/com/parkwoocheol/composewebview/PlatformFeature.kt`

```kotlin
/**
 * Platform-specific feature description.
 *
 * @param param Description of parameter
 * @return Description of return value
 */
expect class PlatformFeature(param: String) {
    fun doSomething(): String
    fun doSomethingElse(input: Int): Boolean
}
```

### Step 2: Implement per Platform

#### Android Implementation

File: `compose-webview/src/androidMain/kotlin/com/parkwoocheol/composewebview/PlatformFeature.android.kt`

```kotlin
import android.webkit.WebView

actual class PlatformFeature actual constructor(private val param: String) {
    actual fun doSomething(): String {
        // Android-specific implementation using WebView
        return "Android: $param"
    }

    actual fun doSomethingElse(input: Int): Boolean {
        // Android implementation
        return input > 0
    }
}
```

#### iOS Implementation

File: `compose-webview/src/iosMain/kotlin/com/parkwoocheol/composewebview/PlatformFeature.ios.kt`

```kotlin
import platform.WebKit.WKWebView

actual class PlatformFeature actual constructor(private val param: String) {
    actual fun doSomething(): String {
        // iOS-specific implementation using WKWebView
        return "iOS: $param"
    }

    actual fun doSomethingElse(input: Int): Boolean {
        // iOS implementation
        return input > 0
    }
}
```

#### Desktop Implementation

File: `compose-webview/src/desktopMain/kotlin/com/parkwoocheol/composewebview/PlatformFeature.desktop.kt`

```kotlin
actual class PlatformFeature actual constructor(private val param: String) {
    actual fun doSomething(): String {
        // Desktop-specific implementation using CEF
        return "Desktop: $param"
    }

    actual fun doSomethingElse(input: Int): Boolean {
        // Desktop implementation
        return input > 0
    }
}
```

#### Web Implementation

File: `compose-webview/src/jsMain/kotlin/com/parkwoocheol/composewebview/PlatformFeature.js.kt`

```kotlin
actual class PlatformFeature actual constructor(private val param: String) {
    actual fun doSomething(): String {
        // Web-specific implementation
        return "Web: $param"
    }

    actual fun doSomethingElse(input: Int): Boolean {
        // Web implementation
        return input > 0
    }
}
```

## Platform Constraints

### Android

**Capabilities**:
- Full WebView API access
- `addJavascriptInterface` for JS bridge
- Custom `WebViewClient` and `WebChromeClient`
- File access, cookies, local storage

**Limitations**:
- Minimum SDK 24 (Android 7.0)
- Requires runtime permissions for some features
- Must run on main thread for WebView operations

**Best Practices**:
```kotlin
// Use AndroidView for composition
AndroidView(
    factory = { context ->
        WebView(context).apply {
            // Configuration
        }
    },
    update = { webView ->
        // Updates
    }
)
```

### iOS

**Capabilities**:
- Modern `WKWebView` API
- `WKScriptMessageHandler` for JS bridge
- Content blocking, custom schemes
- Cookies, local storage

**Limitations**:
- **Limited zoom control** - WKWebView has restricted zoom APIs
- Strict security policies
- Must use message handlers instead of `addJavascriptInterface`
- Some WebView features require specific entitlements

**Best Practices**:
```kotlin
// Use UIKitView for composition
UIKitView(
    factory = {
        WKWebView().apply {
            // Configuration
        }
    },
    update = { webView ->
        // Updates
    }
)
```

**JSBridge Pattern**:
```kotlin
// Register message handler
webView.configuration.userContentController
    .addScriptMessageHandler(handler, name = "appBridge")
```

### Desktop (CEF via KCEF)

**Capabilities**:
- Full Chromium features
- Custom schemes, request interception
- DevTools support

**Limitations**:
- **Asynchronous initialization** - CEF requires async startup
- Larger binary size (includes Chromium)
- Platform-specific threading model
- Experimental status

**Best Practices**:
```kotlin
// Must initialize CEF before use
KCEF.init(builder = {
    // CEF configuration
}, onInitialized = {
    // CEF ready
})

// Use SwingPanel for integration
SwingPanel(
    factory = {
        // Create CEF browser
    }
)
```

**Threading**:
- UI operations must be on the correct thread
- Use `Platform.runLater` for UI updates from CEF callbacks

### Web (JS)

**Capabilities**:
- IFrame-based embedding
- postMessage for communication
- Standard web APIs

**Limitations**:
- Sandboxed environment
- No native WebView features
- Limited to browser capabilities
- Experimental status

**Best Practices**:
```kotlin
// Use IFrame with Canvas or DOM
Canvas(modifier) {
    // Render IFrame
}
```

**Communication**:
```kotlin
// Use postMessage bridge
window.parent.postMessage(message, "*")
```

## Common Patterns

### State Management

All platforms should integrate with `WebViewState`:

```kotlin
val state = rememberWebViewState(initialUrl = "https://example.com")

// Update state on events
state.loadingState = LoadingState.Loading
state.lastLoadedUrl = url
state.pageTitle = title
```

### Controller Pattern

Implement `WebViewController` methods:

```kotlin
class PlatformWebViewController(
    private val webView: PlatformWebView,
    override val state: WebViewState
) : WebViewController {
    override fun loadUrl(url: String) {
        // Platform-specific URL loading
    }

    override fun goBack() {
        // Platform-specific back navigation
    }

    // ... other methods
}
```

### JSBridge Integration

Each platform implements JavaScript bridge differently:

- **Android**: `addJavascriptInterface`
- **iOS**: `WKScriptMessageHandler`
- **Desktop**: CEF message router
- **Web**: `postMessage`

Unified API in `WebViewJsBridge.kt` abstracts these differences.

## Testing Strategy

### Common Tests

File: `compose-webview/src/commonTest/`

```kotlin
class CommonWebViewTest {
    @Test
    fun testCommonLogic() {
        // Test shared business logic
    }
}
```

### Platform Tests

Each platform has its own test directory:
- `androidInstrumentedTest/` - Android UI tests
- `iosTest/` - iOS tests
- `desktopTest/` - Desktop tests
- `jsTest/` - Web/JS tests

## Migration Checklist

When adding a new feature:

- [ ] Define `expect` in `commonMain`
- [ ] Implement `actual` in `androidMain`
- [ ] Implement `actual` in `iosMain`
- [ ] Implement `actual` in `desktopMain`
- [ ] Implement `actual` in `jsMain`
- [ ] Add KDoc to public APIs
- [ ] Write common tests
- [ ] Write platform-specific tests
- [ ] Update API documentation
- [ ] Run `./gradlew spotlessApply`
- [ ] Verify with `bash .agent/skills/development/scripts/platform_status.sh`

## Troubleshooting

### Compilation Errors

**Missing actual implementation**:
```
Expected declaration 'class Foo' has no actual declaration
```
→ Implement `actual` in all platform source sets

**Signature mismatch**:
```
Actual declaration has different parameter types
```
→ Ensure `actual` signature exactly matches `expect`

### Runtime Issues

**Android WebView not loading**:
- Check internet permission in AndroidManifest
- Verify WebView is on main thread
- Enable JavaScript if needed

**iOS WKWebView blank**:
- Check App Transport Security settings
- Verify message handler names match
- Check console for JavaScript errors

**Desktop CEF not initializing**:
- Ensure CEF initialization completes before use
- Check logs for initialization errors
- Verify CEF resources are bundled

## References

- **Architecture**: `.agent/knowledge/architecture.md`
- **Gradle Tasks**: `gradle_tasks.md`
- **Code Style**: `.agent/knowledge/code_style.md`
- **KMP Documentation**: [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)

---

Last updated: 2025-12-28
