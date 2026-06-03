plugins {
    id("base-module")
    id("org.jetbrains.kotlinx.benchmark")
}

kotlin {
    jvm()
    js(IR) { nodejs() }
    linuxX64()
    macosArm64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":geojson"))
            implementation(project(":units"))
            implementation(project(":turf"))
            implementation(libs.kotlinx.benchmark)
        }

        val jvmAndMacosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(project(":pmtiles"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        jvmMain.get().dependsOn(jvmAndMacosMain)
        macosArm64Main.get().dependsOn(jvmAndMacosMain)
    }
}

benchmark {
    configurations {
        named("main") {
            iterations = project.findProperty("benchmarkIterations")?.toString()?.toInt() ?: 5
            warmups = project.findProperty("benchmarkWarmups")?.toString()?.toInt() ?: 5
        }
    }

    targets {
        register("jvm")
        register("macosArm64")
    }
}
