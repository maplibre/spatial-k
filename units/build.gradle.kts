plugins { id("published-library") }

kotlin { sourceSets { commonTest.dependencies { implementation(project(":testutil")) } } }
