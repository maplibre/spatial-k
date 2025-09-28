@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlinx.kover)
}

kotlin {
    explicitApi()
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

    sourceSets {
        commonMain.dependencies {
            api(project(":geojson"))
            api(project(":units"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kotlinx.io.core)
            implementation(project(":testutil"))
        }
    }
}

// TODO fix tests on these platforms
tasks
    .matching { task ->
        listOf(
                // no filesystem support
                ".*BrowserTest",
                "wasmJsD8Test",
                "wasmWasi.*Test",
                ".*Simulator.*Test",
                // runs but fails bezier spline tests
                "wasmJsNodeTest",
            )
            .any { task.name.matches(it.toRegex()) }
    }
    .configureEach { enabled = false }

tasks.register<Copy>("copyiOSTestResources") {
    from("src/commonTest/resources")
    into("build/bin/iosX64/debugTest/resources")
}

tasks.named("iosX64Test") { dependsOn("copyiOSTestResources") }

tasks.register<Copy>("copyiOSArmTestResources") {
    from("src/commonTest/resources")
    into("build/bin/iosSimulatorArm64/debugTest/resources")
}

tasks.named("iosSimulatorArm64Test") { dependsOn("copyiOSArmTestResources") }

dokka {
    dokkaSourceSets {
        configureEach {
            includes.from("MODULE.md")
            sourceLink {
                // TODO link to version (git tag) using jgitver
                remoteUrl("https://github.com/maplibre/spatial-k/tree/main/")
                localDirectory = rootDir
            }
        }
    }
    pluginsConfiguration {
        html { customStyleSheets.from(rootProject.file("docs/styles/dokka-extra.css")) }
    }
}
