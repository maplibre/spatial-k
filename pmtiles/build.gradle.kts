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
        val notImplementedMain by creating { dependsOn(commonMain.get()) }
        jsMain.get().dependsOn(notImplementedMain)
        wasmJsMain.get().dependsOn(notImplementedMain)
        wasmWasiMain.get().dependsOn(notImplementedMain)

        val notImplementedTest by creating { dependsOn(commonTest.get()) }
        jsTest.get().dependsOn(notImplementedTest)
        wasmJsTest.get().dependsOn(notImplementedTest)
        wasmWasiTest.get().dependsOn(notImplementedTest)

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
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
