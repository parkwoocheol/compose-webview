# Claude AI Guide for ComposeWebView

This document helps Claude AI (and other AI assistants) understand and work with this project effectively.

## Project Overview

**ComposeWebView** is a Jetpack Compose library that provides a powerful WebView wrapper with:
- Type-safe JavaScript ↔ Kotlin bridge (JSBridge)
- Reactive state management
- Lifecycle handling
- Custom view support (fullscreen videos, etc.)

**Target**: Android developers using Jetpack Compose

## Project Structure

```
compose-webview/
├── compose-webview/          # Main library module
│   └── src/main/kotlin/com/parkwoocheol/composewebview/
│       ├── ComposeWebView.kt        # Main composable, two overloads (url/state)
│       ├── WebViewState.kt          # State holder (loadingState, errors, etc.)
│       ├── WebViewController.kt     # Navigation controller (20+ utility methods)
│       ├── WebViewJsBridge.kt       # JS↔Kotlin bridge with serialization
│       ├── WebContent.kt            # Sealed class for content types
│       ├── WebViewError.kt          # Error data classes
│       ├── BridgeSerializer.kt      # Interface for custom serializers
│       └── client/
│           ├── ComposeWebViewClient.kt
│           └── ComposeWebChromeClient.kt
├── app/                      # Sample app with 4 demo screens
│   └── src/main/kotlin/.../MainActivity.kt
│       ├── BrowserScreen         # Full browser with URL bar
│       ├── HtmlJsScreen          # JSBridge demo
│       ├── VideoScreen           # Fullscreen video
│       └── CustomClientScreen    # URL blocking example
├── README.md                 # User-facing documentation
├── CONTRIBUTING.md           # Contributor guide (casual tone)
├── CODE_OF_CONDUCT.md        # Community guidelines
└── DEPLOYMENT.md             # GitHub Packages deployment guide (Korean)
```

## Key Concepts

### 1. Two ComposeWebView Overloads

**Simple (URL-based):**
```kotlin
ComposeWebView(url = "https://example.com")
```

**Advanced (State-based):**
```kotlin
val state = rememberWebViewState(url = "...")
val controller = rememberWebViewController()
ComposeWebView(state = state, controller = controller)
```

### 2. JSBridge

Uses **optional** kotlinx-serialization by default, but supports custom serializers (Gson, Moshi).

**Key features:**
- Type-safe: `bridge.register<Input, Output>("name") { ... }`
- Promise-based JavaScript API: `window.AppBridge.call('name', data)`
- Event emission: `bridge.emit("event", data)`
- Custom serializer support via `BridgeSerializer` interface

### 3. State Management

`WebViewState` tracks:
- `loadingState: LoadingState` (Idle/Loading/Finished)
- `lastLoadedUrl: String?`
- `errorsForCurrentRequest: List<WebViewError>`
- `pageTitle`, `pageIcon`, `jsDialogState`, `customViewState`

### 4. Controller

`WebViewController` provides 20+ methods:
- Navigation: `loadUrl()`, `navigateBack()`, `reload()`
- Content: `loadHtml()`, `postUrl()`
- Utilities: `zoomIn()`, `clearCache()`, `findAllAsync()`, etc.

## Common Tasks

### Adding a New Feature

1. Check existing code in relevant files (use symbolic tools)
2. Match existing patterns (KDoc, naming, Composable guidelines)
3. Update README if it's a public API change
4. Test with sample app

### Modifying JSBridge

- Core logic: `WebViewJsBridge.kt`
- Serialization: `BridgeSerializer.kt` (interface) + default impl in WebViewJsBridge
- Injection: `ComposeWebView.kt` (injects JS script on page load)

### Updating Documentation

- **README.md**: User-facing, comprehensive examples
- **KDoc**: All public APIs need documentation
- **Sample app**: Add demo if feature is visual/interactive

## Code Style

- **Kotlin conventions**: PascalCase classes, camelCase functions
- **Indentation**: 4 spaces
- **Composables**: Uppercase start, `Modifier` as last param with default
- **State**: Prefer immutable, use `remember`/`LaunchedEffect` appropriately

## Important Notes

### Dependencies

- **kotlinx-serialization is OPTIONAL** - only needed if using default serializer
- Users can provide custom `BridgeSerializer` for Gson/Moshi

### Overloads

`ComposeWebView` has TWO overloads:
1. Simple: Takes `url: String` directly
2. Advanced: Takes `state: WebViewState` + optional `jsBridge`

Don't confuse them when suggesting code!

### Sample App

Located in `app/` module, demonstrates 4 key use cases:
- Basic browser (URL bar, navigation, loading)
- JSBridge (bidirectional communication)
- Fullscreen video (custom view handling)
- Custom client (URL blocking)

### Deployment

Uses GitHub Packages (see DEPLOYMENT.md - Korean)

## When Working on This Project

1. **Read before writing**: Always check existing implementations
2. **Use symbolic tools**: Don't read entire files unnecessarily
3. **Match existing patterns**: Consistency is key
4. **Keep it simple**: Don't over-engineer
5. **Test with sample app**: Verify changes work

## File Reading Strategy

1. Start with `get_symbols_overview` for new files
2. Use `find_symbol` with `include_body=true` for specific symbols
3. Only read full files when absolutely necessary
4. Use `find_referencing_symbols` to understand symbol usage

## Questions to Ask

Before making changes:
- Is this consistent with existing patterns?
- Does this need documentation updates?
- Should this be demonstrated in the sample app?
- Is this a breaking change?

## Tone & Communication

- **Documentation**: Clear, concise, example-driven
- **Comments**: Explain "why", not "what"
- **CONTRIBUTING.md**: Casual, friendly, not overly formal
- **README.md**: Professional but approachable

---

**Last Updated**: 2025-12-01
**Maintainer**: parkwoocheol