# Code Review Guidelines

Comprehensive guide for reviewing code changes in the ComposeWebView project.

## Review Principles

### 1. Constructive Feedback
- Be kind and respectful
- Explain the "why" behind suggestions
- Offer alternatives when pointing out issues
- Recognize good code

### 2. Focus on Impact
- Prioritize:
  1. **Critical**: Security, correctness, breaking changes
  2. **Important**: Performance, architecture, maintainability
  3. **Nice-to-have**: Style, naming, comments

### 3. Consider Context
- Understand the PR's purpose
- Check related issues/discussions
- Consider backward compatibility
- Evaluate complexity vs. benefit

## Code Quality Checks

### Formatting

✅ **Required**: Code must pass Spotless

```bash
./gradlew spotlessCheck
```

**Common issues**:
- Inconsistent indentation → Auto-fixed by Spotless
- Missing blank lines → Auto-fixed by Spotless
- Import order → Auto-fixed by Spotless

**Action**: Run `./gradlew spotlessApply` before committing

### Naming Conventions

✅ **Required**: Follow Kotlin conventions

- **Classes**: `PascalCase`
  ```kotlin
  class WebViewState { }
  ```

- **Functions/Properties**: `camelCase`
  ```kotlin
  fun loadUrl(url: String) { }
  val currentUrl: String
  ```

- **Constants**: `UPPER_SNAKE_CASE`
  ```kotlin
  const val DEFAULT_TIMEOUT = 30_000
  ```

- **Internal APIs**: Prefix or suffix with `Internal`
  ```kotlin
  internal class WebViewInternal { }
  ```

### Code Structure

✅ **Good practices**:
- Single Responsibility Principle
- Keep functions small (<50 lines)
- Avoid deep nesting (max 3-4 levels)
- Group related code together

❌ **Anti-patterns**:
- God classes (>500 lines)
- Deeply nested if/when statements
- Mixing concerns (UI + business logic)
- Hardcoded values (use constants)

## Multiplatform Review

### Expect/Actual Completeness

✅ **Required**: All platforms implemented

Check with:
```bash
bash .agent/skills/code-review/scripts/check_expect_actual.sh
```

**Review checklist**:
- [ ] `expect` declaration in `commonMain`
- [ ] `actual` implementation in `androidMain`
- [ ] `actual` implementation in `iosMain`
- [ ] `actual` implementation in `desktopMain`
- [ ] `actual` implementation in `jsMain`
- [ ] Signatures match exactly
- [ ] Platform constraints documented

### Common Pitfalls

#### 1. Platform-Specific Code in Common
❌ **Wrong**:
```kotlin
// In commonMain
fun doSomething() {
    android.util.Log.d("TAG", "message") // Android-specific!
}
```

✅ **Right**:
```kotlin
// In commonMain
expect fun logDebug(tag: String, message: String)

// In androidMain
actual fun logDebug(tag: String, message: String) {
    android.util.Log.d(tag, message)
}
```

#### 2. Inconsistent Behavior Across Platforms
❌ **Wrong**: Platform implementations behave differently

✅ **Right**: Consistent behavior, document unavoidable differences

```kotlin
/**
 * Enables zoom control.
 *
 * Note: iOS has limited zoom control due to WKWebView constraints.
 */
expect fun enableZoom(enabled: Boolean)
```

#### 3. Missing Platform Constraints
❌ **Wrong**: No documentation of limitations

✅ **Right**:
```kotlin
/**
 * Desktop implementation note: CEF initialization is asynchronous.
 * Ensure CEF is initialized before calling this method.
 */
actual fun loadUrl(url: String) { }
```

## Documentation Review

### KDoc Coverage

✅ **Required**: All public APIs must have KDoc

Check with:
```bash
bash .agent/skills/code-review/scripts/verify_kdoc.sh
```

**Minimum KDoc**:
```kotlin
/**
 * Brief description (one sentence).
 */
fun simpleFunction()
```

**Complete KDoc**:
```kotlin
/**
 * Loads a URL in the WebView.
 *
 * Detailed explanation if needed. Can span multiple
 * paragraphs to explain complex behavior.
 *
 * @param url The URL to load (must be valid HTTP/HTTPS)
 * @param headers Optional HTTP headers
 * @return True if load started successfully
 * @throws IllegalStateException if WebView is not initialized
 *
 * Example:
 * ```kotlin
 * val success = loadUrl("https://example.com")
 * ```
 */
fun loadUrl(url: String, headers: Map<String, String> = emptyMap()): Boolean
```

### Documentation Standards

✅ **Good documentation**:
- Explains the "what" and "why"
- Includes examples for non-trivial APIs
- Documents exceptions and edge cases
- Notes platform differences

❌ **Poor documentation**:
- Restates the function name
- Missing @param/@return
- Uses "TODO" placeholders
- Outdated information

