# [API Name]

Brief description of what this API provides (1-2 sentences).

## Overview

Detailed explanation of the API's purpose, when to use it, and key concepts (2-3 paragraphs).

## Main APIs

### `FunctionOrClassName`

Brief description of this function/class.

**Signature**:
```kotlin
@Composable
fun FunctionName(
    parameter1: Type1,
    parameter2: Type2 = DefaultValue,
    modifier: Modifier = Modifier
): ReturnType
```

**Parameters**:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `parameter1` | `Type1` | Required | Description of parameter 1 |
| `parameter2` | `Type2` | `DefaultValue` | Description of parameter 2 |
| `modifier` | `Modifier` | `Modifier` | Modifier to be applied |

**Returns**: `ReturnType` - Description of what is returned

**Example**:
```kotlin
val result = FunctionName(
    parameter1 = value1,
    parameter2 = value2
)
```

**See also**:
- [Related API](#related-api)
- [Guide](../guides/related-guide.md)

---

### `AnotherFunction`

Description of another function/class in this API.

**Signature**:
```kotlin
fun anotherFunction(param: String): Int
```

**Parameters**:
- `param: String` - Description

**Returns**: `Int` - Description

**Throws**:
- `IllegalArgumentException` - When param is invalid
- `IllegalStateException` - When not initialized

**Example**:
```kotlin
try {
    val result = anotherFunction("value")
} catch (e: IllegalArgumentException) {
    // Handle error
}
```

## Properties

### `propertyName`

Description of this property.

**Type**: `PropertyType`

**Access**: Read-only / Mutable

**Example**:
```kotlin
val value = instance.propertyName
```

## Enums and Constants

### `EnumName`

Description of this enum.

**Values**:

| Value | Description |
|-------|-------------|
| `VALUE_1` | Description of VALUE_1 |
| `VALUE_2` | Description of VALUE_2 |
| `VALUE_3` | Description of VALUE_3 |

**Example**:
```kotlin
when (state) {
    EnumName.VALUE_1 -> { }
    EnumName.VALUE_2 -> { }
    EnumName.VALUE_3 -> { }
}
```

## Extension Functions

### `Type.extensionFunction()`

Description of extension function.

**Receiver**: `Type`

**Signature**:
```kotlin
fun Type.extensionFunction(param: String): Result
```

**Example**:
```kotlin
val result = instance.extensionFunction("value")
```

## Complete Example

Comprehensive example showing typical usage:

```kotlin
@Composable
fun ApiExample() {
    // Setup
    val state = remember { mutableStateOf(InitialValue) }

    // Usage
    FunctionName(
        parameter1 = state.value,
        parameter2 = "custom",
        modifier = Modifier.fillMaxSize()
    )

    // Handling results
    LaunchedEffect(state.value) {
        val result = anotherFunction(state.value.toString())
        // Process result
    }
}
```

## Platform Compatibility

| Platform | Status | Notes |
|----------|--------|-------|
| Android | ✅ Supported | Full support, API 24+ |
| iOS | ✅ Supported | iOS 14.0+ |
| Desktop | ⚠️ Experimental | JVM 11+ |
| Web | ⚠️ Experimental | Modern browsers |

### Platform-Specific Behavior

=== "Android"
    Description of Android-specific behavior or implementation details.

    ```kotlin
    // Android-specific usage example
    ```

=== "iOS"
    Description of iOS-specific behavior or implementation details.

    ```kotlin
    // iOS-specific usage example
    ```

=== "Desktop"
    Description of Desktop-specific behavior or implementation details.

=== "Web"
    Description of Web-specific behavior or implementation details.

## Common Patterns

### Pattern 1: [Pattern Name]

Description and use case.

```kotlin
// Example implementing this pattern
@Composable
fun PatternExample() {
    // Implementation
}
```

### Pattern 2: [Another Pattern]

Description and use case.

```kotlin
// Another pattern example
```

## Best Practices

!!! tip "Recommended Usage"
    - Best practice 1
    - Best practice 2
    - Best practice 3

!!! warning "Avoid"
    - Anti-pattern 1
    - Anti-pattern 2

## Migration Guide

### From Previous Version

If this API replaces or changes a previous API:

**Before** (v0.x):
```kotlin
// Old API usage
oldFunction(param)
```

**After** (v1.x):
```kotlin
// New API usage
newFunction(param)
```

**Migration steps**:
1. Step 1
2. Step 2
3. Step 3

## Related APIs

- [Related API 1](link)
- [Related API 2](link)
- [Related API 3](link)

## FAQ

### Question 1?

Answer 1.

### Question 2?

Answer 2.

## Troubleshooting

### Common Issue 1

**Problem**: Description

**Solution**: How to fix

### Common Issue 2

**Problem**: Description

**Solution**: How to fix

## See Also

- **Guides**: [Related Guide](../guides/guide-name.md)
- **Other APIs**: [Other API](other-api.md)
- **External**: [External Documentation](https://example.com)

---

*API Version: 1.0.0*
*Last updated: YYYY-MM-DD*
