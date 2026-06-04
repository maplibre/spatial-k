package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

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
) {
    internal constructor(
        maxCompressedBytes: Int,
        maxDecompressedBytes: Int,
    ) : this(
        maxCompressedBytes = maxCompressedBytes.toLimitULong("compressed"),
        maxDecompressedBytes = maxDecompressedBytes.toLimitULong("decompressed"),
    )
}

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
    public suspend fun decompress(bytes: ByteArray, limits: DecompressionLimits): ByteArray
}

private fun Int.toLimitULong(kind: String): ULong {
    if (this < 0) {
        throw PmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "Configured $kind byte limit is negative.",
        )
    }
    return toULong()
}
