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

Artifacts are distributed via **JitPack**.

### Add the Repository

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

The dependency snippet is identical in both Kotlin and Groovy DSL examples above.

!!! tip "Latest Version"
    Check [GitHub Releases](https://github.com/parkwoocheol/compose-webview/releases) or the [JitPack badge](https://jitpack.io/#parkwoocheol/compose-webview) for the current version number.

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
