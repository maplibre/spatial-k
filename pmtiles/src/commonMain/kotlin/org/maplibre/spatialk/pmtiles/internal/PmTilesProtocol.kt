package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString

internal object PmTilesProtocol {
    const val SUPPORTED_VERSION: Int = 3

    val MAGIC_BYTES: ByteString =
        ByteString(
            0x50.toByte(),
            0x4d.toByte(),
            0x54.toByte(),
            0x69.toByte(),
            0x6c.toByte(),
            0x65.toByte(),
            0x73.toByte(),
        )
}
