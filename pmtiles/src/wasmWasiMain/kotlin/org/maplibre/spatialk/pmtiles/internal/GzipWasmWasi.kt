package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.PmTilesErrorCode

internal actual suspend fun decodeGzip(bytes: ByteArray, limits: DecodeLimits): ByteArray =
    throw pmTilesException(
        PmTilesErrorCode.UnsupportedCompression,
        "${limits.purpose.displayName} gzip decompression is not supported on wasmWasi.",
    )