## Testing Review

### Test Coverage

✅ **Required**: Tests for new features

**Minimum coverage**:
- Common logic in `commonTest`
- Platform-specific features in platform tests
- Critical paths (happy path + error cases)

**Review questions**:
- [ ] Are there tests?
- [ ] Do tests cover the main use cases?
- [ ] Are edge cases tested?
- [ ] Do tests have clear names?
- [ ] Are test assertions meaningful?

### Test Quality

✅ **Good tests**:
```kotlin
@Test
fun `loadUrl updates state when loading starts`() {
    // Arrange
    val state = WebViewState()

    // Act
    state.loadingState = LoadingState.Loading

    // Assert
    assertEquals(LoadingState.Loading, state.loadingState)
}
```

❌ **Poor tests**:
- Unclear test names
- Missing assertions
- Testing implementation details
- Fragile (breaks on refactoring)

## Performance Review

### Common Performance Issues

1. **Unnecessary recomposition**
   - Use `remember { }` for expensive calculations
   - Stable state holders with `@Stable` annotation

2. **Memory leaks**
   - Properly dispose resources in `DisposableEffect`
   - Clear listeners/callbacks when done

3. **Blocking operations**
   - No blocking calls on main thread
   - Use coroutines for async work

### Code Smells

❌ **Watch for**:
- Synchronous network calls
- Large allocations in loops
- Unnecessary object creation
- Missing `remember` in Composables

## Architecture Review

### Pattern Compliance

Verify adherence to established patterns:

**State Management**:
```kotlin
// ✅ Use WebViewState for reactive state
val state = rememberWebViewState()

// ❌ Don't use mutableStateOf directly
var url by mutableStateOf("")
```

**Controller Pattern**:
```kotlin
// ✅ Use WebViewController for actions
val controller = rememberWebViewController(state)
controller.loadUrl("https://example.com")

// ❌ Don't expose platform WebView directly
val webView: AndroidWebView // Don't do this
```

### Breaking Changes

⚠️  **Extra scrutiny required for**:
- Public API changes
- Signature modifications
- Behavior changes
- Deprecations

**Checklist**:
- [ ] Is the change necessary?
- [ ] Can it be done non-breaking?
- [ ] Is migration path provided?
- [ ] Is it documented in changelog?

## Security Review

### Common Security Issues

1. **JavaScript Execution**
   - Validate input before `evaluateJavascript`
   - Sanitize user-provided JavaScript

2. **URL Loading**
   - Validate URLs before loading
   - Consider App Transport Security (iOS)
   - Handle file:// URLs carefully

3. **Data Exposure**
   - Don't log sensitive data
   - Secure cookie handling
   - Validate JSBridge inputs

### Security Checklist

- [ ] No hardcoded secrets
- [ ] Input validation for external data
- [ ] Safe JavaScript execution
- [ ] Secure WebView configuration
- [ ] No unintended data exposure

## Pull Request Size

### Ideal PR Size
- **Small**: < 200 lines (preferred)
- **Medium**: 200-500 lines
- **Large**: 500-1000 lines
- **Huge**: > 1000 lines (should be split)

**Large PRs should**:
- Have clear scope
- Be well-documented
- Include comprehensive tests
- Be split if possible

## Review Workflow

### 1. Initial Review

- [ ] Read PR description
- [ ] Understand the context
- [ ] Check linked issues
- [ ] Review commit history

### 2. Code Review

- [ ] Run automated checks
- [ ] Review code changes
- [ ] Check tests
- [ ] Verify documentation

### 3. Testing

- [ ] Check out the branch
- [ ] Run tests locally
- [ ] Test manually if UI changes

### 4. Feedback

- [ ] Provide constructive feedback
- [ ] Categorize comments (critical/important/nice-to-have)
- [ ] Approve or request changes

## Common Review Comments

### Formatting
> "Please run `./gradlew spotlessApply` to fix formatting"

### Missing Tests
> "Could you add tests for this new functionality? Specifically, testing [use case]"

### Documentation
> "This public API needs KDoc. Please document parameters and return value"

### Platform Coverage
> "Missing actual implementation for iOS. Please add in `iosMain/`"

### Breaking Change
> "This changes the public API. Consider deprecating the old method instead of removing it"

### Performance
> "This expensive operation should be wrapped in `remember { }` to avoid recomputation"

## Approval Criteria

✅ **Ready to approve when**:
- All checks pass (formatting, tests, expect/actual)
- Code quality meets standards
- Documentation is adequate
- Tests cover new functionality
- No unresolved critical issues

❌ **Request changes when**:
- Critical bugs present
- Tests failing
- Missing platform implementations
- Breaking changes without justification
- Security concerns

---

Last updated: 2025-12-28
