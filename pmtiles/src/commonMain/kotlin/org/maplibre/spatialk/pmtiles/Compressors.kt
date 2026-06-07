package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.io.bytestring.ByteString

/**
 * Compression byte limits passed to compressor implementations.
 *
 * @property maxUncompressedBytes Maximum uncompressed input bytes.
 * @property maxCompressedBytes Maximum compressed output bytes.
 */
public data class CompressionLimits
internal constructor(
    public val maxUncompressedBytes: ULong,
    public val maxCompressedBytes: ULong,
)

/** Compresses bytes for one PMTiles compression format. */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "__KotlinCompressor")
public fun interface Compressor {
    /**
     * Returns compressed [bytes].
     *
     * Implementations should enforce [limits] while encoding. The writer also validates the
     * returned byte count before using it.
     */
    @Throws(PmTilesException::class, CancellationException::class)
    public suspend fun compress(bytes: ByteString, limits: CompressionLimits): ByteString
}
