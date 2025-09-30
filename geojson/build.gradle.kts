@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("published-library")
    id("test-resources")
    id("org.jetbrains.kotlinx.benchmark")
}

afterEvaluate {
    kotlin {
        // benchmark compilations
        targets.forEach { target ->
            if (target.name in listOf("jvm", "js", "linuxX64", "macosArm64", "mingwX64")) {
                target.compilations.create("bench") {
                    associateWith(target.compilations.getByName("main"))
                }
            }
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.annotations)
                api(libs.kotlinx.serialization.json)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(project(":testutil"))
            }
        }

        create("commonBench").apply {
            listOf("jvm", "js", "linuxX64", "macosArm64", "mingwX64").forEach {
                findByName("${it}Bench")?.dependsOn(this@apply)
            }
            dependencies { implementation(libs.kotlinx.benchmark) }
        }
    }
}

benchmark {
    this.configurations { getByName("main") { iterations = 5 } }

    targets {
        register("jvmBench")
        register("jsBench")
        register("linuxX64Bench")
        register("macosArm64Bench")
        register("mingwX64")
    }
}
