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
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.io.core)
            implementation(project(":testutil"))
        }
    }
}

// ElementSerializer doesn't work in NodeJS because DOMParser is only available in browser
// https://github.com/pdvrieze/xmlutil/issues/298
tasks.named("jsNodeTest") { enabled = false }

mavenPublishing {
    pom {
        name = "Spatial K GPX"
        description =
            "A Kotlin Multiplatform library for working with the GPS Exchange Format (GPX)."
    }
}
