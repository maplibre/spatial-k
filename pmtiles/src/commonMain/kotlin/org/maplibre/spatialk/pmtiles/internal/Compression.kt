package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.DecompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode

internal class BoundedByteArraySink(private val limits: DecompressionLimits) {
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

    fun toByteString(): ByteString = ByteString(bytes, startIndex = 0, endIndex = size)

    private fun ensureCapacity(required: Int) {
        if (required <= bytes.size) return

        var nextCapacity = if (bytes.isEmpty()) INITIAL_OUTPUT_CAPACITY else bytes.size
        while (nextCapacity < required) {
            val doubled = nextCapacity.toLong() * 2
            nextCapacity = if (doubled <= Int.MAX_VALUE) doubled.toInt() else required
        }
        bytes = bytes.copyOf(nextCapacity.coerceAtMost(limits.maxDecompressedBytesAsInt()))
    }
}

internal class BoundedCompressedByteArraySink(private val limits: CompressionLimits) {
    private var bytes = ByteArray(0)
    private var size = 0

    fun append(chunk: ByteArray, length: Int) {
        if (length <= 0) return
        val nextSize = checkedCompressedSize(size, length, limits)
        ensureCapacity(nextSize)
        chunk.copyInto(
            destination = bytes,
            destinationOffset = size,
            startIndex = 0,
            endIndex = length,
        )
        size = nextSize
    }

    fun toByteString(): ByteString = ByteString(bytes, startIndex = 0, endIndex = size)

    private fun ensureCapacity(required: Int) {
        if (required <= bytes.size) return

        var nextCapacity = if (bytes.isEmpty()) INITIAL_OUTPUT_CAPACITY else bytes.size
        while (nextCapacity < required) {
            val doubled = nextCapacity.toLong() * 2
            nextCapacity = if (doubled <= Int.MAX_VALUE) doubled.toInt() else required
        }
        bytes = bytes.copyOf(nextCapacity.coerceAtMost(limits.maxCompressedBytesAsInt()))
    }
}

internal fun checkedCompressedSize(
    current: Int,
    nextChunk: Int,
    limits: CompressionLimits,
): Int {
    val maxCompressedBytes = limits.maxCompressedBytesAsInt()
    if (nextChunk < 0 || current > maxCompressedBytes - nextChunk) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "Compressed length exceeds limit ${limits.maxCompressedBytes}.",
        )
    }
    return current + nextChunk
}

internal fun checkedDecompressedSize(
    current: Int,
    nextChunk: Int,
    limits: DecompressionLimits,
): Int {
    val maxDecompressedBytes = limits.maxDecompressedBytesAsInt()
    if (nextChunk < 0 || current > maxDecompressedBytes - nextChunk) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "Decompressed length exceeds limit ${limits.maxDecompressedBytes}.",
        )
    }
    return current + nextChunk
}

internal fun decompressionFailed(message: String, cause: Throwable? = null): Nothing =
    throw pmTilesException(PmTilesErrorCode.DecompressionFailed, message, cause)

internal fun compressionFailed(message: String, cause: Throwable? = null): Nothing =
    throw pmTilesException(PmTilesErrorCode.CompressionFailed, message, cause)

internal val DecodePurpose.displayName: String
    get() =
        when (this) {
            DecodePurpose.RootDirectory -> "Root directory"
            DecodePurpose.LeafDirectory -> "Leaf directory"
            DecodePurpose.Metadata -> "Metadata"
            DecodePurpose.Tile -> "Tile"
        }

private const val INITIAL_OUTPUT_CAPACITY = 8 * 1024

private fun DecompressionLimits.maxDecompressedBytesAsInt(): Int {
    if (maxDecompressedBytes > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "Decompressed byte limit $maxDecompressedBytes exceeds the supported Int range.",
        )
    }
    return maxDecompressedBytes.toInt()
}

private fun CompressionLimits.maxCompressedBytesAsInt(): Int {
    if (maxCompressedBytes > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "Compressed byte limit $maxCompressedBytes exceeds the supported Int range.",
        )
    }
    return maxCompressedBytes.toInt()
}
