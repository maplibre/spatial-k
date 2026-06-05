package org.maplibre.spatialk.testutil

// Some KMP targets don't easily support filesystem access because they run in a sandbox, such as
// iOS simulators, WASM WASI, and browsers. TODO: embed fixture files in the build so these targets
// can read them without direct filesystem access.
actual fun readResourceFile(filename: String): String {
    TODO()
}

actual fun readResourceBytes(filename: String): ByteArray {
    TODO()
}
