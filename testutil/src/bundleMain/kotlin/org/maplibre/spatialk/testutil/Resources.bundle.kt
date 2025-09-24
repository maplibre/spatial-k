package org.maplibre.spatialk.testutil

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

actual fun readResourceFile(filename: String): String {
    val filePath = Path("kotlin/$filename")
    return SystemFileSystem.source(filePath).use { rawSource -> rawSource.buffered().readString() }
}
