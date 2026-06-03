plugins {
    id("published-library")
    id("test-resources")
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
            implementation(project(":testutil"))
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
