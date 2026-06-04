package org.maplibre.spatialk.pmtiles.internal

// TODO: future PR will add a codec registry, until then wasmWasi is build only
internal actual suspend fun decodeGzip(bytes: ByteArray, limits: DecodeLimits): ByteArray =
    throw NotImplementedError("gzip decoding is not implemented for this Kotlin target.")
