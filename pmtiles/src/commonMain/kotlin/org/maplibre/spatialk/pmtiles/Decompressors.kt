package org.maplibre.spatialk.pmtiles

import kotlin.coroutines.cancellation.CancellationException
import org.maplibre.spatialk.pmtiles.internal.DecodeLimits
import org.maplibre.spatialk.pmtiles.internal.DecodePurpose
import org.maplibre.spatialk.pmtiles.internal.decompressionFailed
import org.maplibre.spatialk.pmtiles.internal.displayName
import org.maplibre.spatialk.pmtiles.internal.pmTilesException

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

internal expect fun platformDefaultDecompressors(): Map<Compression, Decompressor>

internal val noneDecompressor: Decompressor = Decompressor { bytes, _ -> bytes }

internal fun ArchiveOpenOptions.effectiveDecompressors(): Map<Compression, Decompressor> =
    platformDefaultDecompressors() + decompressors

internal suspend fun Map<Compression, Decompressor>.decompress(
    compression: Compression,
    bytes: ByteArray,
    limits: DecodeLimits,
): ByteArray =
    decompress(
        compression = compression,
        bytes = bytes,
        limits =
            DecompressionLimits(
                maxCompressedBytes = limits.maxCompressedBytes,
                maxDecompressedBytes = limits.maxDecompressedBytes,
            ),
        purpose = limits.purpose,
    )

internal suspend fun Map<Compression, Decompressor>.decompress(
    compression: Compression,
    bytes: ByteArray,
    limits: DecompressionLimits,
    purpose: DecodePurpose? = null,
): ByteArray {
    validateDecodeLimits(limits, purpose)
    validateCompressedSize(bytes.size, limits, purpose)

    val decompressor = this[compression] ?: unsupportedCompression(compression, purpose)
    val decoded =
        try {
            decompressor.decompress(bytes, limits)
        } catch (error: PmTilesException) {
            if (purpose != null && error.code == PmTilesErrorCode.DecompressionFailed) {
                val message = error.message ?: "decompression failed."
                decompressionFailed("${purpose.displayName} $message", error)
            }
            throw error
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            decompressionFailed("${purposePrefix(purpose)}decompression failed.", error)
        }

    validateDecompressedSize(decoded.size, limits, purpose)
    return decoded
}

private fun validateDecodeLimits(limits: DecompressionLimits, purpose: DecodePurpose?) {
    if (limits.maxCompressedBytes < 0) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${purposePrefix(purpose)}compressed byte limit is negative.",
        )
    }
    if (limits.maxDecompressedBytes < 0) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${purposePrefix(purpose)}decompressed byte limit is negative.",
        )
    }
}

private fun validateCompressedSize(
    size: Int,
    limits: DecompressionLimits,
    purpose: DecodePurpose?,
) {
    if (size > limits.maxCompressedBytes) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${purposePrefix(purpose)}compressed length $size exceeds limit ${limits.maxCompressedBytes}.",
        )
    }
}

internal fun validateDecompressedSize(
    size: Int,
    limits: DecompressionLimits,
    purpose: DecodePurpose? = null,
) {
    if (size > limits.maxDecompressedBytes) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${purposePrefix(purpose)}decompressed length $size exceeds limit ${limits.maxDecompressedBytes}.",
        )
    }
}

private fun unsupportedCompression(compression: Compression, purpose: DecodePurpose?): Nothing =
    throw pmTilesException(
        PmTilesErrorCode.UnsupportedCompression,
        "${purposePrefix(purpose)}compression code ${compression.code} is not supported.",
    )

private fun purposePrefix(purpose: DecodePurpose?): String =
    if (purpose == null) "" else "${purpose.displayName} "
