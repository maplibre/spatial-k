@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("published-library")
    id("test-resources")
}

kotlin {
    // Test-only framework for the Swift interop tests in Package.swift.
    val xcFramework = XCFramework("SpatialKGeoJson")
    macosArm64 {
        binaries.framework(listOf(NativeBuildType.DEBUG)) {
            baseName = "SpatialKGeoJson"
            isStatic = true
            xcFramework.add(this)
        }
    }

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

mavenPublishing {
    pom {
        name = "Spatial K GeoJSON"
        description = "A Kotlin Multiplatform implementation of the GeoJSON format."
    }
}
