# Welcome to ComposeWebView

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Release](https://jitpack.io/v/parkwoocheol/compose-webview.svg)](https://jitpack.io/#parkwoocheol/compose-webview)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.9.3-blue.svg)](https://github.com/JetBrains/compose-multiplatform)

**ComposeWebView** is a powerful, flexible, and feature-rich WebView wrapper designed specifically for **Jetpack Compose** and **Compose Multiplatform**.

It provides a unified API to control WebViews across **Android**, **iOS**, **Desktop**, and **Web**, with a strong focus on mobile productivity and developer experience.

---

## ✨ Key Features

<div class="grid cards" markdown>

- :material-cellphone-link: **Multiplatform Support**
    ---

    Run your WebView logic on Android, iOS, Desktop (CEF), and Web (JS) with a single codebase.

- :material-language-javascript: **Advanced JSBridge**
    ---

    Promise-based, type-safe communication between Kotlin and JavaScript. No more callback hell.

- :material-state-machine: **Reactive State**
    ---

    Monitor URL changes, loading progress, and errors using standard Compose state objects.

- :material-upload: **Rich Capabilities**
    ---

    Built-in support for File Uploads, Fullscreen Video, and Custom Views.

</div>

## 📱 Platform Support

| Platform | Implementation | Status | Note |
| :--- | :--- | :--- | :--- |
| **Android** | `AndroidView` (WebView) | :white_check_mark: Stable | Full feature support |
| **iOS** | `UIKitView` (WKWebView) | :white_check_mark: Stable | Full feature support (Seamless JS Bridge) |
| **Desktop** | `SwingPanel` (CEF via KCEF) | :construction: Experimental | **WIP**: Basic browsing works. KCEF integration in progress. |
| **Web (JS)** | `Iframe` (DOM) | :construction: Experimental | **WIP**: Basic navigation and `postMessage` bridge. |

!!! note "Project Focus: Mobile Productivity"
    This library is optimized for **Mobile (Android & iOS)** development. While Desktop and Web are supported, they are currently experimental. If you need a battle-tested solution primarily for Desktop/Web, other libraries might be a better fit.

## 🚀 Get Started

Ready to start building? Check out the **[Getting Started](getting-started.md)** guide to add `compose-webview` to your project in minutes.

[Get Started](getting-started.md){ .md-button .md-button--primary }
