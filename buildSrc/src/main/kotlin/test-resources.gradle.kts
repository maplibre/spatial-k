tasks
    .matching { task ->
        listOf(
                // no filesystem support
                ".*BrowserTest",
                "wasmJsD8Test",
                "wasmWasi.*Test",
                ".*Simulator.*Test",
            )
            .any { task.name.matches(it.toRegex()) }
    }
    .configureEach { enabled = false }

tasks.register<Copy>("copyiOSTestResources") {
    from("src/commonTest/resources")
    into("build/bin/iosX64/debugTest/resources")
}

tasks.named("iosX64Test") { dependsOn("copyiOSTestResources") }

tasks.register<Copy>("copyiOSArmTestResources") {
    from("src/commonTest/resources")
    into("build/bin/iosSimulatorArm64/debugTest/resources")
}

tasks.named("iosSimulatorArm64Test") { dependsOn("copyiOSArmTestResources") }
