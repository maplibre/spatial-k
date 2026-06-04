package org.maplibre.spatialk.pmtiles

import org.maplibre.spatialk.pmtiles.internal.decodeGzip

internal actual fun platformDefaultDecompressors(): Map<Compression, Decompressor> =
    mapOf(
        Compression.None to noneDecompressor,
        Compression.Gzip to Decompressor { bytes, limits -> decodeGzip(bytes, limits) },
    )
