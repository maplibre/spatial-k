package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.DecompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode

internal actual suspend fun decodeGzip(bytes: ByteArray, limits: DecompressionLimits): ByteArray =
    throw pmTilesException(
        PmTilesErrorCode.UnsupportedCompression,
        "gzip decompression is not supported on wasmWasi.",
    )
