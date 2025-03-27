import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlinx.benchmark)
}

kotlin {
    explicitApi()

    jvm {
        compilations.create("bench")
    }
    js {
        browser {
        }
        nodejs {
        }

        compilations.create("bench")
    }
    // For ARM, should be changed to iosArm32 or iosArm64
    // For Linux, should be changed to e.g. linuxX64
    // For MacOS, should be changed to e.g. macosX64
    // For Windows, should be changed to e.g. mingwX64
    linuxX64("native") {
        compilations.create("bench")
    }
    mingwX64("mingw")
    macosX64("macos")
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        all {
            with(languageSettings) {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("kotlinx.serialization.InternalSerializationApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.serialization)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsMain by getting {}
        val jsCommonMain by creating {
            dependsOn(commonMain)
        }
        jsMain.dependsOn(jsCommonMain)

        val jvmMain by getting {}
        val jvmCommonMain by creating {
            dependsOn(commonMain)
        }
        jvmMain.dependsOn(jvmCommonMain)

        val nativeMain by getting {}
        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }
        nativeMain.dependsOn(nativeCommonMain)

        val macosMain by getting
        val mingwMain by getting
        macosMain.dependsOn(nativeCommonMain)
        mingwMain.dependsOn(nativeCommonMain)

        val nativeTest by getting {}
        val nativeCommonTest by creating {
            dependsOn(commonTest)
        }
        nativeTest.dependsOn(nativeCommonTest)

        val macosTest by getting
        val mingwTest by getting
        macosTest.dependsOn(nativeCommonTest)
        mingwTest.dependsOn(nativeCommonTest)

        val commonBench by creating {
            dependencies {
                implementation(libs.kotlinx.benchmark)
            }
        }

        val jsBenchCommon by creating {
            dependsOn(commonBench)
        }
        val jsBench by getting {
            dependsOn(jsBenchCommon)
        }

        val jvmBenchCommon by creating {
            dependsOn(commonBench)
        }
        val jvmBench by getting {
            dependsOn(jvmBenchCommon)
        }

        val nativeBenchCommon by creating {
            dependsOn(commonBench)
        }
        val nativeBench by getting {
            dependsOn(nativeBenchCommon)
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(nativeCommonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(nativeCommonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

benchmark {
    this.configurations {
        getByName("main") {
            iterations = 5
        }
    }

    targets {
        register("jvmBench")
        register("jsBench")
        register("nativeBench")
    }
}

tasks.withType<org.jetbrains.dokka.gradle.AbstractDokkaTask>().configureEach {
    outputDirectory.set(layout.buildDirectory.dir("docs/api"))
}

tasks.withType(KotlinCompile::class.java).configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}
