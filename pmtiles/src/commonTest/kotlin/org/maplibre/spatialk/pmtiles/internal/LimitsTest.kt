package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.ArchiveLimits
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException

class LimitsTest {
    @Test
    fun computesCheckedEndOffsets() {
        assertEquals(15uL, ArchiveSection(10uL, 5uL).endOffset(PmTilesErrorCodes.InvalidHeader))
        assertEquals(15uL, ByteRange(10uL, 5uL).endOffset(PmTilesErrorCodes.RangeOutOfBounds))
    }

    @Test
    fun rejectsUnsignedAdditionOverflow() {
        val error =
            assertFailsWith<PmTilesException> {
                checkedAdd(ULong.MAX_VALUE, 1uL, PmTilesErrorCodes.InvalidSectionLayout)
            }

        assertEquals(PmTilesErrorCodes.InvalidSectionLayout, error.code)
    }

    @Test
    fun rejectsReadRangeBeyondArchiveSize() {
        val error =
            assertFailsWith<PmTilesException> {
                validateReadRange(ByteRange(8uL, 3uL), archiveSize = 10uL, maxBytes = 3uL)
            }

        assertEquals(PmTilesErrorCodes.RangeOutOfBounds, error.code)
    }

    @Test
    fun rejectsReadRangeBeyondAllocationLimit() {
        val error =
            assertFailsWith<PmTilesException> {
                validateReadRange(ByteRange(0uL, 4uL), archiveSize = 10uL, maxBytes = 3uL)
            }

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
    }

    @Test
    fun rejectsInvalidArchiveLimitConfiguration() {
        assertFailsWith<IllegalArgumentException> {
            ArchiveLimits.build { maxInitialReadBytes = (16 * 1024 - 1).toULong() }
        }
        assertFailsWith<IllegalArgumentException> {
            ArchiveLimits.build { maxDirectoryDecompressedBytes = 0uL }
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
    fun copiedLimitsRecomputeDerivedDirectoryEntryLimitForLargerDirectoryBudget() {
        val maxDirectoryDecompressedBytes = (32 * 1024 * 1024).toULong()
        val limits =
            ArchiveLimits()
                .toBuilder()
                .apply { this.maxDirectoryDecompressedBytes = maxDirectoryDecompressedBytes }
                .build()

        assertEquals((32 * 1024 * 1024) / 17, limits.maxDirectoryEntries)
    }
}
