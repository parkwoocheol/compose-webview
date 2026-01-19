# Contributing to ComposeWebView

Hey! Thanks for wanting to contribute 👋

Don't worry about getting everything perfect. We're here to help!

## Getting Started

### What You'll Need

- JDK 11+
- Android Studio
- Git

### Setup

1. Fork and clone the repo
   ```bash
   git clone https://github.com/YOUR_USERNAME/compose-webview.git
   cd compose-webview
   ```

2. Open in Android Studio and run the sample app to make sure everything works

### WASM Target (Optional)

The WASM target is **enabled by default** for local development but **disabled on JitPack** due to GLIBC compatibility issues.

**To disable WASM locally** (if you encounter Node.js or build issues):
```bash
./gradlew build -PENABLE_WASM=false
```

**To explicitly enable WASM**:
```bash
./gradlew build -PENABLE_WASM=true
```

You can also add this to your `gradle.properties` to persist the setting:
```properties
ENABLE_WASM=false
```

## How to Contribute

### Found a Bug?

Just open an issue with:
- What happened vs what you expected
- Steps to reproduce
- Your environment (Android version, library version)

### Want to Add a Feature?

Cool! Open an issue first so we can discuss it.

### Making Changes

1. Create a branch (name it whatever makes sense)
   ```bash
   git checkout -b your-branch-name
   ```

2. Make your changes

3. Test it with the sample app
   - Android: `./gradlew :sample:androidApp:installDebug`
   - Desktop: `./gradlew :sample:desktopApp:run`
   - WASM: `./gradlew :sample:wasmApp:wasmJsBrowserDevelopmentRun`
   - If WASM build fails due to Node.js issues, you can disable it: `./gradlew build -PENABLE_WASM=false`

4. Push and open a PR

## Code Style

Just follow standard Kotlin conventions:
- 4 spaces for indentation
- Use meaningful names
- Add comments for complex stuff
- Public APIs should have KDoc

If you use Android Studio's default formatter, you're probably fine.

## Commit Messages

Try to use conventional commits when you can:
- `feat: add something`
- `fix: fix something`
- `docs: update docs`

But honestly, as long as your commit message is clear, we're good.

## Pull Requests

When opening a PR:
- Describe what you changed and why
- Link related issues if any
- Update docs if you changed public APIs

We'll review it and might ask for some changes. No biggie!

## Questions?

Just ask! Open an issue or discussion if you need help.

Thanks for contributing! 🎉