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

For Android web content served from origins listed in `allowedOriginRules`, prefer the origin-aware Android bridge:

```kotlin
val bridge =
    rememberAndroidWebViewJsBridge(
        config =
            AndroidWebViewJsBridgeConfig(
                allowedOriginRules = setOf("https://example.com"),
                policy = AndroidJsBridgePolicy.OriginAwareOnly,
            ),
    )
```

Notes:
- `rememberWebViewJsBridge()` remains the default cross-platform bridge.
- On Android, the default bridge is compatibility-oriented and preserves the classic `addJavascriptInterface` flow.
- `rememberAndroidWebViewJsBridge()` adds `allowedOriginRules`-backed listener bootstrap and optional compatibility
  fallback.
- `policy = Compatible` is a migration/fallback mode. It intentionally restores `addJavascriptInterface`-style
  exposure for pages that do not receive the origin-aware bridge.

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

### No-Argument Handler

For no-argument calls, you can use either `register<Unit, R>` or `register<R>`.

```kotlin
bridge.register<Unit, Unit>("refreshSession") {
    Log.d("WebView", "Refreshing session...")
}

bridge.register<Unit>("refreshSessionAlt") {
    Log.d("WebView", "Refreshing session...")
}
```

Both JavaScript forms are valid:

```javascript
await window.AppBridge.call('refreshSession');
await window.AppBridge.call('refreshSession', null);
```

`register<T, R>` null input rules:
- `T` is `Unit`: `null` is accepted.
- Other non-null input types: `null` throws an input type error.

### Nullable Payload Handler

Use `registerNullable<T, R>` when JavaScript may send `null` for a typed payload.

```kotlin
bridge.registerNullable<UserRequest, User>("getUserMaybe") { requestOrNull ->
    if (requestOrNull == null) {
        User(id = "0", name = "Guest")
    } else {
        userRepository.findById(requestOrNull.id)
    }
}
```

### Invocation Metadata

Use `registerWithContext<T, R>` when you need source metadata from the calling frame.

```kotlin
bridge.registerWithContext<UserRequest, User>("getTrustedUser") { request ->
    check(isMainFrame)
    check(sourceOrigin == "https://example.com")
    userRepository.findById(request.id)
}
```

### Suspend / Async Handlers

All `register` and `registerNullable` methods accept **both regular and `suspend` lambdas**. This enables handlers that perform asynchronous operations (e.g., showing a dialog and waiting for user input, making a network call) before returning a result to JavaScript.

```kotlin
// Suspend lambda — the JS Promise resolves only after the async work completes
bridge.register<Unit, UserChoice>("showConfirmDialog") {
    // Show a dialog and suspend until user responds
    val result = dialogManager.showConfirm("Are you sure?")
    UserChoice(confirmed = result)
}

// Network call
bridge.register<SearchQuery, SearchResult>("search") { query ->
    val results = api.search(query.term)   // suspend call
    SearchResult(items = results)
}
```

JavaScript callers don't need any changes — calls already return Promises:

```javascript
const choice = await window.AppBridge.call('showConfirmDialog');
console.log("User confirmed:", choice.confirmed);
```

### Android Experimental Message APIs

`rememberAndroidWebViewJsBridge(...)` also unlocks experimental Android-only message APIs for raw string/binary
messages and main-frame `WebMessageChannel` sessions.

```kotlin
@OptIn(ExperimentalComposeWebViewApi::class)
LaunchedEffect(bridge) {
    bridge.registerMessage("echoText") { message ->
        when (message) {
            is AndroidBridgeMessage.Text -> AndroidBridgeMessage.Text("echo:${message.value}")
            is AndroidBridgeMessage.Binary -> AndroidBridgeMessage.Binary(message.value)
        }
    }

    bridge.postMainFrameMessage(
        targetOrigin = "https://example.com",
        message = AndroidBridgeMessage.Text("native-ready"),
    )

    val session = bridge.openMainFrameSession("https://example.com")
    session?.setMessageHandler { payload ->
        println("Session payload: $payload")
    }
}
```

These APIs map directly to Android `postWebMessage` and `WebMessageChannel`. They are useful transport primitives,
but they do not replace the inbound origin metadata and trust model provided by `addWebMessageListener`.

JavaScript helpers added by the origin-aware Android bridge:

```javascript
window.AppBridge.onMessage((payload) => {
  console.log('Native message:', payload);
});

window.AppBridge.onSession((session) => {
  session.onMessage((payload) => console.log('Session payload:', payload));
  session.postMessage('hello from JS session');
});

const echoed = await window.AppBridge.callMessage('echoText', 'hello');
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
const onEvent = (data) => {
    // Handle event
};

window.AppBridge.on('eventName', onEvent);

// Later
window.AppBridge.off('eventName', onEvent);
```

---

## Custom JSON Serializer

By default, the library uses `kotlinx.serialization`. If you prefer **Gson**, **Moshi**, or **Jackson**, you can implement the `BridgeSerializer` interface.

```kotlin
import kotlin.reflect.KType

class CustomSerializer : BridgeSerializer {
    override fun encode(data: Any?, type: KType): String =
        TODO("Serialize data using your JSON library and KType")

    override fun <T> decode(json: String, type: KType): T =
        TODO("Deserialize json using your JSON library and KType")
}

// Usage
val bridge = rememberWebViewJsBridge(serializer = CustomSerializer())
```
