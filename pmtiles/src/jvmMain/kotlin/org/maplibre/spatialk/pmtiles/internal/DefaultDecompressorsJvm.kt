package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.Decompressor
import org.maplibre.spatialk.pmtiles.KnownCompression

internal actual fun platformDefaultDecompressors(): Map<Compression, Decompressor> =
    mapOf(
        Compression(KnownCompression.None) to noneDecompressor,
        Compression(KnownCompression.Gzip) to
            Decompressor { bytes, limits -> decodeGzip(bytes, limits) },
    )
