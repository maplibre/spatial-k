package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.ArchiveLimits
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

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
                validateReadRange(ByteRange(0uL, -1), archiveSize = 1uL, maxBytes = 1uL)
            }

        assertEquals(PmTilesErrorCode.RangeOutOfBounds, error.code)
    }

    @Test
    fun rejectsReadRangeBeyondArchiveSize() {
        val error =
            assertFailsWith<PmTilesException> {
                validateReadRange(ByteRange(8uL, 3), archiveSize = 10uL, maxBytes = 3uL)
            }

        assertEquals(PmTilesErrorCode.RangeOutOfBounds, error.code)
    }

    @Test
    fun rejectsReadRangeBeyondAllocationLimit() {
        val error =
            assertFailsWith<PmTilesException> {
                validateReadRange(ByteRange(0uL, 4), archiveSize = 10uL, maxBytes = 3uL)
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun rejectsInvalidArchiveLimitConfiguration() {
        assertFailsWith<IllegalArgumentException> {
            ArchiveLimits.build { maxInitialReadBytes = (16 * 1024 - 1).toULong() }
        }
        assertFailsWith<IllegalArgumentException> {
            ArchiveLimits.build {
                maxDirectoryDecompressedBytes = 1024uL
                maxDirectoryEntries = 256
            }
        }
        assertFailsWith<IllegalArgumentException> {
            ArchiveLimits.build { maxDirectoryEntries = -1 }
        }
        assertFailsWith<IllegalArgumentException> {
            ArchiveLimits.build { maxDirectoryEntries = Int.MAX_VALUE }
        }
        assertFailsWith<IllegalArgumentException> {
            ArchiveLimits.build { maxVarintBytes = 0 }
        }
    }

    @Test
    fun recomputesDefaultDirectoryEntryLimitForSmallerDirectoryBudget() {
        val limits = ArchiveLimits.build { maxDirectoryDecompressedBytes = 1024uL }

        assertEquals(1024 / 17, limits.maxDirectoryEntries)
    }

    @Test
    fun acceptsDirectoryEntryLimitThatFitsDecompressedDirectoryBudget() {
        val limits = ArchiveLimits.build {
            maxDirectoryDecompressedBytes = 1024uL
            maxDirectoryEntries = 255
        }

        assertEquals(255, limits.maxDirectoryEntries)
    }
}
