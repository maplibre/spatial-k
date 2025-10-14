plugins { id("published-library") }

kotlin {
    sourceSets {
        commonMain.dependencies { implementation(libs.kotlinx.serialization.core) }
        commonTest.dependencies {
            implementation(project(":testutil"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

mavenPublishing {
    pom {
        name = "Spatial K Units"
        description =
            "A Kotlin Multiplatform library for working with physical units of measurement."
    }
}
