import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

plugins {
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.dokka")
    id("semver")
}

dokka {
    moduleName = "Spatial K"
    dokkaPublications {
        html { outputDirectory = rootDir.absoluteFile.resolve("docs/public/api/dokka") }
    }
    pluginsConfiguration {
        html {
            customStyleSheets.from(file("docs/src/styles/dokka-extra.css"))
            customAssets.from(file("docs/src/assets/logo-icon-dark.svg"))
            footerMessage = "Copyright &copy; 2025 MapLibre Contributors"
        }
    }
}

kover {
    reports {
        total {
            log {
                // default groups by module
                groupBy = GroupingEntityType.PACKAGE
            }
        }
    }
}

dependencies {
    dokka(project(":geojson"))
    kover(project(":geojson"))

    dokka(project(":units"))
    kover(project(":units"))

    dokka(project(":turf"))
    kover(project(":turf"))

    dokka(project(":gpx"))
    kover(project(":gpx"))

    dokka(project(":pmtiles"))
    kover(project(":pmtiles"))

    dokka(project(":polyline-encoding"))
    kover(project(":polyline-encoding"))
}
