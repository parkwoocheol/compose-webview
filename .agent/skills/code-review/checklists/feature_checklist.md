# Feature Implementation Checklist

Use this checklist when implementing new features in ComposeWebView.

## Planning

- [ ] Feature clearly defined in issue/spec
- [ ] Discussed with maintainers (if significant)
- [ ] Breaking changes justified and documented
- [ ] Migration path planned (if breaking)

## Multiplatform Implementation

### Common (Required)

- [ ] Feature defined in `commonMain`
- [ ] Used `expect` for platform-specific parts
- [ ] Documented expected behavior
- [ ] Added to appropriate interfaces/classes

### Android (Required)

- [ ] `actual` implementation in `androidMain`
- [ ] Uses `android.webkit.WebView` APIs appropriately
- [ ] Handles Android-specific constraints
- [ ] Tested on Android API 24+

### iOS (Required)

- [ ] `actual` implementation in `iosMain`
- [ ] Uses `WKWebView` APIs appropriately
- [ ] Handles iOS-specific constraints (e.g., zoom limitations)
- [ ] Tested on iOS 14.0+

### Desktop (Required)

- [ ] `actual` implementation in `desktopMain`
- [ ] Uses CEF/KCEF APIs appropriately
- [ ] Handles async CEF initialization
- [ ] Tested on JVM 11+

### Web (Required)

- [ ] `actual` implementation in `jsMain`
- [ ] Uses appropriate web APIs
- [ ] Handles browser limitations
- [ ] Tested in modern browsers

## Code Quality

### Formatting & Style

- [ ] Code formatted with Spotless: `./gradlew spotlessApply`
- [ ] Follows Kotlin naming conventions
- [ ] No compiler warnings
- [ ] Proper visibility modifiers (public/internal/private)

### Architecture

- [ ] Follows established patterns (see `common_patterns.md`)
- [ ] Uses `WebViewState` for reactive state
- [ ] Uses `WebViewController` for actions
- [ ] No platform-specific code in common
- [ ] Properly abstracted platform differences

### Code Structure

- [ ] Functions are focused and small (<50 lines preferred)
- [ ] Classes have single responsibility
- [ ] No deep nesting (max 3-4 levels)
- [ ] Constants extracted (no magic numbers/strings)

## Documentation

### KDoc

- [ ] All public APIs have KDoc comments
- [ ] KDoc includes:
  - [ ] Brief description
  - [ ] `@param` for all parameters
  - [ ] `@return` for return values
  - [ ] `@throws` for exceptions
  - [ ] Usage examples for non-trivial APIs

### Platform Notes

- [ ] Platform differences documented in KDoc
- [ ] Limitations clearly stated
- [ ] Known issues mentioned

### User Documentation

- [ ] Added/updated guide in `docs/guides/` (if needed)
- [ ] Added/updated API reference in `docs/api/` (if needed)
- [ ] Updated `README.md` (if public-facing feature)
- [ ] Added examples showing usage

## Testing

### Unit Tests

- [ ] Tests added in `commonTest` for common logic
- [ ] Platform-specific tests in platform test sources
- [ ] Tests cover:
  - [ ] Happy path
  - [ ] Error cases
  - [ ] Edge cases
  - [ ] Null/empty inputs (where applicable)

### Test Quality

- [ ] Test names are descriptive (e.g., `` `loadUrl updates state when loading starts` ``)
- [ ] Tests are isolated (no dependencies between tests)
- [ ] Assertions are meaningful
- [ ] No flaky tests

### Manual Testing

- [ ] Tested on Android device/emulator
- [ ] Tested on iOS simulator/device
- [ ] Tested on Desktop (JVM)
- [ ] Tested in browser (Web)
- [ ] Verified with sample app

## Security

- [ ] No hardcoded credentials or secrets
- [ ] Input validation for external data
- [ ] Safe JavaScript execution (if applicable)
- [ ] No unintended data exposure
- [ ] Secure WebView configuration

## Performance

- [ ] No blocking operations on main thread
- [ ] Expensive operations wrapped in `remember { }`
- [ ] Proper cleanup in `DisposableEffect`
- [ ] No memory leaks (listeners, references)
- [ ] Tested performance impact (if significant feature)

## Backward Compatibility

- [ ] No breaking changes to public API (or justified)
- [ ] Deprecation warnings added (if replacing API)
- [ ] Migration guide provided (if breaking)
- [ ] Versioning considered (if needed)

## Pre-Commit Checks

- [ ] Run Spotless: `./gradlew spotlessApply`
- [ ] Run tests: `bash .agent/skills/development/scripts/test_all.sh`
- [ ] Check expect/actual: `bash .agent/skills/code-review/scripts/check_expect_actual.sh`
- [ ] Verify KDoc: `bash .agent/skills/code-review/scripts/verify_kdoc.sh`
- [ ] Run full review: `bash .agent/skills/code-review/scripts/review_checklist.sh`

## Build Verification

- [ ] Clean build succeeds: `./gradlew clean build`
- [ ] All platforms compile: `bash .agent/skills/development/scripts/build_all.sh`
- [ ] No new lint warnings: `./gradlew lint`
- [ ] Sample app builds and runs

## Before Creating PR

- [ ] Commits are atomic and have clear messages
- [ ] Branch is up-to-date with main
- [ ] Feature complete and tested
- [ ] Documentation complete
- [ ] All checklists above completed

## PR Description

- [ ] Clear title describing the change
- [ ] Description explains the "why"
- [ ] Links to related issues
- [ ] Screenshots/videos for UI changes
- [ ] Notes any breaking changes
- [ ] Lists testing performed

---

## Quick Command Reference

```bash
# Format code
./gradlew spotlessApply

# Run all tests
bash .agent/skills/development/scripts/test_all.sh

# Check expect/actual
bash .agent/skills/code-review/scripts/check_expect_actual.sh

# Full review
bash .agent/skills/code-review/scripts/review_checklist.sh

# Build all platforms
bash .agent/skills/development/scripts/build_all.sh
```

---

*Complete this checklist before submitting your PR to ensure high quality and consistency.*
