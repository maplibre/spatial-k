package org.maplibre.spatialk.pmtiles.internal

internal actual fun decodeGzip(bytes: ByteArray, limits: DecodeLimits): ByteArray =
    throw NotImplementedError("gzip decoding is not implemented for this Kotlin target.")
