# Workflow & Commands

## Adding a New Feature

### 1. Define in Common

Add `expect` in `WebViewPlatform.kt` or related file:

```kotlin
expect class PlatformFeature {
    fun doSomething(): String
}
```

### 2. Implement in Platforms

Add `actual` implementation in each platform source set:

- **androidMain**: Android WebView implementation
- **iosMain**: WKWebView implementation
- **desktopMain**: CEF implementation
- **jsMain**: Web/JS implementation
- **wasmJsMain**: WASM implementation

### 3. Update Public API

Add public API to `WebViewController` or `WebViewState`.

### 4. Test

Verify on all platforms using a simple app.

## Build & Verification

### Run All Tests

```bash
./gradlew :compose-webview:allTests
```

### Platform-Specific Builds

#### Android

```bash
./gradlew :compose-webview:assembleDebug
```

#### iOS

```bash
# Simulator Arm64
./gradlew :compose-webview:linkIosSimulatorArm64

# Real device (Arm64)
./gradlew :compose-webview:linkIosArm64

# Simulator x64 (Intel Mac)
./gradlew :compose-webview:linkIosX64
```

#### Desktop

```bash
./gradlew :compose-webview:compileKotlinDesktop
```

#### Web (JS)

```bash
./gradlew :compose-webview:compileKotlinJs
```

#### Web (WASM)

```bash
./gradlew :compose-webview:compileKotlinWasmJs
```

### Platform-Specific Tests

#### Android

```bash
./gradlew :compose-webview:testDebugUnitTest
```

#### iOS

```bash
# Simulator Arm64
./gradlew :compose-webview:iosSimulatorArm64Test

# Real device (Arm64)
./gradlew :compose-webview:iosArm64Test

# Simulator x64
./gradlew :compose-webview:iosX64Test
```

#### Desktop

```bash
./gradlew :compose-webview:desktopTest
```

#### Web (JS)

```bash
./gradlew :compose-webview:jsTest
```

#### Web (WASM)

```bash
./gradlew :compose-webview:wasmJsTest
```

### Lint Check

```bash
./gradlew lint
```

### Apply Formatting

```bash
./gradlew spotlessApply
```

## Project Structure

```text
compose-webview/
├── compose-webview/          # Main library module
│   ├── src/
│   │   ├── commonMain/...    # Common code (expect)
│   │   ├── androidMain/...   # Android implementation (actual)
│   │   ├── iosMain/...       # iOS implementation (actual)
│   │   ├── desktopMain/...   # Desktop implementation (actual)
│   │   ├── jsMain/...        # Web/JS implementation (actual)
│   │   └── wasmJsMain/...    # WASM implementation (actual)
│   └── build.gradle.kts
├── app/                      # Sample app (Multiplatform)
├── docs/                     # MkDocs documentation
├── .agent/                   # AI agent configuration
├── README.md
└── build.gradle.kts          # Root build script
```

## Gradle Tasks Reference

### Useful Tasks

- **Clean**: `./gradlew clean`
- **Build All Platforms**: `./gradlew build`
- **All Tests**: `./gradlew :compose-webview:allTests`
- **Specific Platform Build**: `./gradlew :compose-webview:compile<Platform>`
- **Publishing**: `./gradlew publish` (GitHub Packages)

### View Task List

```bash
./gradlew tasks --all
```

## Best Practices

1. **Common First**: Always start in `commonMain`
2. **expect/actual Pairs**: All platforms must have implementations
3. **Run Spotless**: Required before committing
4. **Write KDoc**: Document all public APIs
5. **Write Tests**: `commonTest` and platform-specific tests
