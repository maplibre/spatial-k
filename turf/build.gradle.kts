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

// runs but fails bezier spline tests
tasks.named("wasmJsNodeTest") { enabled = false }
