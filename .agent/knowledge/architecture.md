# Architecture & Design Patterns

## Multiplatform Structure

### Common First Principle

- **Common Code First**: Define all features in `commonMain` using `expect`
- **Platform-Specific Implementation**: Implement with `actual` in each platform

### Platform-Specific Implementations

#### androidMain

- Maps to `android.webkit.WebView`
- Uses `AndroidView` Composable
- Extends `WebViewClient`, `WebChromeClient`

#### iosMain

- Maps to `WKWebView`
- Uses `UIKitView` Composable
- Utilizes `WKNavigationDelegate`, `WKUIDelegate`, `WKScriptMessageHandler`

#### desktopMain

- Uses `SwingPanel` and `KCEF`
- Handles asynchronous CEF initialization
- Requires JogAmp dependencies

#### jsMain

- Iframe-based implementation
- Bridge communication via `postMessage` API

#### wasmJsMain

- Iframe-based implementation (dynamic positioning)
- Considers Same-origin policy constraints

### PlatformContext

Abstracts platform-specific Context (Android) or Application for usage.

## Clean Architecture (2026 Recommended)

### Layer Separation

- **Presentation Layer**: Composable UI components
- **Domain Layer**: Business logic (`WebViewState`, `WebViewController`)
- **Data Layer**: Platform-specific WebView implementations (`expect`/`actual`)

### Core Principles

- Keep common code framework-agnostic
- Isolate platform-specific implementations
- Dependency direction: Presentation → Domain → Data

## State Management

### WebViewState

Class-based state holder managing:

- `loadingState`: `LoadingState` enum (Idle, Loading, Finished)
- `lastLoadedUrl`: Last loaded URL
- `errorsForCurrentRequest`: Error list for current request
- `pageTitle`: Page title
- `pageIcon`: Page icon (favicon)
- `scrollPosition`: Scroll position (x, y)

**State Updates**: Utilizes `StateFlow`, `MutableState`

### WebViewController

Controller separation pattern for command handling:

- `loadUrl()`, `loadHtml()`, `postUrl()`
- `navigateBack()`, `navigateForward()`, `reload()`
- `evaluateJavascript()`, `scrollTo()`, `zoomIn/Out()`

## JSBridge Architecture

### Platform-Specific Mechanisms

- **Android**: `addJavascriptInterface`
- **iOS**: `WKScriptMessageHandler`
- **Desktop**: CEF MessageRouter
- **Web (JS/WASM)**: `postMessage` API

### Key Features

- **Type-Safe Registration**: `bridge.register<Input, Output>`
- **Promise-based JS API**: `window.AppBridge.call`
- **Event Emission**: `bridge.emit`
- **Kotlinx Serialization**: Automatic JSON conversion

### Implementation Location

Core logic resides in `WebViewJsBridge.kt`.

## 2026 Best Practices

### Default Use of Kotlinx Serialization

- Use `Kotlinx Serialization` for JSON serialization/deserialization
- Ensures type-safe data transfer

### Consider Platform Constraints

#### iOS (WKWebView)

- **Limited Zoom Control**: Only pinch-to-zoom supported
- **Progress Polling**: Observes `estimatedProgress` at 100ms intervals
- **Security Policy**: Strict CORS and Same-origin policy

#### Desktop (CEF)

- **Asynchronous Initialization**: CEF initializes asynchronously
- **Threading**: Consider separation of UI thread and CEF thread

#### Web (WASM)

- **Same-origin Policy**: Restricted access to content within Iframe
- **Limited Native Features**: File upload, permission requests, etc. unsupported

## AI Agent Guidelines

- **Korean Documentation**: Write Implementation Plan and all deliverables in Korean
- **Consider Platform Constraints**: Always consider constraints of iOS, Desktop, WASM
- **Type Safety First**: Recommend using Kotlinx Serialization
