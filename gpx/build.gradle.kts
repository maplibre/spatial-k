plugins {
    id("published-library")
    id("test-resources")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":geojson"))
            api(project(":units"))

            implementation("io.github.pdvrieze.xmlutil:core:0.91.2")
            implementation("io.github.pdvrieze.xmlutil:serialization:0.91.2")
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.io.core)
            implementation(project(":testutil"))
        }
    }
}
