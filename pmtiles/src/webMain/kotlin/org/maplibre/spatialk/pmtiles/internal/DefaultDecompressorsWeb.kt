package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.Decompressor
import org.maplibre.spatialk.pmtiles.KnownCompression

internal actual fun platformDefaultDecompressors(): Map<Compression, Decompressor> = buildMap {
    put(Compression(KnownCompression.None), noneDecompressor)
    if (hasDecompressionStream()) {
        put(
            Compression(KnownCompression.Gzip),
            Decompressor { bytes, limits -> decodeGzip(bytes, limits) },
        )
    }
}
