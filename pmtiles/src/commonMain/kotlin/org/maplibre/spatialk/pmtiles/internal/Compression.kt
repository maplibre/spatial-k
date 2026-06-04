package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode

internal suspend fun decodeCompression(
    compression: Compression,
    bytes: ByteArray,
    limits: DecodeLimits,
): ByteArray {
    validateDecodeLimits(limits)
    validateCompressedSize(bytes.size, limits)

    return when (compression.code) {
        Compression.None.code -> {
            validateDecompressedSize(bytes.size, limits)
            bytes
        }

        Compression.Gzip.code -> decodeGzip(bytes, limits)
        else -> unsupportedCompression(compression, limits)
    }
}

// It's a suspend fun only because CompressionStreams on web requires it
internal expect suspend fun decodeGzip(bytes: ByteArray, limits: DecodeLimits): ByteArray

internal class BoundedByteArraySink(private val limits: DecodeLimits) {
    private var bytes = ByteArray(0)
    private var size = 0

    fun append(chunk: ByteArray, length: Int) {
        if (length <= 0) return
        val nextSize = checkedDecompressedSize(size, length, limits)
        ensureCapacity(nextSize)
        chunk.copyInto(
            destination = bytes,
            destinationOffset = size,
            startIndex = 0,
            endIndex = length,
        )
        size = nextSize
    }

    fun toByteArray(): ByteArray = bytes.copyOf(size)

    private fun ensureCapacity(required: Int) {
        if (required <= bytes.size) return

        var nextCapacity = if (bytes.isEmpty()) INITIAL_OUTPUT_CAPACITY else bytes.size
        while (nextCapacity < required) {
            val doubled = nextCapacity * 2
            nextCapacity = if (doubled > nextCapacity) doubled else required
        }
        bytes = bytes.copyOf(nextCapacity.coerceAtMost(limits.maxDecompressedBytes))
    }
}

private fun validateDecodeLimits(limits: DecodeLimits) {
    if (limits.maxCompressedBytes < 0) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${limits.purpose.displayName} compressed byte limit is negative.",
        )
    }
    if (limits.maxDecompressedBytes < 0) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${limits.purpose.displayName} decompressed byte limit is negative.",
        )
    }
}

private fun validateCompressedSize(size: Int, limits: DecodeLimits) {
    if (size > limits.maxCompressedBytes) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${limits.purpose.displayName} compressed length $size exceeds limit ${limits.maxCompressedBytes}.",
        )
    }
}

internal fun validateDecompressedSize(size: Int, limits: DecodeLimits) {
    if (size > limits.maxDecompressedBytes) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${limits.purpose.displayName} decompressed length $size exceeds limit ${limits.maxDecompressedBytes}.",
        )
    }
}

internal fun checkedDecompressedSize(current: Int, nextChunk: Int, limits: DecodeLimits): Int {
    if (nextChunk < 0 || current > limits.maxDecompressedBytes - nextChunk) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "${limits.purpose.displayName} decompressed length exceeds limit ${limits.maxDecompressedBytes}.",
        )
    }
    return current + nextChunk
}

internal fun decompressionFailed(message: String, cause: Throwable? = null): Nothing =
    throw pmTilesException(PmTilesErrorCode.DecompressionFailed, message, cause)

private fun unsupportedCompression(compression: Compression, limits: DecodeLimits): Nothing =
    throw pmTilesException(
        PmTilesErrorCode.UnsupportedCompression,
        "${limits.purpose.displayName} compression code ${compression.code} is not supported.",
    )

internal val DecodePurpose.displayName: String
    get() =
        when (this) {
            DecodePurpose.RootDirectory -> "Root directory"
            DecodePurpose.LeafDirectory -> "Leaf directory"
            DecodePurpose.Metadata -> "Metadata"
            DecodePurpose.Tile -> "Tile"
        }

private const val INITIAL_OUTPUT_CAPACITY = 8 * 1024
