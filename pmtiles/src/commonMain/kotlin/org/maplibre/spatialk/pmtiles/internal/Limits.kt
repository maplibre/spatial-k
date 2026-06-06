package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

internal data class DecodeLimits(
    val maxCompressedBytes: ULong,
    val maxDecompressedBytes: ULong,
    val purpose: DecodePurpose,
)

internal enum class DecodePurpose {
    RootDirectory,
    LeafDirectory,
    Metadata,
    Tile,
}

internal fun pmTilesException(
    code: PmTilesErrorCode,
    message: String,
    cause: Throwable? = null,
): PmTilesException = PmTilesException(code, message, cause)

internal fun checkedAdd(left: ULong, right: ULong, errorCode: PmTilesErrorCode): ULong {
    val result = left + right
    if (result < left) {
        throw pmTilesException(errorCode, "Unsigned 64-bit addition overflow.")
    }
    return result
}

internal fun ArchiveSection.endOffset(errorCode: PmTilesErrorCode): ULong =
    checkedAdd(offset, length, errorCode)

internal fun ByteRange.endOffset(errorCode: PmTilesErrorCode): ULong =
    checkedAdd(offset, length, errorCode)

internal fun ArchiveSection.toByteRange(maxBytes: ULong, purpose: String): ByteRange =
    ByteRange(offset = offset, length = allocationLength(length, maxBytes, purpose).toULong())

internal fun allocationLength(length: ULong, maxBytes: ULong, purpose: String): Int {
    if (length > maxBytes) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "$purpose length $length exceeds limit $maxBytes.",
        )
    }
    if (length > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "$purpose length $length exceeds the supported allocation limit ${Int.MAX_VALUE}.",
        )
    }
    return length.toInt()
}

internal fun validateReadRange(range: ByteRange, archiveSize: ULong, maxBytes: ULong) {
    allocationLength(range.length, maxBytes, "Read range")
    val end = range.endOffset(PmTilesErrorCode.RangeOutOfBounds)
    if (end > archiveSize) {
        throw pmTilesException(
            PmTilesErrorCode.RangeOutOfBounds,
            "Read range $range exceeds archive size $archiveSize.",
        )
    }
}
