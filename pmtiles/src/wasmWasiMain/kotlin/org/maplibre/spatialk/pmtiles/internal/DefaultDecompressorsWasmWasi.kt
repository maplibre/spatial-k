package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.Decompressor

internal actual fun platformDefaultDecompressors(): Map<CompressionCode, Decompressor> =
    mapOf(CompressionCodes.None to noneDecompressor)
