package org.maplibre.spatialk.pmtiles

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.Foundation.NSFileManager

@OptIn(ExperimentalForeignApi::class)
internal actual fun readPmTilesFixture(path: String): ByteArray {
    val candidates =
        listOf(
            "pmtiles/src/commonTest/resources/fixtures/$path",
            "../pmtiles/src/commonTest/resources/fixtures/$path",
        )
    val data = candidates.firstNotNullOfOrNull { fixturePath ->
        NSFileManager.defaultManager.contentsAtPath(fixturePath)
    }
    require(data != null) { "Missing PMTiles benchmark fixture $path" }
    return data.readBytes()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.readBytes(): ByteArray {
    val byteCount: ULong = length.convert()
    if (byteCount == 0uL) return ByteArray(0)
    require(byteCount <= Int.MAX_VALUE.toULong()) { "PMTiles benchmark fixture is too large" }
    val bytesPointer = bytes
    require(bytesPointer != null) { "PMTiles benchmark fixture has no bytes pointer" }
    return bytesPointer.readBytes(byteCount.toInt())
}
