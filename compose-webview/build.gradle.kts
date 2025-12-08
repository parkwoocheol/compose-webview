plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
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
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        
        iosMain.dependencies {
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kcef)
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }
        
        val wasmJsMain by getting {
            dependencies {
                // implementation(compose.html.core) // Not supported for Wasm yet
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
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Publishing configuration for KMP is handled by the plugin, 
// but we can configure the POM details here if needed.
// For now, disabling the manual publication to avoid conflicts.
/*
publishing {
    publications {
        register<MavenPublication>("release") {
            // ...
        }
    }
}
*/