plugins {
    id("published-library")
    id("test-resources")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":geojson"))
            implementation(libs.jetbrains.annotations)
            implementation(libs.xmlutil.core)
            implementation(libs.xmlutil.serialization)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.io.core)
            implementation(project(":testutil"))
        }
    }
}

mavenPublishing {
    pom {
        name = "Spatial K GPX"
        description =
            "A Kotlin Multiplatform library for working with the GPS Exchange Format (GPX)."
    }
}
