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
    - **v1.6.1+ (future)**: Maven Central only (`io.github.parkwoocheol`)
    - **v1.6.0 (current)**: Available on all repositories but uses different group IDs
        - JitPack/GitHub Packages: `com.github.parkwoocheol:compose-webview:1.6.0`
        - Maven Central: `io.github.parkwoocheol:compose-webview:1.6.0` ← **Recommended**
    - **v1.5.x and earlier**: JitPack and GitHub Packages only (`com.github.parkwoocheol`)

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

| Version | Repository | Group ID | Coordinates |
|---------|------------|----------|-------------|
| v1.5.x and earlier | JitPack, GitHub Packages | `com.github.parkwoocheol` | `com.github.parkwoocheol:compose-webview:1.5.x` |
| **v1.6.0** (current) | JitPack, GitHub Packages | `com.github.parkwoocheol` | `com.github.parkwoocheol:compose-webview:1.6.0` |
| **v1.6.0** (current) | **Maven Central** | `io.github.parkwoocheol` | `io.github.parkwoocheol:compose-webview:1.6.0` |
| **v1.6.1+** (future) | **Maven Central only** | `io.github.parkwoocheol` | `io.github.parkwoocheol:compose-webview:1.6.1+` |

!!! info "v1.6.0 Group ID Difference"
    v1.6.0 uses different group IDs depending on the repository:

    - JitPack/GitHub Packages: `com.github.parkwoocheol`
    - Maven Central: `io.github.parkwoocheol`

    Same version, different coordinates.

#### Recommended: Three-Step Migration

=== "Step 1: Test v1.6.0"

    Upgrade to v1.6.0 while keeping your existing repository:

    ```kotlin
    // settings.gradle.kts - Keep existing repository
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // or GitHub Packages
    }

    // build.gradle.kts - Upgrade version only, keep same group ID
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("com.github.parkwoocheol:compose-webview:1.6.0")
            }
        }
    }
    ```

    Build and test your app.

=== "Step 2: Switch to Maven Central"

    Change group ID to use Maven Central:

    ```kotlin
    // settings.gradle.kts - Keep both temporarily
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // will remove soon
    }

    // build.gradle.kts - Change group ID to io.github
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.parkwoocheol:compose-webview:1.6.0")
            }
        }
    }
    ```

    Verify it downloads from Maven Central.

=== "Step 3: Clean Up"

    Remove old repositories:

    ```kotlin
    // settings.gradle.kts - Remove old repositories
    repositories {
        google()
        mavenCentral()  // All you need!
    }

    // build.gradle.kts - Same as Step 2
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("io.github.parkwoocheol:compose-webview:1.6.0")
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
            // Before: implementation("com.github.parkwoocheol:compose-webview:1.5.1")
            implementation("io.github.parkwoocheol:compose-webview:1.6.0")
        }
    }
}
```

!!! note "Backward Compatibility"
    - Old versions (v1.5.x and earlier) remain available on JitPack/GitHub Packages with `com.github` group ID
    - v1.6.0 is the current and last version available on JitPack/GitHub Packages
    - Future versions (v1.6.1+) will be Maven Central exclusive with `io.github` group ID

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
