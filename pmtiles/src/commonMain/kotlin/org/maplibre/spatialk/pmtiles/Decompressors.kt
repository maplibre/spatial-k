package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException

/**
 * Decompressed byte limits passed to a [Decompressor].
 *
 * @property maxCompressedBytes Maximum compressed input bytes.
 * @property maxDecompressedBytes Maximum decompressed output bytes.
 */
public data class DecompressionLimits(
    public val maxCompressedBytes: Int,
    public val maxDecompressedBytes: Int,
)

/** Decompresses bytes for one PMTiles compression format. */
public fun interface Decompressor {
    /**
     * Returns decompressed [bytes].
     *
     * Implementations should enforce [limits] while decoding. The reader also validates the
     * returned byte count before using it.
     */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun decompress(bytes: ByteArray, limits: DecompressionLimits): ByteArray
}
