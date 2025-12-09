# Claude AI Guide for ComposeWebView

This document helps Claude AI (and other AI assistants) understand and work with this project effectively.

## Project Overview

**ComposeWebView** is a **Compose Multiplatform** library that provides a powerful WebView wrapper for:

- **Android** (System WebView)
- **iOS** (WKWebView)
- **Desktop** (CEF via KCEF)
- **Web** (IFrame)

**Key Features**:

- Type-safe JavaScript ↔ Kotlin bridge (JSBridge)
- Reactive state management
- Lifecycle handling
- Custom view support (fullscreen videos, etc.)
- **Cross-platform support**

## Project Structure

```text
compose-webview/
├── compose-webview/          # Main library module
│   ├── src/
│   │   ├── commonMain/kotlin/com/parkwoocheol/composewebview/
│   │   │   ├── ComposeWebView.kt        # Main composable
│   │   │   ├── WebViewPlatform.kt       # Expect/Actual definitions
│   │   │   ├── WebViewState.kt          # State holder
│   │   │   ├── WebViewController.kt     # Navigation controller
│   │   │   ├── WebViewJsBridge.kt       # JS↔Kotlin bridge
│   │   │   └── client/                  # WebViewClient/ChromeClient
│   │   ├── androidMain/      # Android implementation
│   │   ├── desktopMain/      # Desktop implementation (KCEF)
│   │   ├── iosMain/          # iOS implementation (WKWebView)
│   │   └── jsMain/           # Web implementation (IFrame)
├── app/                      # Sample app (Multiplatform)
│   ├── src/
│   │   ├── commonMain/       # Shared UI code
│   │   ├── androidMain/
│   │   ├── desktopMain/
│   │   └── iosMain/
├── README.md                 # User-facing documentation
├── CLAUDE.md                 # AI Assistant guide
└── build.gradle.kts          # Root build script
```

## Key Concepts

### 1. Multiplatform Architecture

- **Common API**: Defined in `commonMain` (e.g., `ComposeWebView`, `WebViewState`).
- **Platform Implementation**: Uses `expect`/`actual` pattern in `WebViewPlatform.kt`.
- **Desktop (CEF)**: Uses `dev.datlag:kcef` to embed Chromium. Requires `JogAmp` maven repository.
- **iOS**: Uses `WKWebView` with `UIKitView`.
- **Web**: Uses `HTMLIFrameElement` with `HtmlView`.

### 2. Two ComposeWebView Overloads

**Simple (URL-based):**

```kotlin
ComposeWebView(url = "https://example.com")
```

**Advanced (State-based):**

```kotlin
val state = rememberWebViewState(url = "...")
val controller = rememberWebViewController()
ComposeWebView(state = state, controller = controller)
```

### 3. JSBridge

Uses **optional** kotlinx-serialization by default, but supports custom serializers.

**Key features:**

- Type-safe: `bridge.register<Input, Output>("name") { ... }`
- Promise-based JavaScript API: `window.AppBridge.call('name', data)`
- Event emission: `bridge.emit("event", data)`

### 4. State Management

`WebViewState` tracks:

- `loadingState: LoadingState` (Idle/Loading/Finished)
- `lastLoadedUrl: String?`
- `errorsForCurrentRequest: List<WebViewError>`
- `pageTitle`, `pageIcon`

## Common Tasks

### Adding a New Feature

1. **Define in Common**: Add `expect` function/property in `WebViewPlatform.kt`.
2. **Implement in Platforms**: Add `actual` implementation in `androidMain`, `desktopMain`, `iosMain`, `jsMain`.
3. **Update API**: Add public API in `WebViewController` or `WebViewState` if needed.
4. **Test**: Verify on all platforms using the sample app.

### Modifying JSBridge

- Core logic: `WebViewJsBridge.kt`
- Serialization: `BridgeSerializer.kt`
- Injection: `ComposeWebView.kt` (injects JS script on page load)

## Code Style

- **Kotlin conventions**: PascalCase classes, camelCase functions.
- **Spotless**: Run `./gradlew :spotlessApply` before committing.
- **Composables**: Uppercase start, `Modifier` as last param.

## Important Notes

### Dependencies

- **KCEF**: Used for Desktop. Requires `maven("https://jogamp.org/deployment/maven")` in `build.gradle.kts`.
- **kotlinx-serialization**: Optional.

### Sample App

Demonstrates 4 key use cases across platforms:

- Basic browser
- JSBridge
- Fullscreen video
- Custom client

### Deployment

Uses GitHub Packages. See `DEPLOYMENT.md`.

## When Working on This Project

1. **Think Multiplatform**: Always consider how a change affects Android, iOS, Desktop, and Web.
2. **Check `expect`/`actual`**: Ensure all platforms are implemented.
3. **Run Spotless**: `./gradlew :spotlessApply` is mandatory.
4. **Test Locally**: Verify builds and tests (`./gradlew :compose-webview:allTests`).

---

**Last Updated**: 2025-12-09
**Maintainer**: parkwoocheol
