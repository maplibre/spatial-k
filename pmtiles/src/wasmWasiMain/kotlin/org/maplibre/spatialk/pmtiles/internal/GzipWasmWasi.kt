package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.DecompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode

internal actual suspend fun decodeGzip(bytes: ByteString, limits: DecompressionLimits): ByteString =
    throw pmTilesException(
        PmTilesErrorCode.UnsupportedCompression,
        "gzip decompression is not supported on wasmWasi.",
    )
