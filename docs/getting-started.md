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

This library is available from **JitPack** and **GitHub Packages**. Choose based on your platform needs:

| Repository | Best For | Authentication |
|-----------|---------|---------------|
| **JitPack** | Android, Desktop, Web, WASM | None required |
| **GitHub Packages** | **iOS projects** (also supports all other platforms) | GitHub token required |

### Option 1: JitPack (Recommended for non-iOS projects)

1. **Add the JitPack repository**

    === "Kotlin (`settings.gradle.kts`)"

        ```kotlin
        dependencyResolutionManagement {
            repositories {
                google()
                mavenCentral()
                maven { url = uri("https://jitpack.io") }
            }
        }
        ```

    === "Groovy (`settings.gradle`)"

        ```groovy
        dependencyResolutionManagement {
            repositories {
                google()
                mavenCentral()
                maven { url = "https://jitpack.io" }
            }
        }
        ```

### Option 2: GitHub Packages (Required for iOS)

GitHub Packages includes **iOS klib artifacts** built on macOS. JitPack builds on Linux and cannot produce these.

1. **Create a GitHub Personal Access Token (PAT)**
    * Go to [GitHub Settings → Developer settings → Personal access tokens](https://github.com/settings/tokens)
    * Click "Generate new token (classic)"
    * Scopes: Select `read:packages`

2. **Configure credentials** (`~/.gradle/gradle.properties`):

    ```properties
    gpr.user=YOUR_GITHUB_USERNAME
    gpr.key=YOUR_GITHUB_PAT
    ```

3. **Add GitHub Packages repository**

    === "Kotlin (`settings.gradle.kts`)"

        ```kotlin
        dependencyResolutionManagement {
            repositories {
                google()
                mavenCentral()
                maven {
                    url = uri("https://maven.pkg.github.com/parkwoocheol/compose-webview")
                    credentials {
                        username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                        password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
        ```

### Add the Dependency (shared/common module)

    === "Kotlin (`build.gradle.kts`)"

        ```kotlin
        kotlin {
            sourceSets {
                commonMain.dependencies {
                    implementation("com.github.parkwoocheol:compose-webview:<version>")
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
                        implementation 'com.github.parkwoocheol:compose-webview:<version>'
                    }
                }
            }
        }
        ```

!!! tip "Latest Version"
    Check [GitHub Releases](https://github.com/parkwoocheol/compose-webview/releases) or the [JitPack badge](https://jitpack.io/#parkwoocheol/compose-webview) for the current version number.

### Platform-Specific Artifacts

The Kotlin Multiplatform Gradle plugin **automatically selects** the correct artifact:

| Platform | Artifact ID |
|----------|-------------|
| **Android** | `compose-webview-android` |
| **iOS (arm64)** | `compose-webview-iosarm64` |
| **iOS (Simulator arm64)** | `compose-webview-iossimulatorarm64` |
| **Desktop (JVM)** | `compose-webview-desktop` |
| **Web (JS)** | `compose-webview-js` |
| **Web (WASM)** | `compose-webview-wasmjs` |

> **Note**: You don't need to specify platform-specific artifacts manually. Just use `compose-webview` and Gradle resolves the correct artifact automatically.

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
