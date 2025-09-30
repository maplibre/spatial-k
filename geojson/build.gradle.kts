@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("published-library")
    id("test-resources")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.annotations)
                api(libs.kotlinx.serialization.json)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(project(":testutil"))
            }
        }
    }
}
