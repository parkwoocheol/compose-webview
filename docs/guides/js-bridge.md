# JavaScript Bridge

One of the most powerful features of `compose-webview` is its **Promise-based JavaScript Bridge**. It simplifies communication between your Kotlin code and the JavaScript running inside the WebView.

!!! info "Mobile First"
    The JSBridge is highly optimized for **Android** and **iOS**, providing a unified API. Support for Web and Desktop is currently experimental.

---

## Concept

Traditional `JavascriptInterface` (Android) or `WKScriptMessageHandler` (iOS) often leads to callback hell or requires complex JSON parsing manually.

Our bridge solves this by:

1. **Promises**: All calls from JS return a Promise, allowing `await` syntax.
2. **Serialization**: Automatic JSON conversion for arguments and return values.
3. **Type Safety**: Define your data models in Kotlin once.

---

## Setup

### 1. Define Data Models

Use `@Serializable` from `kotlinx.serialization` to define the data structures you want to pass.

```kotlin
@Serializable
data class User(val id: String, val name: String)

@Serializable
data class DeviceInfo(val model: String, val osVersion: String)
```

### 2. Create the Bridge

Create a bridge instance using `rememberWebViewJsBridge`. You can optionally customize the JavaScript object name (default is `window.AppBridge`).

```kotlin
val bridge = rememberWebViewJsBridge(
    jsObjectName = "MyApp" // JS will access via window.MyApp
)
```

---

## Receiving Calls from JavaScript

Use the `register` function to define handlers for JavaScript calls.

### Typed Handling (Recommended)

Specify the input type `T` and output type `R`. The library handles JSON parsing automatically.

```kotlin
LaunchedEffect(bridge) {
    // JS: await window.MyApp.call('getUser', { id: '123' })
    bridge.register<UserRequest, User>("getUser") { request ->
        // This block runs in a coroutine
        val user = userRepository.findById(request.id)
        user // Returned value is sent back to JS
    }
}
```

### No Return Value

If your handler doesn't return anything (void), use `Unit` as the return type.

```kotlin
// JS: await window.MyApp.call('log', 'Hello')
bridge.register<String, Unit>("log") { message ->
    Log.d("WebView", "JS Log: $message")
}
```

---

## Calling JavaScript from Kotlin

You can also send events or execute arbitrary JavaScript.

### Emitting Events

You can "emit" events that JavaScript can listen to.

**Kotlin:**

```kotlin
bridge.emit("onNetworkStatusChange", NetworkStatus(online = true))
```

**JavaScript:**

```javascript
window.MyApp.on('onNetworkStatusChange', (status) => {
    console.log("Is Online:", status.online);
});
```

### Evaluating JavaScript

Execute raw JavaScript code.

```kotlin
controller.evaluateJavascript("alert('Hello from Kotlin!')") { result ->
    println("Result: $result")
}
```

---

## JavaScript API Reference

The bridge injects a global object (default `AppBridge`) into the WebView.

### `call(handlerName, data)`

Calls a native handler registered in Kotlin. Returns a `Promise`.

```javascript
try {
    const result = await window.AppBridge.call('getUser', { id: 1 });
    console.log(result);
} catch (error) {
    console.error("Native error:", error);
}
```

### `on(eventName, callback)`

Subscribes to an event emitted from Kotlin.

```javascript
const unsubscribe = window.AppBridge.on('eventName', (data) => {
    // Handle event
});

// Later...
unsubscribe();
```

---

## Custom JSON Serializer

By default, the library uses `kotlinx.serialization`. If you prefer **Gson**, **Moshi**, or **Jackson**, you can implement the `BridgeSerializer` interface.

```kotlin
class GsonSerializer(val gson: Gson) : BridgeSerializer {
    override fun <T> encode(value: T, clazz: Class<T>): String = gson.toJson(value)
    override fun <T> decode(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)
}

// Usage
val bridge = rememberWebViewJsBridge(serializer = GsonSerializer(Gson()))
```
