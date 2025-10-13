import dev.detekt.gradle.extensions.FailOnSeverity
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("multiplatform-module")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlinx.kover")
    id("semver")
    id("dev.detekt")
}

group = "org.maplibre.spatialk"

kotlin {
    explicitApi()
    abiValidation {
        @OptIn(ExperimentalAbiValidation::class)
        enabled = true
    }
}

detekt {
    source.setFrom("src/commonMain/kotlin")
    config.setFrom(rootProject.file("detekt.yml"))
    failOnSeverity = FailOnSeverity.Warning
    basePath.set(rootProject.rootDir)
}

dokka {
    dokkaSourceSets {
        configureEach {
            includes.from("MODULE.md")
            sourceLink {
                remoteUrl("https://github.com/maplibre/spatial-k/tree/${project.version}/")
                localDirectory = rootDir
            }
            externalDocumentationLinks {
                create("kotlinx-serialization") {
                    url("https://kotlinlang.org/api/kotlinx.serialization/")
                }
            }
        }
    }
    pluginsConfiguration {
        html { customStyleSheets.from(rootProject.file("docs/styles/dokka-extra.css")) }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    pom {
        url = "https://maplibre.org/spatial-k/"

        scm {
            url = "https://github.com/maplibre/spatial-k"
            connection = "scm:git:git://github.com/maplibre/spatial-k.git"
            developerConnection = "scm:git:git://github.com/maplibre/spatial-k.git"
        }

        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "maplibre"
                name = "MapLibre"
            }
        }
    }
}
