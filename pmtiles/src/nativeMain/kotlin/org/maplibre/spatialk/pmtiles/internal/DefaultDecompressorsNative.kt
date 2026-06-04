package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.Decompressor

internal actual fun platformDefaultDecompressors(): Map<Compression, Decompressor> =
    mapOf(
        Compression.None to noneDecompressor,
        Compression.Gzip to Decompressor { bytes, limits -> decodeGzip(bytes, limits) },
    )
