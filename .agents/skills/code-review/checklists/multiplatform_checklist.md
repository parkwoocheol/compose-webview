# Multiplatform Implementation Checklist

Specialized checklist for multiplatform features in ComposeWebView.

## Common (Shared) Implementation

### Design

- [ ] Feature is truly cross-platform (or platform-specific is justified)
- [ ] Common interface defined in `commonMain`
- [ ] Platform differences identified upfront
- [ ] Fallback behavior defined for unsupported platforms

### Common Code

File: `compose-webview/src/commonMain/kotlin/com/parkwoocheol/composewebview/`

- [ ] `expect` declarations for platform-specific parts
- [ ] Common business logic in `commonMain` (no platform code)
- [ ] Interfaces/sealed classes in common
- [ ] Shared data structures defined

### Documentation

- [ ] Common API documented with KDoc
- [ ] Platform differences noted in documentation
- [ ] Usage examples provided
- [ ] Known limitations stated

## Android Implementation

File: `compose-webview/src/androidMain/kotlin/com/parkwoocheol/composewebview/`

### Implementation

- [ ] `actual` implementation matches `expect` signature exactly
- [ ] Uses `android.webkit.WebView` APIs appropriately
- [ ] Handles Android-specific behavior
- [ ] Proper use of Context

### Android-Specific Considerations

- [ ] **Minimum SDK**: Works on API 24+ (Android 7.0)
- [ ] **Permissions**: Required permissions declared in docs
- [ ] **Lifecycle**: Handles Activity lifecycle (onResume/onPause)
- [ ] **Threading**: WebView operations on main thread

### WebView Features

- [ ] JavaScript interface configured correctly
- [ ] WebViewClient/WebChromeClient set up
- [ ] Settings configured appropriately
- [ ] File access handled (if needed)
- [ ] Cookie management working

### Testing

- [ ] Unit tests in `androidInstrumentedTest/`
- [ ] Tested on emulator (API 24, 29, 33+)
- [ ] Tested on physical device
- [ ] Edge cases covered

## iOS Implementation

File: `compose-webview/src/iosMain/kotlin/com/parkwoocheol/composewebview/`

### Implementation

- [ ] `actual` implementation matches `expect` signature exactly
- [ ] Uses `WKWebView` APIs appropriately
- [ ] Uses `UIKitView` for Compose integration
- [ ] Proper Objective-C/Swift interop

### iOS-Specific Considerations

- [ ] **Minimum iOS**: Works on iOS 14.0+
- [ ] **WKWebView constraints**: Documented limitations (e.g., zoom control)
- [ ] **Message handlers**: `WKScriptMessageHandler` configured
- [ ] **Security**: App Transport Security considered
- [ ] **Memory**: Proper retain/release (no leaks)

### WKWebView Features

- [ ] Configuration set up correctly
- [ ] Message handlers registered
- [ ] Navigation delegate implemented
- [ ] UI delegate implemented (if needed)
- [ ] User script injection working

### Testing

- [ ] Unit tests in `iosTest/`
- [ ] Tested on iOS Simulator (latest iOS)
- [ ] Tested on physical device (if possible)
- [ ] Different iOS versions tested

## Desktop Implementation

File: `compose-webview/src/desktopMain/kotlin/com/parkwoocheol/composewebview/`

### Implementation

- [ ] `actual` implementation matches `expect` signature exactly
- [ ] Uses CEF (via KCEF) appropriately
- [ ] Uses `SwingPanel` for Compose integration
- [ ] Handles async CEF initialization

### Desktop-Specific Considerations

- [ ] **JVM version**: Works on JVM 11+
- [ ] **CEF initialization**: Async init handled properly
- [ ] **Threading**: CEF thread model respected
- [ ] **Native libraries**: KCEF dependencies bundled
- [ ] **Platform**: Tested on macOS, Windows, Linux (or limitations noted)

### CEF Features

- [ ] Browser created correctly
- [ ] Message router configured
- [ ] Request handlers set up (if needed)
- [ ] Display handler implemented
- [ ] Resource disposal handled

### Testing

- [ ] Unit tests in `desktopTest/`
- [ ] Tested on macOS
- [ ] Tested on Windows (or limitations noted)
- [ ] Tested on Linux (or limitations noted)

## Web Implementation

File: `compose-webview/src/jsMain/kotlin/com/parkwoocheol/composewebview/`

### Implementation

- [ ] `actual` implementation matches `expect` signature exactly
- [ ] Uses appropriate web APIs
- [ ] IFrame or Canvas approach (consistent with design)
- [ ] postMessage bridge working

### Web-Specific Considerations

- [ ] **Browser compatibility**: Modern browsers supported
- [ ] **Sandbox limitations**: Documented
- [ ] **Communication**: postMessage bridge functional
- [ ] **No native features**: Platform limitations clear

### Web Features

