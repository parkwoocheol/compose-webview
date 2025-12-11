# Workflow & Commands

## Adding a New Feature

1. **Define in Common**: Add `expect` in `WebViewPlatform.kt`.
2. **Implement in Platforms**: Add `actual` in `androidMain`, `desktopMain`, `iosMain`, `jsMain`.
3. **Update API**: Add public API in `WebViewController` or `WebViewState`.
4. **Test**: Verify on all platforms using simple app.

## Build & Verify

- **Run all tests**:

  ```bash
  ./gradlew :compose-webview:allTests
  ```

- **Lint check**:

  ```bash
  ./gradlew lint
  ```

- **Apply Formatting**:

  ```bash
  ./gradlew :spotlessApply
  ```

## Project Structure

```text
compose-webview/
├── compose-webview/          # Main library module
│   ├── src/commonMain/...    # Common code
│   ├── src/androidMain/...   # Android implementation
│   ├── src/iosMain/...       # iOS implementation
│   ├── src/desktopMain/...   # Desktop implementation
├── app/                      # Sample app (Multiplatform)
├── README.md                 # User-facing documentation
└── build.gradle.kts          # Root build script
```
