plugins { id("com.javiersc.semver") }

semver { tagPrefix = "v" }

tasks.register("version") { doLast { println(project.version) } }
