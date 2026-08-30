plugins {
    id("published-library")
    id("test-resources")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":geojson"))
            implementation(libs.kotlinx.serialization.core)
        }

        commonTest.dependencies {
            implementation(project(":testutil"))
            implementation(libs.kotlinx.serialization.json)
        }

        jvmTest.dependencies { implementation(kotlin("test")) }
    }
}

mavenPublishing {
    pom {
        name = "Spatial K Geohash"
        description =
            "A Kotlin Multiplatform library for Geohash cells and OpenStreetMap shortlinks."
    }
}
