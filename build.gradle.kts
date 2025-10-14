import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import ru.vyarus.gradle.plugin.mkdocs.task.MkdocsTask

plugins {
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.dokka")
    id("semver")
    id("ru.vyarus.mkdocs-build")
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

mkdocs {
    sourcesDir = "."
    strict = true
    publish {
        docPath = null // single version site
    }
}

tasks.withType<MkdocsTask>().configureEach {
    dependsOn("dokkaGenerateHtml")
    extras.assign(provider { mapOf("project_version" to project.version.toString()) })
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
