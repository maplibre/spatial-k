import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.dokka)
}

dokka {
    moduleName = "Spatial K"
    dokkaPublications { html { outputDirectory = rootDir.absoluteFile.resolve("docs/api") } }
    pluginsConfiguration {
        html {
            customStyleSheets.from(file("docs/styles/dokka-extra.css"))
            customAssets.from(file("docs/images/logo-icon.svg"))
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
}
