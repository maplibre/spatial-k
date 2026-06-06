package org.maplibre.spatialk.pmtiles.internal

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.ByteRangeSource
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

internal suspend fun ByteRangeSource.readSourceRange(
    range: ByteRange,
    archiveSize: ULong,
    maxBytes: ULong,
): ByteString {
    validateReadRange(range, archiveSize, maxBytes)
    val bytes =
        try {
            read(range)
        } catch (error: PmTilesException) {
            throw error
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            throw pmTilesException(
                PmTilesErrorCode.SourceUnavailable,
                "Byte range source could not read $range.",
                error,
            )
        }

    if (bytes.size.toULong() != range.length) {
        throw pmTilesException(
            PmTilesErrorCode.SourceUnavailable,
            "Byte range source returned ${bytes.size} bytes for requested length ${range.length}.",
        )
    }
    return bytes
}

internal suspend fun ByteRangeSource.sourceSize(): ULong =
    try {
        size()
    } catch (error: PmTilesException) {
        throw error
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        throw pmTilesException(
            PmTilesErrorCode.SourceUnavailable,
            "Byte range source could not report archive size.",
            error,
        )
    }
