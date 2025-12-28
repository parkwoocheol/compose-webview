# [Guide Title]

Brief introduction explaining what this guide covers and what the reader will learn (2-3 sentences).

## Prerequisites

List what readers need to know or have installed before following this guide:

- ComposeWebView 1.0.0 or later
- Basic knowledge of Jetpack Compose
- [Other prerequisite]

## Overview

Provide a high-level overview of the topic (3-5 sentences).

Explain:
- Why this feature exists
- When to use it
- Key concepts involved

## Quick Start

Provide a minimal working example that demonstrates the core concept:

```kotlin
@Composable
fun MinimalExample() {
    // Simplest possible usage
    val state = rememberWebViewState("https://example.com")

    ComposeWebView(
        state = state,
        modifier = Modifier.fillMaxSize()
    )
}
```

## Detailed Guide

### Step 1: [First Major Step]

Explain the first step in detail.

```kotlin
// Code example demonstrating step 1
val state = rememberWebViewState(
    initialUrl = "https://example.com",
    additionalHttpHeaders = mapOf(
        "Custom-Header" to "Value"
    )
)
```

**Key points**:
- Point 1 about this step
- Point 2 about this step

### Step 2: [Second Major Step]

Explain the second step.

```kotlin
// Code example for step 2
val controller = rememberWebViewController(state)

LaunchedEffect(someCondition) {
    controller.loadUrl("https://newurl.com")
}
```

### Step 3: [Third Major Step]

Continue with additional steps as needed.

## Advanced Usage

### [Advanced Topic 1]

Show more sophisticated usage:

```kotlin
@Composable
fun AdvancedExample() {
    val state = rememberWebViewState("https://example.com")
    val controller = rememberWebViewController(state)

    // Advanced configuration
    ComposeWebView(
        state = state,
        modifier = Modifier.fillMaxSize(),
        onCreated = { webView ->
            // Platform-specific configuration
        }
    )

    // Handle state changes
    LaunchedEffect(state.loadingState) {
        when (state.loadingState) {
            LoadingState.Loading -> {
                // Handle loading
            }
            LoadingState.Finished -> {
                // Handle finished
            }
        }
    }
}
```

### [Advanced Topic 2]

Additional advanced patterns or techniques.

## Platform-Specific Considerations

!!! info "Platform Differences"
    **Android**: Specific behavior or limitation
    **iOS**: Specific behavior or limitation
    **Desktop**: Specific behavior or limitation
    **Web**: Specific behavior or limitation

### Android

Details specific to Android implementation.

```kotlin
// Android-specific code if needed
```

### iOS

Details specific to iOS implementation.

```kotlin
// iOS-specific code if needed
```

## Best Practices

List recommended patterns and practices:

1. **Practice 1**: Explanation
   ```kotlin
   // Example of best practice
   ```

2. **Practice 2**: Explanation
   ```kotlin
   // Another example
   ```

3. **Practice 3**: Explanation

## Common Issues

### Issue 1: [Problem Description]

**Symptoms**: What the user experiences

**Cause**: Why this happens

**Solution**:
```kotlin
// Code showing the fix
```

### Issue 2: [Another Problem]

Description and solution.

## Complete Example

Provide a comprehensive, real-world example:

```kotlin
@Composable
fun CompleteWebViewExample() {
    // State
    val state = rememberWebViewState("https://example.com")
    val controller = rememberWebViewController(state)

    // UI
    Column(modifier = Modifier.fillMaxSize()) {
        // Loading indicator
        if (state.loadingState == LoadingState.Loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // WebView
        ComposeWebView(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { controller.goBack() },
                enabled = state.canGoBack
            ) {
                Text("Back")
            }

            Button(
                onClick = { controller.goForward() },
                enabled = state.canGoForward
            ) {
                Text("Forward")
            }

            Button(onClick = { controller.reload() }) {
                Text("Reload")
            }
        }
    }
}
```

## API Reference

Quick reference to related APIs:

- [`ComposeWebView`](../api/compose-webview.md#composewebview) - Main composable
- [`WebViewState`](../api/types.md#webviewstate) - State holder
- [`WebViewController`](../api/types.md#webviewcontroller) - Controller interface

## Next Steps

- [Related Topic 1](link-to-related-topic.md)
- [Related Topic 2](link-to-another-topic.md)
- [Advanced Feature](link-to-advanced-feature.md)

## Additional Resources

- [External Resource 1](https://example.com)
- [External Resource 2](https://example.com)

---

**Related guides**:
- [State Management](state-management.md)
- [JS Bridge](js-bridge.md)

**API reference**:
- [ComposeWebView API](../api/compose-webview.md)

---

*Last updated: YYYY-MM-DD*
