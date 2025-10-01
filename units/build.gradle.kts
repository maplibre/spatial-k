plugins { id("published-library") }

kotlin { sourceSets { commonTest.dependencies { implementation(project(":testutil")) } } }

mavenPublishing {
    pom {
        name = "Spatial K Units"
        description =
            "A Kotlin Multiplatform library for working with physical units of measurement."
    }
}
