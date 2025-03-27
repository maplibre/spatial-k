import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.resources)
}

kotlin {
    explicitApi()

    jvm()
    // Disable JavaScript target to work around the npm directory issue
    // js {
    //     browser()
    //     nodejs()
    //     binaries.executable()
    // }
    // For ARM, should be changed to iosArm32 or iosArm64
    // For Linux, should be changed to e.g. linuxX64
    // For MacOS, should be changed to e.g. macosX64
    // For Windows, should be changed to e.g. mingwX64

    linuxX64("native")
    mingwX64("mingw")
    macosX64("macos")
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets["commonMain"].dependencies {
        api(project(":geojson"))
    }

    sourceSets["commonTest"].dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))
        implementation(libs.resources)
    }

    sourceSets["jvmMain"].dependencies {
    }

    sourceSets["jvmTest"].dependencies {
    }

    // JavaScript target is disabled
    // sourceSets["jsMain"].dependencies {
    // }

    // sourceSets["jsTest"].dependencies {
    // }

    sourceSets {
        val commonMain by getting {}
        val commonTest by getting {}

        // JVM intermediate source sets
        val jvmCommonMain by creating {
            dependsOn(commonMain)
        }
        val jvmCommonTest by creating {
            dependsOn(commonTest)
        }
        val jvmMain by getting {
            dependsOn(jvmCommonMain)
        }
        val jvmTest by getting {
            dependsOn(jvmCommonTest)
        }

        // JS intermediate source sets are disabled
        // val jsCommonMain by creating {
        //     dependsOn(commonMain)
        // }
        // val jsCommonTest by creating {
        //     dependsOn(commonTest)
        // }
        // val jsMain by getting {
        //     dependsOn(jsCommonMain)
        // }
        // val jsTest by getting {
        //     dependsOn(jsCommonTest)
        // }

        val nativeMain by getting {
            dependsOn(commonMain)
        }
        getByName("macosMain").dependsOn(nativeMain)
        getByName("mingwMain").dependsOn(nativeMain)
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(nativeMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val nativeTest by getting {}
        getByName("macosTest").dependsOn(nativeTest)
        getByName("mingwTest").dependsOn(nativeTest)
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(nativeTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

        all {
            with(languageSettings) {
                optIn("kotlin.RequiresOptIn")
            }
        }
    }
}

// JavaScript target is disabled
// tasks.named("jsBrowserTest") { enabled = false }

// Configure Dokka tasks
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    // Disable Dokka tasks to work around the npm directory issue
    enabled = false

    // custom output directory (will not be used since tasks are disabled)
    outputDirectory.set(buildDir.resolve("$rootDir/docs/api"))
}

tasks.register<Jar>("dokkaJavadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

tasks.withType(KotlinCompile::class.java).configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

// Working around dokka problems
afterEvaluate {
    tasks.named("dokkaJavadocJar").configure {
        dependsOn(":geojson:dokkaHtml")
    }
}
