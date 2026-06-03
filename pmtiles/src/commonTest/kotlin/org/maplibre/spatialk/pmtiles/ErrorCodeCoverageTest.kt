package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorCodeCoverageTest {
    @Test
    fun everyErrorCodeHasCoverageOrReservedStatus() {
        val emittedByConcreteFailureTests =
            setOf(
                PmTilesErrorCode.InvalidMagic,
                PmTilesErrorCode.UnsupportedVersion,
                PmTilesErrorCode.InvalidHeader,
                PmTilesErrorCode.InvalidSectionLayout,
                PmTilesErrorCode.InvalidRootDirectoryLocation,
                PmTilesErrorCode.InvalidDirectory,
                PmTilesErrorCode.InvalidVarint,
                PmTilesErrorCode.InvalidTileCoordinate,
                PmTilesErrorCode.UnsupportedCompression,
                PmTilesErrorCode.DecompressionFailed,
                PmTilesErrorCode.InvalidMetadata,
                PmTilesErrorCode.RangeOutOfBounds,
                PmTilesErrorCode.SourceChanged,
                PmTilesErrorCode.SourceUnavailable,
                PmTilesErrorCode.LimitExceeded,
                PmTilesErrorCode.Closed,
            )
        val reservedBySpec =
            setOf(
                PmTilesErrorCode.Cancelled,
                PmTilesErrorCode.InternalError,
            )

        assertEquals(
            PmTilesErrorCode.entries.toSet(),
            emittedByConcreteFailureTests + reservedBySpec,
        )
    }
}
