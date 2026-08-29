plugins { id("published-library") }

kotlin {
    sourceSets {
        commonMain.dependencies { api(project(":geojson")) }

        commonTest.dependencies { implementation(project(":testutil")) }

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
