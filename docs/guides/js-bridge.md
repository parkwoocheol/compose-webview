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

Create a bridge instance using `rememberWebViewJsBridge`. This is the default cross-platform bridge and is the right
starting point when you want one API across Android, iOS, Desktop, JS, and WASM. You can optionally customize the
JavaScript object name (default is `window.AppBridge`).

```kotlin
val bridge = rememberWebViewJsBridge(
    jsObjectName = "MyApp" // JS will access via window.MyApp
)
```

For Android web content served from origins you control, prefer the Android-only origin-aware bridge from your
`androidMain` source set:

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

Pass the bridge to the state-aware `ComposeWebView` overload:

```kotlin
ComposeWebView(
    state = state,
    jsBridge = bridge,
)
```

### Choosing a Bridge

| Use case | API | Notes |
| :--- | :--- | :--- |
| Cross-platform typed calls and events | `rememberWebViewJsBridge()` | Default for shared code. On Android it keeps the compatibility bridge path. |
| Android typed calls with origin and frame metadata | `rememberAndroidWebViewJsBridge(..., policy = OriginAwareOnly)` | Uses `addWebMessageListener` with `allowedOriginRules`. Prefer this for trusted Android web content. |
| Native-to-JavaScript main-frame messages on Android | `postMainFrameMessage(...)` | Android-only experimental API. Maps to `postWebMessage`. |
| Long-lived bidirectional main-frame channel on Android | `openMainFrameSession(...)` | Android-only experimental API. Maps to `WebMessageChannel` plus `postWebMessage`. |
| Provider or page fallback that cannot use the origin-aware path | `policy = Compatible` | Re-enables the classic Android bridge fallback. Use only when you accept the weaker trust model. |

For Android bridge security background, see Google's guidance on
[native bridge risks](https://developer.android.com/privacy-and-security/risks/insecure-webview-native-bridges) and
[JSBridge message APIs](https://developer.android.com/develop/ui/views/layout/webapps/native-api-access-jsbridge).

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

### Android Raw Message APIs

`rememberAndroidWebViewJsBridge(...)` also unlocks experimental Android-only APIs for raw string/binary messages and
main-frame `WebMessageChannel` sessions.

Use these APIs when you need Android WebView message primitives directly:

- `registerMessage(...)`: receives raw string or binary messages posted through the origin-aware listener bridge.
- `postMainFrameMessage(...)`: sends one message from Kotlin to the main frame using Android `postWebMessage`.
- `openMainFrameSession(...)`: opens a long-lived main-frame `WebMessageChannel` session.

`postMainFrameMessage(...)` is main-frame focused. Use an exact `targetOrigin` such as `https://example.com`; wildcard
target origins are intentionally rejected.

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

For native-to-JavaScript delivery, register the JavaScript listener early in the page lifecycle. Calling from Kotlin
after `LoadingState.Finished` is a reasonable minimum, but the page still needs to have installed its listener:

```kotlin
@OptIn(ExperimentalComposeWebViewApi::class)
LaunchedEffect(state.loadingState, bridge.capabilities) {
    if (
        state.loadingState == LoadingState.Finished &&
        bridge.capabilities.supportsMainFrameOutboundMessaging
    ) {
        bridge.postMainFrameMessage(
            targetOrigin = "https://example.com",
            message = AndroidBridgeMessage.Text("native-ready"),
        )
    }
}
```

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

You can also receive `postMainFrameMessage(...)` with a regular browser `message` listener:

```javascript
window.addEventListener('message', (event) => {
  console.log('Native message:', event.data);
});
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
