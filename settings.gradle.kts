pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "compose-webview"
include(":compose-webview")
include(":sample:shared")
include(":sample:androidApp")
include(":sample:desktopApp")

// Only include wasmApp if WASM is enabled (not on JitPack by default)
val enableWasm =
    providers.gradleProperty("ENABLE_WASM")
        .orElse(providers.environmentVariable("JITPACK").map { "false" })
        .orElse("true")
        .get()
        .toBoolean()

if (enableWasm) {
    include(":sample:wasmApp")
}
