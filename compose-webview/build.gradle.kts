plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

group = "com.github.parkwoocheol"
version = providers.gradleProperty("COMPOSE_WEBVIEW_VERSION").orElse("0.0.0-SNAPSHOT").get()

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                }
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm("desktop")

    js(IR) {
        browser()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }

        getByName("androidInstrumentedTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.androidx.junit)
                implementation(libs.androidx.espresso.core)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.compose.ui.test.junit4)
            }
        }

        iosMain.dependencies {
            // No platform-specific dependencies needed for iOS
        }

        getByName("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kcef)
            }
        }

        getByName("jsMain") {
            dependencies {
                implementation(compose.html.core)
            }
        }

        getByName("wasmJsMain") {
            dependencies {
                // WASM uses Canvas-based Compose UI
                // HTML interop is limited - WebView uses manual DOM manipulation
            }
        }
    }
}

android {
    namespace = "com.parkwoocheol.composewebview"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("Compose WebView")
                description.set("A powerful and flexible WebView wrapper for Compose Multiplatform (Android, iOS, Desktop, Web).")
                url.set("https://github.com/parkwoocheol/compose-webview")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("parkwoocheol")
                        name.set("Woocheol Park")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/parkwoocheol/compose-webview.git")
                    developerConnection.set("scm:git:ssh://git@github.com/parkwoocheol/compose-webview.git")
                    url.set("https://github.com/parkwoocheol/compose-webview")
                }
            }
        }
    }

    repositories {
        val githubActor =
            providers.environmentVariable("GITHUB_ACTOR")
                .orElse(providers.provider { findProperty("gpr.user") as String? })
        val githubToken =
            providers.environmentVariable("GITHUB_TOKEN")
                .orElse(providers.provider { findProperty("gpr.key") as String? })

        if (githubActor.isPresent && githubToken.isPresent) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/parkwoocheol/compose-webview")
                credentials {
                    username = githubActor.get()
                    password = githubToken.get()
                }
            }
        }
    }
}
