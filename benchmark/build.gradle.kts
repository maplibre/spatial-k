import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.benchmark")
}

kotlin {
    applyDefaultHierarchyTemplate()
    compilerOptions { allWarningsAsErrors = true }

    jvm { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }
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

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
        }
    }
}

benchmark {
    configurations {
        named("main") {
            iterations = project.findProperty("benchmarkIterations")?.toString()?.toInt() ?: 5
        }
    }

    targets {
        register("jvm")
        register("js")
        register("linuxX64")
        register("macosArm64")
        register("mingwX64")
    }
}
