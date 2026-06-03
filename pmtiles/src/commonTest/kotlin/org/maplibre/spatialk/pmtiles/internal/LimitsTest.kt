package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.ArchiveOpenOptions
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException
import org.maplibre.spatialk.pmtiles.TileReadMode
import org.maplibre.spatialk.pmtiles.ValidationMode

class LimitsTest {
    @Test
    fun computesCheckedEndOffsets() {
        assertEquals(15uL, ArchiveSection(10uL, 5uL).endOffset(PmTilesErrorCode.InvalidHeader))
        assertEquals(15uL, ByteRange(10uL, 5).endOffset(PmTilesErrorCode.RangeOutOfBounds))
    }

    @Test
    fun rejectsUnsignedAdditionOverflow() {
        val error =
            assertFailsWith<PmTilesException> {
                checkedAdd(ULong.MAX_VALUE, 1uL, PmTilesErrorCode.InvalidSectionLayout)
            }

        assertEquals(PmTilesErrorCode.InvalidSectionLayout, error.code)
    }

    @Test
    fun rejectsNegativeRangeLength() {
        val error =
            assertFailsWith<PmTilesException> {
                validateReadRange(ByteRange(0uL, -1), archiveSize = 1uL, maxBytes = 1)
            }

        assertEquals(PmTilesErrorCode.RangeOutOfBounds, error.code)
    }

    @Test
    fun rejectsReadRangeBeyondArchiveSize() {
        val error =
            assertFailsWith<PmTilesException> {
                validateReadRange(ByteRange(8uL, 3), archiveSize = 10uL, maxBytes = 3)
            }

        assertEquals(PmTilesErrorCode.RangeOutOfBounds, error.code)
    }

    @Test
    fun rejectsReadRangeBeyondAllocationLimit() {
        val error =
            assertFailsWith<PmTilesException> {
                validateReadRange(ByteRange(0uL, 4), archiveSize = 10uL, maxBytes = 3)
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun convertsSectionToBoundedByteRange() {
        assertEquals(ByteRange(7uL, 3), ArchiveSection(7uL, 3uL).toByteRange(3, "test"))
    }

    @Test
    fun defaultOptionsUseStrictCompressedReads() {
        assertEquals(ValidationMode.Strict, ArchiveOpenOptions.Default.validationMode)
        assertEquals(TileReadMode.CompressedBytes, ArchiveOpenOptions.Default.tileReadMode)
    }

    @Test
    fun lenientOptionsOnlySwitchValidationMode() {
        assertEquals(ValidationMode.Lenient, ArchiveOpenOptions.Lenient.validationMode)
        assertEquals(
            ArchiveOpenOptions.Default.tileReadMode,
            ArchiveOpenOptions.Lenient.tileReadMode,
        )
        assertEquals(ArchiveOpenOptions.Default.limits, ArchiveOpenOptions.Lenient.limits)
    }
}
