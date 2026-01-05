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
include(":sample:wasmApp")
