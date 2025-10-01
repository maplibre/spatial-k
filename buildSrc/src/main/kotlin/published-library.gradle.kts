import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("base-module")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    explicitApi()
    abiValidation {
        @OptIn(ExperimentalAbiValidation::class)
        enabled = true
    }
}

dokka {
    dokkaSourceSets {
        configureEach {
            includes.from("MODULE.md")
            sourceLink {
                // TODO link to version (git tag) using jgitver
                remoteUrl("https://github.com/maplibre/spatial-k/tree/main/")
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
