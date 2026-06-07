package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.Compressor

internal actual fun platformDefaultCompressors(): Map<CompressionCode, Compressor> =
    mapOf(CompressionCodes.None to noneCompressor)
