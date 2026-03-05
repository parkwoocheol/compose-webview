# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.8.0] - 2026-03-05

### Added
- JSBridge `register` and `registerNullable` now accept `suspend` lambdas, enabling asynchronous handlers (e.g., showing dialogs, making network calls) that return results to JavaScript after completion. Both regular and suspend lambdas work transparently. (#47, #49)

### Fixed
- iOS: replaced deprecated `UIApplication.openURL(_:)` with `UIApplication.open(_:options:completionHandler:)`, fixing `mailto:`, `tel:`, and other external scheme links failing silently on iOS 10+. (#48)

## [1.7.1] - 2026-02-19

### Fixed
- Web(JS): resolved `ClassCastException` by replacing Compose HTML `Iframe` rendering with an imperative DOM overlay implementation.

### Changed
- Web(JS): removed `compose-html` dependency from `jsMain` to align with the imperative DOM overlay runtime model.
- Documentation: updated README and docs index to align with the new JS runtime model.

## [1.7.0] - 2026-02-13

### Changed
- Desktop internals migrated from KCEF-based integration to direct JCEF runtime integration.
- Desktop CEF initialization and client wiring hardened while preserving existing public Desktop APIs.

## [1.6.0] - 2026-01-19

### Added
- Android/iOS network request interception via `WebViewSettings.interceptedSchemes`.
- Android/iOS dark mode controls (`DARK`, `LIGHT`, `AUTO`) with system theme synchronization support.
- Enhanced cookie management APIs, including URL-scoped cookie removal on Android/iOS.
- iOS find-on-page parity improvements with `onFindResultReceived`.
- Android custom context menu action mode callback support.
- WASM target support for Compose Multiplatform.

### Changed
- Publishing strategy expanded with Maven Central support and complete multiplatform artifacts.
- Build configuration updated to conditionally disable WASM targets in JitPack environments.
- Dependency resolution and CI workflow tuning for faster and more reliable builds.

### Fixed
- iOS 14+ compatibility by removing deprecated `WKPreferences.javaScriptEnabled` usage.
- iOS find API integration issues (`WKFindResult` property access alignment).
- Cross-platform build issues across JS, WasmJs, Desktop, iOS, and shared API synchronization.

[Unreleased]: https://github.com/parkwoocheol/compose-webview/compare/1.8.0...main
[1.8.0]: https://github.com/parkwoocheol/compose-webview/compare/1.7.1...1.8.0
[1.7.1]: https://github.com/parkwoocheol/compose-webview/compare/1.7.0...1.7.1
[1.7.0]: https://github.com/parkwoocheol/compose-webview/compare/1.6.0...1.7.0
[1.6.0]: https://github.com/parkwoocheol/compose-webview/compare/1.5.0...1.6.0
