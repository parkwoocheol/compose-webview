# Project Overview

**ComposeWebView** is a Compose Multiplatform WebView wrapper library supporting Android, iOS, Desktop, and Web platforms.

## Basic Information

- **Maintainer**: parkwoocheol
- **Version**: Kotlin 2.2.0, Compose Multiplatform 1.9.3
- **License**: MIT

## Supported Platforms

- **Android**: `minSdk 24`, `compileSdk 36` (Stable)
- **iOS**: WKWebView-based (Stable)
- **Desktop**: CEF (Chromium Embedded Framework) via KCEF (Experimental)
- **Web (JS)**: Iframe-based (Experimental)
- **Web (WASM)**: Iframe-based (Experimental)

## Key Features

- **Reactive State Management**: Manages loading state, URL, errors via `WebViewState`
- **Promise-based JSBridge**: Type-safe bridge between JavaScript ↔ Kotlin
  - `async/await` support to avoid callback hell
  - **Kotlinx Serialization** built-in support
  - Event bus system (`emit`/`on`)
- **Custom View Support**: Fullscreen video, custom HTML views
- **Lifecycle Management**: Automatic `onResume`/`onPause` handling
- **File Upload**: Native file picker support on Android and iOS

## 2026 Project Direction

- **Mobile-First**: Android and iOS are stable and production-ready
- **Desktop/Web Experimental**: Desktop (CEF), Web (JS/WASM) are experimental
- **Type Safety**: Type-safe data transfer via Kotlinx Serialization
- **Performance Optimization**: State management and bridge performance improvements
