import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("published-library")
    id("test-resources")
}

kotlin {
    val frameworkName = "SpatialKPmtiles"
    val xcFramework = XCFramework(frameworkName)
    val frameworkTargets = setOf("iosArm64", "iosSimulatorArm64", "macosArm64")

    targets.withType<KotlinNativeTarget>().configureEach {
        if (name in frameworkTargets) {
            binaries.framework {
                baseName = frameworkName
                isStatic = true
                binaryOption("bundleId", "org.maplibre.spatialk.pmtiles")
                xcFramework.add(this)
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":testutil"))
        }

        webMain.dependencies {
            implementation(libs.kotlin.web)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    pom {
        name = "Spatial K PMTiles"
        description = "A Kotlin Multiplatform reader for PMTiles archives."
    }
}
