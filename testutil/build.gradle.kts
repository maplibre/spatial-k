plugins { id("multiplatform-module") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.io.core)
            api(libs.kotlinx.serialization.json)
            implementation(project(":units"))
            implementation(project(":geojson"))
        }

        val commonMain by getting

        create("fsMain").apply {
            dependsOn(commonMain)
            jvmMain.get().dependsOn(this)
            linuxMain.get().dependsOn(this)
            mingwMain.get().dependsOn(this)
            macosMain.get().dependsOn(this)
        }

        create("bundleMain").apply {
            dependsOn(commonMain)
            jsMain.get().dependsOn(this)
            wasmJsMain.get().dependsOn(this)
        }

        create("todoMain").apply {
            dependsOn(commonMain)
            wasmWasiMain.get().dependsOn(this)
            iosMain.get().dependsOn(this)
            watchosMain.get().dependsOn(this)
            tvosMain.get().dependsOn(this)
            androidNativeMain.get().dependsOn(this)
        }
    }
}
