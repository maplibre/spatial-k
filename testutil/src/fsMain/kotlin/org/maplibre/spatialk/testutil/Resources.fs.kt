package org.maplibre.spatialk.testutil

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString

actual fun readResourceFile(filename: String): String {
    return SystemFileSystem.source(resourcePath(filename)).use { rawSource ->
        rawSource.buffered().readString()
    }
}

actual fun readResourceBytes(filename: String): ByteArray {
    return SystemFileSystem.source(resourcePath(filename)).use { rawSource ->
        rawSource.buffered().readByteArray()
    }
}

private fun resourcePath(filename: String): Path = Path("src/commonTest/resources/$filename")
