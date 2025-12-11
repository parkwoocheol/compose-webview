# Architecture & Design Patterns

## Multiplatform Structure

- **Common First**: Define all features in `commonMain` using `expect`.
- **Platform Specific**:
  - `androidMain`: Map to `android.webkit.WebView`.
  - `iosMain`: Map to `WKWebView`, use `UikitView`.
  - `desktopMain`: Use `SwingPanel` and `KCEF`.
- **PlatformContext**: Abstract Platform Context (Android) or Application for usage.

## State Management

- **WebViewState**: Class-based State holder managing loading state, URL, page title, etc.
  - Tracks: `loadingState`, `lastLoadedUrl`, `errorsForCurrentRequest`, `pageTitle`, `pageIcon`.
- **WebViewController**: Controller separation pattern for handling commands (load, go back, postUrl, etc.).

## JSBridge

- **Mechanism**:
  - Android: `addJavascriptInterface`
  - iOS: `WKScriptMessageHandler`
  - Desktop: CEF MessageRouter
- **Features**:
  - Type-safe registration: `bridge.register<Input, Output>`
  - Promise-based JS API: `window.AppBridge.call`
  - Event emission: `bridge.emit`
  - Core logic in `WebViewJsBridge.kt`.

## Agent Guidelines

- **Agent Mode**: Write Implementation Plan (`implementation_plan.md`) in **Korean** and request user review.
- **iOS**: Consider `WKWebView` constraints (e.g., Zoom Control).
- **Desktop**: Pay attention to asynchronous CEF initialization.
