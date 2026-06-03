@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.maplibre.spatialk.pmtiles

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import org.maplibre.spatialk.pmtiles.internal.pmTilesException
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.posix.memcpy

internal actual fun ByteArray.toNSData(): NSData {
    val data = NSMutableData()
    data.setLength(size.convert())
    if (isNotEmpty()) {
        usePinned { pinned ->
            memcpy(data.mutableBytes, pinned.addressOf(0), size.convert())
        }
    }
    return data
}

internal actual fun NSData.byteCount(): ULong = length.convert()

internal actual fun NSData.readBytes(length: Int): ByteArray {
    val bytesPointer =
        bytes
            ?: throw pmTilesException(
                PmTilesErrorCode.SourceUnavailable,
                "Byte range data source returned non-empty NSData with a null bytes pointer.",
            )
    return bytesPointer.readBytes(length)
}
