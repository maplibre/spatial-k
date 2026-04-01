plugins {
    id("published-library")
    id("test-resources")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":geojson"))
        }
    }
}

mavenPublishing {
    pom {
        name = "Spatial K Polyline Encoding"
        description = "A Kotlin Multiplatform library for encoding & decoding polylines."
    }
}
