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
