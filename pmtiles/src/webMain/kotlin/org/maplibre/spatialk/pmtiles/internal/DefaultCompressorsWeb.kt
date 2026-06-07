package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.Compressor

internal actual fun platformDefaultCompressors(): Map<CompressionCode, Compressor> = buildMap {
    put(CompressionCodes.None, noneCompressor)
    if (hasCompressionStream()) {
        put(
            CompressionCodes.Gzip,
            Compressor(::encodeGzip),
        )
    }
}
