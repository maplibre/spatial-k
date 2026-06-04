package org.maplibre.spatialk.pmtiles

import org.maplibre.spatialk.pmtiles.internal.decodeGzip
import org.maplibre.spatialk.pmtiles.internal.hasDecompressionStream

internal actual fun platformDefaultDecompressors(): Map<Compression, Decompressor> = buildMap {
    put(Compression.None, noneDecompressor)
    if (hasDecompressionStream()) {
        put(Compression.Gzip, Decompressor { bytes, limits -> decodeGzip(bytes, limits) })
    }
}
