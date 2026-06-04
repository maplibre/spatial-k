package org.maplibre.spatialk.pmtiles

internal actual fun platformDefaultDecompressors(): Map<Compression, Decompressor> =
    mapOf(Compression.None to noneDecompressor)
