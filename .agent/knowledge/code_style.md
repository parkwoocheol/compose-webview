# Code Style & Conventions

## Naming Rules

### Basic Conventions

- **Class/Interface**: PascalCase (e.g., `WebViewState`, `ComposeWebView`)
- **Function/Variable**: camelCase (e.g., `loadUrl`, `currentState`)
- **Composable**: PascalCase (even for functions). Filename should match component name
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)

### Platform Implementation

- **commonMain**: `expect class/fun Name`
- **platformMain**: `actual typealias Name = PlatformClass` or `actual class Name`
- **Internal Implementation**: Use `internal` visibility (e.g., `ComposeWebViewImpl`)

## Formatting (REQUIRED)

### Running Spotless

**MUST** run Spotless before every commit:

```bash
./gradlew spotlessApply
```

### Basic Rules

- **Indentation**: 4 spaces
- **Import Optimization**: Handled by Spotless, but manually remove unused imports if needed
- **Max Line Length**: 120 characters (ktlint default)

## Documentation (KDoc)

### Required Targets

- **public API**: All APIs with `public` keyword must have KDoc
- **Parameters/Returns**: Detailed description with `@param`, `@return`

### Example

```kotlin
/**
 * Loads a URL in the WebView.
 *
 * @param url The URL to load
 * @param additionalHttpHeaders Additional HTTP headers (Android/iOS only)
 */
fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>? = null)
```

## Kotlin 2.2.0 Related Styles

### Utilize Stable Features

- **Guard conditions**: Use early return pattern in `if` conditions
- **Multi-dollar interpolation**: For complex string interpolation
- **Non-local break/continue**: Loop control within lambdas

### Preview Features (Optional)

- **Context Parameters**: Consider for DI and DSL design (requires `@OptIn`)

## 2026 Best Practices

### Strictly Follow Null Safety

- **Avoid `!!`**: Never use except absolutely unavoidable cases
- **Prefer Safe Call `?.`**: Always use when null is possible
- **Elvis Operator `?:`**: Use for providing default values

```kotlin
// Bad
val length = text!!.length

// Good
val length = text?.length ?: 0
```

### Prioritize Immutability

- **`val` over `var`**: Use `val` whenever possible
- **immutable collections**: Prefer `listOf`, `setOf`, `mapOf`

```kotlin
// Bad
var items = mutableListOf<String>()

// Good (when not global state)
val items = listOf("item1", "item2")
```

### Utilize Data Classes

Use `data class` for model definitions:

```kotlin
@Serializable
data class WebViewError(
    val errorCode: Int,
    val description: String,
    val failingUrl: String?
)
```

Auto-generates: `equals()`, `hashCode()`, `toString()`, `copy()`

### Extension Functions

Use extension functions for clean modularization:

```kotlin
// util file
fun String.isValidUrl(): Boolean {
    return URLUtil.isValidUrl(this)
}

// usage
if (url.isValidUrl()) {
    loadUrl(url)
}
```

### Utilize `when` Expression

Use `when` for complex conditional processing:

```kotlin
when (loadingState) {
    is LoadingState.Idle -> showIdleUI()
    is LoadingState.Loading -> showProgress(loadingState.progress)
    is LoadingState.Finished -> hideProgress()
}
```

## Visibility Modifiers

- **`public`**: External API (default even without explicit declaration)
- **`internal`**: Internal implementation (accessible within module only)
- **`private`**: Encapsulated logic

## Function Size

- **Small, Focused Functions**: Follow Single Responsibility Principle
- **Complex Logic**: Properly separate into functions
