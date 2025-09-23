@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlinx.benchmark)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
        compilations.create("bench") {
            associateWith(this@jvm.compilations.getByName("main"))
        }
    }

    js(IR) {
        browser()
        nodejs()
        compilations.create("bench") {
            associateWith(this@js.compilations.getByName("main"))
        }
    }

    wasmJs {
        browser()
        nodejs()
        d8()
    }

    wasmWasi {
        nodejs()
    }

    // native tier 1
    macosArm64()
    iosSimulatorArm64()
    iosArm64()

    // native tier 2
    macosX64()
    iosX64()
    linuxX64 {
        compilations.create("bench") {
            associateWith(this@linuxX64.compilations.getByName("main"))
        }
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
    mingwX64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    watchosDeviceArm64()

    sourceSets {
        all {
            with(languageSettings) {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("kotlinx.serialization.InternalSerializationApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }

        commonMain {
            dependencies {
                api(libs.kotlinx.serialization)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val commonBench by creating {
            dependencies {
                implementation(libs.kotlinx.benchmark)
            }
        }

        getByName("jsBench") {
            dependsOn(commonBench)
        }

        getByName("jvmBench") {
            dependsOn(commonBench)
        }

        getByName("linuxX64Bench") {
            dependsOn(commonBench)
        }
    }
}

benchmark {
    this.configurations {
        getByName("main") {
            iterations = 5
        }
    }

    targets {
        register("jvmBench")
        register("jsBench")
        register("linuxX64Bench")
    }
}

dokka {
    dokkaSourceSets {
        configureEach {
            includes.from("MODULE.md")
        }
    }
}
