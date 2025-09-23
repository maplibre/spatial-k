@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.resources)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    // TODO: blocked by the goncalossilva resources library used in tests
    // wasmJs {
    //     browser()
    //     nodejs()
    //     d8()
    // }
    //
    // wasmWasi {
    //     nodejs()
    // }

    // native tier 1
    macosArm64()
    iosSimulatorArm64()
    iosArm64()

    // native tier 2
    macosX64()
    iosX64()
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()

    // native tier 3
    mingwX64()
    // TODO: blocked by the goncalossilva resources library used in tests
    // androidNativeArm32()
    // androidNativeArm64()
    // androidNativeX86()
    // androidNativeX64()
    // watchosDeviceArm64()

    sourceSets {
        all {
            with(languageSettings) {
                optIn("kotlin.RequiresOptIn")
            }
        }

        commonMain.dependencies {
            api(project(":geojson"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.resources)
        }
    }
}

tasks.named("jsBrowserTest") { enabled = false }

dokka {
    dokkaSourceSets {
        configureEach {
            includes.from("MODULE.md")
        }
    }
}
