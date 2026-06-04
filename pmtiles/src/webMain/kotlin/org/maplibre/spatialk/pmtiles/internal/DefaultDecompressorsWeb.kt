package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.Decompressor

internal actual fun platformDefaultDecompressors(): Map<Compression, Decompressor> = buildMap {
    put(Compression.None, noneDecompressor)
    if (hasDecompressionStream()) {
        put(Compression.Gzip, Decompressor { bytes, limits -> decodeGzip(bytes, limits) })
    }
}
