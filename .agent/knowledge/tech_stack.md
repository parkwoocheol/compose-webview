# Tech Stack & Environment

- **Language**: Kotlin 1.9+ / 2.0 (Multiplatform)
- **UI Framework**: Jetpack Compose (Multiplatform)
- **Support Platforms**:
  - **Android**: `minSdk 24`, `compileSdk 36`, JVM 11
  - **iOS**: WKWebView based (via `UIKitView`)
  - **Desktop**: CEF (Chromium Embedded Framework) via `dev.datlag:kcef`. Requires `JogAmp` maven repository.
  - **Web**: IFrame based (via `HtmlView`)
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)
- **Formatting**: Spotless (using `ktlint`)
