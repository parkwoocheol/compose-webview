# Tech Stack & Environment

## Core Technologies

- **Language**: Kotlin 2.2.0 (Multiplatform)
- **UI Framework**: Compose Multiplatform 1.9.3
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)
- **Code Formatting**: Spotless (using ktlint)

## Supported Platforms

### Android

- **minSdk**: 24
- **compileSdk**: 36
- **JVM Target**: 11
- **Implementation**: Wraps `android.webkit.WebView`
- **Status**: ✅ Stable

### iOS

- **Minimum Version**: iOS 14.0+
- **Implementation**: Based on `WKWebView` (using `UIKitView`)
- **Status**: ✅ Stable
- **Key Constraints**: Limited zoom control, 100ms progress polling

### Desktop (JVM)

- **JVM Version**: 11+
- **Implementation**: CEF (Chromium Embedded Framework) via `dev.datlag:kcef`
- **Dependencies**: Requires `JogAmp` maven repository
- **UI**: `SwingPanel` integration
- **Status**: 🚧 Experimental
- **Key Constraints**: Asynchronous CEF initialization, threading considerations

### Web (JS)

- **Implementation**: Iframe-based (`HtmlView`)
- **Status**: 🚧 Experimental
- **Key Constraints**: CORS restrictions, `postMessage` bridge

### Web (WASM)

- **Implementation**: Iframe-based (dynamic positioning)
- **Status**: 🚧 Experimental
- **Key Constraints**: Same-origin policy, limited native features

## Key Dependencies

- **Kotlinx Serialization**: JSON serialization/deserialization
- **Kotlinx Coroutines**: Asynchronous operations
- **Compose Runtime/Foundation/Material3**: UI composition
- **KCEF** (Desktop): Chromium browser integration

## 2026 Best Practices

- **Kotlinx Serialization First**: Native KMP support and performance
- **Null Safety**: Avoid `!!`, use safe call `?.` and Elvis operator `?:`
- **Immutability**: `val` over `var`
- **Data Classes**: Use for model definitions
- **Extension Functions**: For clean modularization
