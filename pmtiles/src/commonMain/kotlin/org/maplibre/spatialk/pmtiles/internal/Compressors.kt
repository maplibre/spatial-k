package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.Compressor

internal val noneCompressor: Compressor = Compressor { bytes, _ -> bytes }

internal fun platformDefaultCompressors(): Map<CompressionCode, Compressor> =
    mapOf(CompressionCodes.None to noneCompressor)

internal fun ArchiveWriteOptions.effectiveCompressors(): Map<CompressionCode, Compressor> =
    platformDefaultCompressors() + compressors
