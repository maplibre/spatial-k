plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.dokka)
}

// Configure NodeJS for all projects
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().apply {
        nodeVersion = "16.0.0"
        nodeDownloadBaseUrl = "https://nodejs.org/dist"
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(rootDir.absoluteFile.resolve("docs/api"))
    moduleName.set("Spatial K")

    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """
            {
                "footerMessage": "Copyright &copy; 2022 Derek Ellis",
                "customStyleSheets": ["${file("docs/css/logo-styles.css").invariantSeparatorsPath}"]
            }
        """.trimIndent()
        )
    )
}
