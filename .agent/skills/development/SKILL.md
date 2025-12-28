---
name: composewebview-development
description: Builds, tests, and formats ComposeWebView multiplatform library. Handles Android, iOS, Desktop (JVM), and Web (JS) platforms. Use when building, testing, running Spotless formatting, or checking platform-specific code. Supports expect/actual pattern workflows.
---

# ComposeWebView Development Workflow

This skill automates building, testing, and formatting for the ComposeWebView multiplatform library across Android, iOS, Desktop (JVM), and Web (JS) platforms.

## Quick Commands

### Format Code (REQUIRED before commits)
```bash
./gradlew spotlessApply
```

### Run All Tests
```bash
./gradlew :compose-webview:allTests
```

### Build All Platforms
```bash
bash .agent/skills/development/scripts/build_all.sh
```

### Check Implementation Status
```bash
bash .agent/skills/development/scripts/platform_status.sh
```

## Platform-Specific Workflows

### Android
```bash
# Build
./gradlew :compose-webview:assembleDebug

# Test
./gradlew :compose-webview:testDebugUnitTest

# Lint
./gradlew lintDebug
```

### iOS
```bash
# Link (build)
./gradlew :compose-webview:linkIosSimulatorArm64

# Test
./gradlew :compose-webview:iosSimulatorArm64Test
```

### Desktop (JVM)
```bash
# Compile
./gradlew :compose-webview:compileKotlinDesktop

# Test
./gradlew :compose-webview:desktopTest
```

### Web (JS)
```bash
# Compile
./gradlew :compose-webview:compileKotlinJs

# Test
./gradlew :compose-webview:jsTest
```

## Adding New Features

ComposeWebView uses the **expect/actual pattern** for multiplatform code. Follow these steps:

1. **Define in Common**: Add `expect` declaration in `commonMain/kotlin/com/parkwoocheol/composewebview/`
   ```kotlin
   expect class PlatformFeature {
       fun doSomething(): String
   }
   ```

2. **Implement per Platform**: Add `actual` implementation in each platform's source set
   - `androidMain/` - Android WebView implementation
   - `iosMain/` - WKWebView implementation
   - `desktopMain/` - CEF (Chromium Embedded Framework) implementation
   - `jsMain/` - Web/JS implementation

3. **Verify Completeness**: Run platform status check
   ```bash
   bash .agent/skills/development/scripts/platform_status.sh
   ```

4. **Test**: Run platform-specific tests
   ```bash
   bash .agent/skills/development/scripts/test_all.sh
   ```

5. **Format**: MUST run Spotless before committing
   ```bash
   ./gradlew spotlessApply
   ```

See [reference/platform_specifics.md](reference/platform_specifics.md) for detailed platform patterns and constraints.

## Code Templates

### Creating expect/actual
Use the template: [templates/ExpectActualTemplate.kt.template](templates/ExpectActualTemplate.kt.template)

### Creating Composables
Use the template: [templates/ComposableTemplate.kt.template](templates/ComposableTemplate.kt.template)

## Gradle Tasks Reference

For a complete list of available Gradle tasks, see [reference/gradle_tasks.md](reference/gradle_tasks.md).

## Common Tasks

### Clean Build
```bash
./gradlew clean
```

### Build and Test Everything
```bash
bash .agent/skills/development/scripts/test_all.sh
```

### Check Code Formatting (without applying)
```bash
bash .agent/skills/development/scripts/format_check.sh
```

## Platform Constraints

### iOS (WKWebView)
- Limited zoom control
- Strict security policies
- Requires message handlers for JSBridge

### Desktop (CEF via KCEF)
- Asynchronous initialization required
- SwingPanel integration
- Platform-specific threading considerations

### Web (JS)
- IFrame-based implementation
- postMessage bridge for communication
- Limited native features

See detailed constraints in [reference/platform_specifics.md](reference/platform_specifics.md).

## Scripts

This skill provides the following automation scripts:

- **build_all.sh**: Builds all platforms sequentially with error handling
- **test_all.sh**: Runs comprehensive test suite across all platforms
- **format_check.sh**: Checks Spotless formatting (for CI/pre-commit hooks)
- **platform_status.sh**: Reports expect/actual implementation completeness

All scripts are located in `scripts/` and can be run from the project root.

## Troubleshooting

### Build Failures
1. Clean Gradle cache: `./gradlew clean`
2. Invalidate caches: `rm -rf .gradle`
3. Check Java version: `java -version` (should be 17+)

### Test Failures
1. Run specific platform tests to isolate issues
2. Check platform-specific logs
3. Verify all expect/actual pairs are implemented

### Formatting Issues
Run `./gradlew spotlessApply` to auto-fix formatting issues.

## Related Resources

- **Architecture**: See `.agent/knowledge/architecture.md` for design patterns
- **Commands**: See `.agent/knowledge/commands.md` for workflow reference
- **Code Style**: See `.agent/knowledge/code_style.md` for coding conventions
