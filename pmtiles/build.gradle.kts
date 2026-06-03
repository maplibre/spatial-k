plugins { id("published-library") }

tasks
    .withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeHostTest>()
    .configureEach {
        environment("PMTILES_PROJECT_DIR", projectDir.absolutePath)
    }

tasks
    .withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>()
    .configureEach {
        environment("PMTILES_PROJECT_DIR", projectDir.absolutePath)
        environment("SIMCTL_CHILD_PMTILES_PROJECT_DIR", projectDir.absolutePath)
    }

kotlin {
    sourceSets {
        listOf(
                "iosArm64Main",
                "iosSimulatorArm64Main",
                "iosX64Main",
                "macosArm64Main",
                "macosX64Main",
                "tvosArm64Main",
                "tvosSimulatorArm64Main",
                "tvosX64Main",
                "watchosArm64Main",
                "watchosDeviceArm64Main",
                "watchosSimulatorArm64Main",
                "watchosX64Main",
                "watchosArm32Main",
            )
            .forEach { getByName(it).kotlin.srcDir("src/appleConcreteMain/kotlin") }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    pom {
        name = "Spatial K PMTiles"
        description = "A Kotlin Multiplatform reader for PMTiles archives."
    }
}
