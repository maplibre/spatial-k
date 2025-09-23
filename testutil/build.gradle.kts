@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    // disabled until tests are fixed
    //wasmJs {
    //    browser()
    //    nodejs()
    //    d8()
    //}
    //
    //wasmWasi {
    //    nodejs()
    //}

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
    // disabled until tests are fixed
    //androidNativeArm32()
    //androidNativeArm64()
    //androidNativeX86()
    //androidNativeX64()
    //watchosDeviceArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.io.core)
            api(libs.kotlinx.serialization)
        }

    }
}
