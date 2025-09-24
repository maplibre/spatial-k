plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.dokka)
}

dokka {
    moduleName = "Spatial K"
    dokkaPublications.html {
        outputDirectory = rootDir.absoluteFile.resolve("docs/api")
    }
    pluginsConfiguration.html {
        customStyleSheets.from(file("docs/css/logo-styles.css"))
        customAssets.from(file("docs/images/logo.png"))
        footerMessage = "Copyright &copy; 2025 MapLibre Contributors"
    }
}

dependencies {
    dokka(project(":geojson"))
    dokka(project(":turf"))
    project(":testutil")
}
