package org.maplibre.spatialk.pmtiles.internal

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.Compressor
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException

internal val noneCompressor: Compressor = Compressor { bytes, _ -> bytes }

internal expect fun platformDefaultCompressors(): Map<CompressionCode, Compressor>

internal fun ArchiveWriteOptions.effectiveCompressors(): Map<CompressionCode, Compressor> =
    platformDefaultCompressors() + compressors

internal enum class EncodePurpose {
    RootDirectory,
    LeafDirectory,
    Metadata,
    Tile,
}

internal suspend fun Map<CompressionCode, Compressor>.compress(
    compression: CompressionCode,
    bytes: ByteString,
    limits: CompressionLimits,
    purpose: EncodePurpose,
): ByteString {
    validateCompressionLimits(limits, purpose)
    validateUncompressedSize(bytes.size, limits, purpose)

    val compressor = this[compression] ?: unsupportedCompressionCode(compression, purpose)
    val encoded =
        try {
            compressor.compress(bytes, limits)
        } catch (error: PmTilesException) {
            throw error
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            compressionFailed("${purpose.displayName} compression failed.", error)
        }

    validateCompressedSize(encoded.size, limits, purpose)
    return encoded
}

private val EncodePurpose.displayName: String
    get() =
        when (this) {
            EncodePurpose.RootDirectory -> "Root directory"
            EncodePurpose.LeafDirectory -> "Leaf directory"
            EncodePurpose.Metadata -> "Metadata"
            EncodePurpose.Tile -> "Tile"
        }

private fun validateCompressionLimits(limits: CompressionLimits, purpose: EncodePurpose) {
    if (limits.maxUncompressedBytes > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCodes.LimitExceeded,
            "${purpose.displayName} uncompressed byte limit ${limits.maxUncompressedBytes} exceeds the supported Int range.",
        )
    }
    if (limits.maxCompressedBytes > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCodes.LimitExceeded,
            "${purpose.displayName} compressed byte limit ${limits.maxCompressedBytes} exceeds the supported Int range.",
        )
    }
}

private fun validateUncompressedSize(
    size: Int,
    limits: CompressionLimits,
    purpose: EncodePurpose,
) {
    if (size.toULong() > limits.maxUncompressedBytes) {
        throw pmTilesException(
            PmTilesErrorCodes.LimitExceeded,
            "${purpose.displayName} uncompressed length $size exceeds limit ${limits.maxUncompressedBytes}.",
        )
    }
}

private fun validateCompressedSize(size: Int, limits: CompressionLimits, purpose: EncodePurpose) {
    if (size.toULong() > limits.maxCompressedBytes) {
        throw pmTilesException(
            PmTilesErrorCodes.LimitExceeded,
            "${purpose.displayName} compressed length $size exceeds limit ${limits.maxCompressedBytes}.",
        )
    }
}

private fun unsupportedCompressionCode(
    compression: CompressionCode,
    purpose: EncodePurpose,
): Nothing =
    throw pmTilesException(
        PmTilesErrorCodes.UnsupportedCompression,
        "${purpose.displayName} compression code ${compression.code} is not supported.",
    )
