@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.kotlinx.kover)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()
    compilerOptions { allWarningsAsErrors = true }

    jvm {
        compilerOptions { jvmTarget = JvmTarget.JVM_1_8 }
        val main by compilations.getting
        compilations.create("bench") { associateWith(main) }
    }

    js(IR) {
        browser()
        nodejs()
        val main by compilations.getting
        compilations.create("bench") { associateWith(main) }
    }

    wasmJs {
        browser()
        nodejs()
        d8()
    }

    wasmWasi { nodejs() }

    // native tier 1
    macosArm64 {
        val main by compilations.getting
        compilations.create("bench") { associateWith(main) }
    }
    iosSimulatorArm64()
    iosArm64()

    // native tier 2
    macosX64()
    iosX64()
    linuxX64 {
        val main by compilations.getting
        compilations.create("bench") { associateWith(main) }
    }
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()

    // native tier 3
    mingwX64 {
        val main by compilations.getting
        compilations.create("bench") { associateWith(main) }
    }
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    watchosDeviceArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.annotations)
                api(libs.kotlinx.serialization.json)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(project(":testutil"))
            }
        }

        create("commonBench").apply {
            listOf("jvm", "js", "linuxX64", "macosArm64", "mingwX64").forEach {
                getByName("${it}Bench").dependsOn(this@apply)
            }
            dependencies { implementation(libs.kotlinx.benchmark) }
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

benchmark {
    this.configurations { getByName("main") { iterations = 5 } }

    targets {
        register("jvmBench")
        register("jsBench")
        register("linuxX64Bench")
        register("macosArm64Bench")
        register("mingwX64")
    }
}

dokka {
    dokkaSourceSets {
        configureEach {
            includes.from("MODULE.md")
            sourceLink {
                // TODO link to version (git tag) using jgitver
                remoteUrl("https://github.com/maplibre/spatial-k/tree/main/")
                localDirectory = rootDir
            }
            externalDocumentationLinks {
                create("kotlinx-serialization") {
                    url("https://kotlinlang.org/api/kotlinx.serialization/")
                }
            }
        }
    }
    pluginsConfiguration {
        html { customStyleSheets.from(rootProject.file("docs/styles/dokka-extra.css")) }
    }
}
