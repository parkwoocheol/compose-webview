# Gradle Tasks Reference

Comprehensive list of Gradle tasks available for ComposeWebView development.

## Common Tasks

### Building

```bash
# Build all platforms
./gradlew build

# Build specific platform
./gradlew :compose-webview:assembleDebug          # Android
./gradlew :compose-webview:linkIosSimulatorArm64  # iOS
./gradlew :compose-webview:compileKotlinDesktop   # Desktop
./gradlew :compose-webview:compileKotlinJs        # Web

# Clean build
./gradlew clean
```

### Testing

```bash
# Run all tests
./gradlew :compose-webview:allTests

# Run platform-specific tests
./gradlew :compose-webview:testDebugUnitTest       # Android
./gradlew :compose-webview:iosSimulatorArm64Test  # iOS
./gradlew :compose-webview:desktopTest             # Desktop
./gradlew :compose-webview:jsTest                  # Web

# Run common tests
./gradlew :compose-webview:testDebugUnitTest       # Includes commonTest
```

### Code Quality

```bash
# Apply code formatting (Spotless)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Android lint
./gradlew lint
./gradlew lintDebug

# Generate lint report
./gradlew lintReport
```

## Platform-Specific Tasks

### Android

```bash
# Assemble variants
./gradlew :compose-webview:assembleDebug
./gradlew :compose-webview:assembleRelease

# Install on device/emulator
./gradlew :compose-webview:installDebug

# Unit tests
./gradlew :compose-webview:testDebugUnitTest
./gradlew :compose-webview:testReleaseUnitTest

# Instrumented tests
./gradlew :compose-webview:connectedDebugAndroidTest

# Lint
./gradlew :compose-webview:lintDebug
./gradlew :compose-webview:lintRelease
```

### iOS

```bash
# Link binaries
./gradlew :compose-webview:linkIosArm64
./gradlew :compose-webview:linkIosSimulatorArm64
./gradlew :compose-webview:linkIosX64

# Test
./gradlew :compose-webview:iosArm64Test
./gradlew :compose-webview:iosSimulatorArm64Test
./gradlew :compose-webview:iosX64Test

# Build framework
./gradlew :compose-webview:linkDebugFrameworkIosArm64
./gradlew :compose-webview:linkReleaseFrameworkIosArm64
```

### Desktop (JVM)

```bash
# Compile
./gradlew :compose-webview:compileKotlinDesktop

# Test
./gradlew :compose-webview:desktopTest

# Run sample app
./gradlew :app:run
```

### Web (JS)

```bash
# Compile
./gradlew :compose-webview:compileKotlinJs

# Test
./gradlew :compose-webview:jsTest

# Browser test
./gradlew :compose-webview:jsBrowserTest

# Node.js test
./gradlew :compose-webview:jsNodeTest
```

## Sample App Tasks

```bash
# Run Android app
./gradlew :app:installDebug

# Run Desktop app
./gradlew :app:run

# Build all app variants
./gradlew :app:assemble
```

## Documentation Tasks

```bash
# Generate Dokka documentation (if configured)
./gradlew dokkaHtml
./gradlew dokkaGfm
```

## Publishing Tasks

```bash
# Publish to Maven Local (for testing)
./gradlew publishToMavenLocal

# Publish to repository (requires configuration)
./gradlew publish
```

## Advanced Tasks

### Dependency Management

```bash
# List dependencies
./gradlew :compose-webview:dependencies

# Dependency insight
./gradlew :compose-webview:dependencyInsight --dependency kotlinx-coroutines-core

# Check for dependency updates
./gradlew dependencyUpdates
```

### Project Information

```bash
# List all tasks
./gradlew tasks

# List all tasks (including private)
./gradlew tasks --all

# Project properties
./gradlew properties

# Project structure
./gradlew projects
```

### Build Scans

```bash
# Generate build scan
./gradlew build --scan
```

### Caching

```bash
# Build with cache
./gradlew build --build-cache

# Clean cache
rm -rf .gradle/caches
```

## Useful Task Combinations

### Full Clean Build

```bash
./gradlew clean build
```

### Build and Test Everything

```bash
./gradlew clean build :compose-webview:allTests
```

### Format and Lint

```bash
./gradlew spotlessApply lint
```

### Pre-Commit Check

```bash
./gradlew spotlessCheck :compose-webview:allTests lint
```

## Task Options

### Common Flags

```bash
# Continue on failure
./gradlew build --continue

# Parallel execution
./gradlew build --parallel

# Offline mode
./gradlew build --offline

# Refresh dependencies
./gradlew build --refresh-dependencies

# Show stack traces
./gradlew build --stacktrace   # Brief
./gradlew build --full-stacktrace  # Full

# Quiet output
./gradlew build --quiet

# Debug output
./gradlew build --debug

# Profile build
./gradlew build --profile
```

## Multiplatform-Specific

### Compilation Targets

```bash
# Check configured targets
./gradlew :compose-webview:targets

# Compile for specific target
./gradlew :compose-webview:compileKotlinAndroid
./gradlew :compose-webview:compileKotlinIosSimulatorArm64
./gradlew :compose-webview:compileKotlinDesktop
./gradlew :compose-webview:compileKotlinJs
```

### Source Sets

```bash
# List source sets
./gradlew :compose-webview:sourceSets
```

## CI/CD Tasks

### For Continuous Integration

```bash
# Full verification
./gradlew clean build :compose-webview:allTests spotlessCheck lint

# Android-only verification
./gradlew :compose-webview:assembleDebug :compose-webview:testDebugUnitTest lintDebug

# Quick check (formatting + tests)
./gradlew spotlessCheck :compose-webview:allTests
```

## Troubleshooting Tasks

```bash
# Clean everything
./gradlew clean
rm -rf .gradle
rm -rf build
rm -rf */build

# Refresh dependencies and rebuild
./gradlew clean build --refresh-dependencies

# Debug build with full output
./gradlew clean build --debug --stacktrace --info
```

## Performance

```bash
# Enable parallel builds
./gradlew build --parallel --max-workers=8

# Use build cache
./gradlew build --build-cache

# Generate build report
./gradlew build --profile
```

## Tips

1. **Use the Gradle Wrapper**: Always use `./gradlew` instead of `gradle`
2. **Tab Completion**: Type `./gradlew` and press Tab twice to see available tasks
3. **Task Names**: Use camelCase abbreviations (e.g., `cKD` for `compileKotlinDesktop`)
4. **Dry Run**: Add `--dry-run` to see what would execute without running
5. **Help**: Run `./gradlew help --task <task-name>` for task details

---

Last updated: 2025-12-28
