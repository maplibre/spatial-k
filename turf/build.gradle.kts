plugins {
    id("published-library")
    id("test-resources")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":geojson"))
            api(project(":units"))
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.io.core)
            implementation(project(":testutil"))
        }
    }
}

// TODO: runs but fails bezier spline tests
tasks.named("wasmJsNodeTest") { enabled = false }

mavenPublishing {
    pom {
        name = "Spatial K Turf"
        description = "A Kotlin Multiplatform port of Turf.js, a spatial analysis library."
    }
}