- [ ] IFrame created and managed
- [ ] Message passing working
- [ ] Events handled correctly
- [ ] Limitations documented

### Testing

- [ ] Unit tests in `jsTest/`
- [ ] Tested in Chrome/Chromium
- [ ] Tested in Firefox
- [ ] Tested in Safari (if possible)

## Cross-Platform Validation

### Consistency

- [ ] Behavior consistent across platforms (or differences documented)
- [ ] Return values consistent
- [ ] Errors handled consistently
- [ ] State updates consistent

### Platform Differences

- [ ] All differences documented in KDoc
- [ ] Fallback behavior defined
- [ ] Unavailable features gracefully handled
- [ ] Platform notes in user documentation

### Signature Verification

- [ ] Exact same signature for all `actual` implementations
- [ ] Parameter names match
- [ ] Return types match
- [ ] Nullability matches

## Testing Across Platforms

### Unit Tests

- [ ] Common tests in `commonTest/`
- [ ] Platform-specific tests in each platform's test directory
- [ ] Shared test utilities (if applicable)
- [ ] Mock/fake implementations for testing

### Integration Tests

- [ ] Feature works end-to-end on each platform
- [ ] State synchronization working
- [ ] Events propagated correctly
- [ ] No platform-specific bugs

### Manual Testing

- [ ] Tested in sample app on Android
- [ ] Tested in sample app on iOS
- [ ] Tested in sample app on Desktop
- [ ] Tested in sample app on Web

## Performance Considerations

- [ ] No platform performs significantly worse than others
- [ ] Memory usage reasonable on all platforms
- [ ] No platform-specific memory leaks
- [ ] Startup time acceptable

## Documentation

### Platform Matrix

Document feature availability:

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|---------|-----|---------|-----|-------|
| Feature X | ✅ | ✅ | ⚠️ | ❌ | Desktop: Limited, Web: N/A |

### Platform-Specific Notes

In KDoc:
```kotlin
/**
 * Feature description.
 *
 * Platform notes:
 * - **Android**: Full support via WebView
 * - **iOS**: Limited zoom control due to WKWebView constraints
 * - **Desktop**: Requires CEF initialization
 * - **Web**: Not available (browser sandbox limitation)
 */
expect fun feature()
```

## Build Verification

### Compilation

- [ ] Android builds: `./gradlew :compose-webview:assembleDebug`
- [ ] iOS builds: `./gradlew :compose-webview:linkIosSimulatorArm64`
- [ ] Desktop builds: `./gradlew :compose-webview:compileKotlinDesktop`
- [ ] Web builds: `./gradlew :compose-webview:compileKotlinJs`

Quick check:
```bash
bash .agent/skills/development/scripts/build_all.sh
```

### Testing

- [ ] All tests pass: `bash .agent/skills/development/scripts/test_all.sh`
- [ ] Platform-specific tests run separately without errors

## Common Multiplatform Pitfalls

### ❌ Avoid

1. **Platform-specific code in common**:
   ```kotlin
   // commonMain - WRONG
   fun log(message: String) {
       android.util.Log.d("TAG", message) // Android-specific!
   }
   ```

2. **Inconsistent behavior**:
   ```kotlin
   // WRONG: Android returns true, iOS returns false for same input
   ```

3. **Missing platform implementations**:
   ```kotlin
   // WRONG: Forgot to implement for Web
   ```

4. **Signature mismatches**:
   ```kotlin
   // commonMain
   expect fun doSomething(param: String): Int

   // androidMain - WRONG
   actual fun doSomething(param: String): String // Different return type!
   ```

### ✅ Do

1. **Use expect/actual**:
   ```kotlin
   // commonMain
   expect fun log(tag: String, message: String)

   // androidMain
   actual fun log(tag: String, message: String) {
       android.util.Log.d(tag, message)
   }
   ```

2. **Consistent behavior or document differences**
3. **Implement all platforms**
4. **Match signatures exactly**

## Pre-Merge Checklist

- [ ] All 4 platforms implemented and tested
- [ ] Expect/actual check passes: `bash .agent/skills/code-review/scripts/check_expect_actual.sh`
- [ ] All platforms build: `bash .agent/skills/development/scripts/build_all.sh`
- [ ] All tests pass: `bash .agent/skills/development/scripts/test_all.sh`
- [ ] Platform differences documented
- [ ] Full review passes: `bash .agent/skills/code-review/scripts/review_checklist.sh`

---

## Quick Command Reference

```bash
# Check expect/actual completeness
bash .agent/skills/code-review/scripts/check_expect_actual.sh

# Platform implementation status
bash .agent/skills/development/scripts/platform_status.sh

# Build all platforms
bash .agent/skills/development/scripts/build_all.sh

# Run all tests
bash .agent/skills/development/scripts/test_all.sh
```

---

*Complete this checklist for all multiplatform features to ensure consistency and quality across all supported platforms.*
