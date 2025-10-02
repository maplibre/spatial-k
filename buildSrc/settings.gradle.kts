dependencyResolutionManagement {
    versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0") }
