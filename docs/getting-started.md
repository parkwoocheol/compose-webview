# Getting Started

## Requirements

Before you begin, ensure your project meets the following requirements:

* **Android API Level**: 24+
* **iOS Deployment Target**: 14.0+
* **Java Version**: 11+
* **Jetpack Compose / Compose Multiplatform**: 1.9.3+
* **Kotlin**: 2.2.0+

---

## Installation

This library is available on **Maven Central** for universal access without authentication starting from **v1.6.0**.

!!! warning "Version Availability"
    - **v1.6.0+**: Maven Central only
    - **v1.6.0**: JitPack, GitHub Packages, and Maven Central (transition version)
    - **v1.5.x and earlier**: JitPack and GitHub Packages only

!!! info "Migration from v1.5.x"
    If you're upgrading from v1.5.x or earlier (JitPack/GitHub Packages), see the [Migration Guide](#migration-guide) below.

### Step 1. Configure Repository

Add Maven Central to `settings.gradle.kts` (or `settings.gradle`):

=== "Kotlin (`settings.gradle.kts`)"

    ```kotlin
    dependencyResolutionManagement {
        repositories {
            google()
            mavenCentral()
        }
    }
    ```

=== "Groovy (`settings.gradle`)"

    ```groovy
    dependencyResolutionManagement {
        repositories {
            google()
            mavenCentral()
        }
    }
    ```

### Step 2. Add Dependency

Add the dependency to your **commonMain** source set.

=== "Kotlin (`build.gradle.kts`)"

    ```kotlin
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.parkwoocheol:compose-webview:<version>")
            }
        }
    }
    ```

=== "Groovy (`build.gradle`)"

    ```groovy
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    implementation 'io.github.parkwoocheol:compose-webview:<version>'
                }
            }
        }
    }
    ```

!!! tip "Latest Version"
    Check [GitHub Releases](https://github.com/parkwoocheol/compose-webview/releases) or [Maven Central](https://central.sonatype.com/artifact/io.github.parkwoocheol/compose-webview) for the current version number.

### Platform-Specific Artifacts

The Kotlin Multiplatform Gradle plugin **automatically selects** the correct artifact:

| Platform | Artifact ID |
|----------|-------------|
| **Android** | `compose-webview-android` |
| **iOS (arm64)** | `compose-webview-iosarm64` |
| **iOS (x64)** | `compose-webview-iosx64` |
| **iOS (Simulator arm64)** | `compose-webview-iossimulatorarm64` |
| **Desktop (JVM)** | `compose-webview-desktop` |
| **Web (JS)** | `compose-webview-js` |
| **Web (WASM)** | `compose-webview-wasmjs` |

> **Note**: You don't need to specify platform-specific artifacts manually. Just use `compose-webview` and Gradle resolves the correct artifact automatically.

### Migration Guide

If you previously used this library from JitPack or GitHub Packages:

#### Version History

| Version | Group ID | Available On |
|---------|----------|--------------|
| v1.5.x and earlier | `com.github.parkwoocheol` | JitPack, GitHub Packages |
| **v1.6.0** | `io.github.parkwoocheol` | JitPack, GitHub Packages, **Maven Central** |
| **v1.6.0+** | `io.github.parkwoocheol` | **Maven Central only** |

#### Recommended: Two-Step Migration

=== "Step 1: Test with v1.6.0"

    First, upgrade to v1.6.0 while keeping your existing repository to test compatibility:

    ```kotlin
    // settings.gradle.kts - Keep existing repository
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // or GitHub Packages
    }

    // build.gradle.kts - Update to v1.6.0
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.parkwoocheol:compose-webview:1.6.0")
            }
        }
    }
    ```

    Build and test your app to ensure everything works.

=== "Step 2: Switch to Maven Central"

    After confirming compatibility, clean up and use the latest version:

    ```kotlin
    // settings.gradle.kts - Remove old repositories
    repositories {
        google()
        mavenCentral()  // All you need!
    }

    // build.gradle.kts - Use latest version
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.parkwoocheol:compose-webview:1.6.0")  // or later
            }
        }
    }
    ```

    Remove authentication configuration (GitHub tokens, etc.).

#### Quick Migration

For direct migration without intermediate testing:

```kotlin
// settings.gradle.kts
repositories {
    google()
    mavenCentral()
}

// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.parkwoocheol:compose-webview:1.6.0")  // or latest
        }
    }
}
```

!!! note "Backward Compatibility"
    - Old versions (v1.5.x and earlier) remain available on JitPack/GitHub Packages
    - v1.6.0 is the last version available on all repositories
    - v1.6.0+ are Maven Central exclusive

---

## Quick Start

Here is the minimal code required to get a functional WebView running in your Compose application.

### Basic Usage

Use `ComposeWebView` directly with a URL.

```kotlin title="BasicWebView.kt"
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.WebViewSettings

@Composable
fun MyBrowser() {
    ComposeWebView(
        url = "https://google.com",
        modifier = Modifier.fillMaxSize(),
        settings = WebViewSettings(
            javaScriptEnabled = true,
            domStorageEnabled = true
        )
    )
}
```

### With State & Controller

For most apps, you will want to control navigation (Back/Forward) or react to loading states. Use `rememberSaveableWebViewState` and `rememberWebViewController`.

```kotlin title="ControlledWebView.kt"
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import com.parkwoocheol.composewebview.*

@Composable
fun ControlledBrowser() {
    // 1. Create State and Controller
    val state = rememberSaveableWebViewState(url = "https://github.com")
    val controller = rememberWebViewController()

    Column(modifier = Modifier.fillMaxSize()) {
        // 2. Show Loading Indicator
        if (state.isLoading) {
            LinearProgressIndicator(
                progress = (state.loadingState as? LoadingState.Loading)?.progress ?: 0f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. The WebView
        ComposeWebView(
            state = state,
            controller = controller,
            modifier = Modifier.weight(1f),
            settings = WebViewSettings(
                javaScriptEnabled = true,
                domStorageEnabled = true
            )
        )
    }
}
```

## Next Steps

Now that you have a basic WebView, explore the advanced features:

* [**State Management**](guides/state-management.md): Learn about persistent vs transient state.
* [**JS Bridge**](guides/js-bridge.md): Communicate between Kotlin and JavaScript.
* [**Lifecycle & Errors**](guides/lifecycle.md): Handle app lifecycle and loading errors.
