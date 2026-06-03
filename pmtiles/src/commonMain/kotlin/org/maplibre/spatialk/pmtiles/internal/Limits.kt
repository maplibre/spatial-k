package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

internal data class DecodeLimits(
    val maxCompressedBytes: Int,
    val maxDecompressedBytes: Int,
)

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

internal fun ByteRange.endOffset(errorCode: PmTilesErrorCode): ULong {
    if (length < 0) {
        throw pmTilesException(errorCode, "Byte range length $length is negative.")
    }
    return checkedAdd(offset, length.toULong(), errorCode)
}

internal fun ArchiveSection.toByteRange(maxBytes: Int, purpose: String): ByteRange =
    ByteRange(offset = offset, length = allocationLength(length, maxBytes, purpose))

internal fun allocationLength(length: ULong, maxBytes: Int, purpose: String): Int {
    if (maxBytes < 0) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "Configured byte limit for $purpose is negative.",
        )
    }
    if (length > maxBytes.toULong()) {
        throw pmTilesException(
            PmTilesErrorCode.LimitExceeded,
            "$purpose length $length exceeds limit $maxBytes.",
        )
    }
    return length.toInt()
}

internal fun validateReadRange(range: ByteRange, archiveSize: ULong, maxBytes: Int) {
    allocationLength(range.length.toNonNegativeULong(), maxBytes, "Read range")
    val end = range.endOffset(PmTilesErrorCode.RangeOutOfBounds)
    if (end > archiveSize) {
        throw pmTilesException(
            PmTilesErrorCode.RangeOutOfBounds,
            "Read range $range exceeds archive size $archiveSize.",
        )
    }
}

private fun Int.toNonNegativeULong(): ULong {
    if (this < 0) {
        throw pmTilesException(
            PmTilesErrorCode.RangeOutOfBounds,
            "Read range length $this is negative.",
        )
    }
    return toULong()
}
