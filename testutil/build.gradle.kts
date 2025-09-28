@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { alias(libs.plugins.kotlin.multiplatform) }

kotlin {
    applyDefaultHierarchyTemplate()
    compilerOptions { allWarningsAsErrors = true }

    jvm { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }

    js(IR) {
        browser()
        nodejs()
    }

    wasmJs {
        browser()
        nodejs()
        d8()
    }

    wasmWasi { nodejs() }

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
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    watchosDeviceArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.io.core)
            api(libs.kotlinx.serialization.json)
            implementation(project(":geojson"))
        }

        val commonMain by getting

        create("fsMain").apply {
            dependsOn(commonMain)
            jvmMain.get().dependsOn(this)
            linuxMain.get().dependsOn(this)
            mingwMain.get().dependsOn(this)
            macosMain.get().dependsOn(this)
        }

        create("bundleMain").apply {
            dependsOn(commonMain)
            jsMain.get().dependsOn(this)
            wasmJsMain.get().dependsOn(this)
        }

        create("todoMain").apply {
            dependsOn(commonMain)
            wasmWasiMain.get().dependsOn(this)
            iosMain.get().dependsOn(this)
            watchosMain.get().dependsOn(this)
            tvosMain.get().dependsOn(this)
            androidNativeMain.get().dependsOn(this)
        }
    }
}
