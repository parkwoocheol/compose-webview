plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":sample:shared"))
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.parkwoocheol.sample.composewebview.MainKt"
        jvmArgs +=
            listOf(
                "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.lwawt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
            )
    }
}
