package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlinx.io.bytestring.ByteString

/**
 * Decompressed byte limits passed to decompressor implementations.
 *
 * @property maxCompressedBytes Maximum compressed input bytes.
 * @property maxDecompressedBytes Maximum decompressed output bytes.
 */
public data class DecompressionLimits
internal constructor(
    public val maxCompressedBytes: ULong,
    public val maxDecompressedBytes: ULong,
)

/** Decompresses bytes for one PMTiles compression format. */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
public fun interface Decompressor {
    /**
     * Returns decompressed [bytes].
     *
     * Implementations should enforce [limits] while decoding. The reader also validates the
     * returned byte count before using it.
     */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun decompress(bytes: ByteString, limits: DecompressionLimits): ByteString
}
