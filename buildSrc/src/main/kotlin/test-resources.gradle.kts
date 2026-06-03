import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

// Resource tests read files via testutil on runners with filesystem or bundled-resource support.
// Disable runners where those resources are not available.
tasks.withType<KotlinNativeSimulatorTest>().configureEach { enabled = false }

tasks
    .matching { task ->
        listOf(
                ".*BrowserTest",
                "wasmJsD8Test",
                "wasmWasi.*Test",
            )
            .any { task.name.matches(it.toRegex()) }
    }
    .configureEach { enabled = false }
