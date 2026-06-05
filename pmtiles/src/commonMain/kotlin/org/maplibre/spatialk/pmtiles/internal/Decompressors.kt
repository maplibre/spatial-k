package org.maplibre.spatialk.pmtiles.internal

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveOpenOptions
import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.DecompressionLimits
import org.maplibre.spatialk.pmtiles.Decompressor
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

internal expect fun platformDefaultDecompressors(): Map<CompressionCode, Decompressor>

internal val noneDecompressor: Decompressor = Decompressor { bytes, _ -> bytes }

internal fun ArchiveOpenOptions.effectiveDecompressors(): Map<CompressionCode, Decompressor> =
    platformDefaultDecompressors() + decompressors

internal suspend fun Map<CompressionCode, Decompressor>.decompress(
    compression: CompressionCode,
    bytes: ByteString,
    limits: DecodeLimits,
): ByteString =
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

internal suspend fun Map<CompressionCode, Decompressor>.decompress(
    compression: CompressionCode,
    bytes: ByteString,
    limits: DecompressionLimits,
    purpose: DecodePurpose? = null,
): ByteString {
    validateDecodeLimits(limits, purpose)
    validateCompressedSize(bytes.size, limits, purpose)

    val decompressor = this[compression] ?: unsupportedCompressionCode(compression, purpose)
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
        } catch (error: Exception) {
            decompressionFailed("${purposePrefix(purpose)}decompression failed.", error)
        }

    validateDecompressedSize(decoded.size, limits, purpose)
    return decoded
}

private fun validateDecodeLimits(limits: DecompressionLimits, purpose: DecodePurpose?) {
    if (limits.maxCompressedBytes > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${purposePrefix(purpose)}compressed byte limit ${limits.maxCompressedBytes} exceeds the supported Int range.",
        )
    }
    if (limits.maxDecompressedBytes > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${purposePrefix(purpose)}decompressed byte limit ${limits.maxDecompressedBytes} exceeds the supported Int range.",
        )
    }
}

private fun validateCompressedSize(
    size: Int,
    limits: DecompressionLimits,
    purpose: DecodePurpose?,
) {
    if (size.toULong() > limits.maxCompressedBytes) {
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
    if (size.toULong() > limits.maxDecompressedBytes) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${purposePrefix(purpose)}decompressed length $size exceeds limit ${limits.maxDecompressedBytes}.",
        )
    }
}

private fun unsupportedCompressionCode(
    compression: CompressionCode,
    purpose: DecodePurpose?,
): Nothing =
    throw pmTilesException(
        PmTilesErrorCode.UnsupportedCompression,
        "${purposePrefix(purpose)}compression code ${compression.code} is not supported.",
    )

private fun purposePrefix(purpose: DecodePurpose?): String =
    if (purpose == null) "" else "${purpose.displayName} "
