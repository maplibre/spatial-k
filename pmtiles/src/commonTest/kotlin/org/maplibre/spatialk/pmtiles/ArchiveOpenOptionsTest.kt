package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArchiveOpenOptionsTest {
    @Test
    fun tileReadCoalescingCopyUpdatesSelectedFields() {
        val coalescing = TileReadCoalescing().copy(maxGapBytes = 64uL, maxCoalescedBytes = 2048uL)

        assertEquals(2048uL, coalescing.maxCoalescedBytes)
        assertEquals(64uL, coalescing.maxGapBytes)
        assertEquals(
            coalescing,
            TileReadCoalescing(maxCoalescedBytes = 2048uL, maxGapBytes = 64uL),
        )
    }

    @Test
    fun archiveLimitsCopyUpdatesSelectedFields() {
        val limits =
            ArchiveLimits()
                .copy(
                    maxMetadataBytes = 1024uL,
                    maxDirectoryDecompressedBytes = 2048uL,
                    maxDirectoryEntries = 2048 / 17,
                    maxTileCompressedBytes = 4096uL,
                    maxDirectoryDepth = 1,
                )

        assertEquals(1024uL, limits.maxMetadataBytes)
        assertEquals(2048uL, limits.maxDirectoryDecompressedBytes)
        assertEquals(2048 / 17, limits.maxDirectoryEntries)
        assertEquals(4096uL, limits.maxTileCompressedBytes)
        assertEquals(1, limits.maxDirectoryDepth)
        assertEquals(ArchiveLimits().maxInitialReadBytes, limits.maxInitialReadBytes)
    }

    @Test
    fun archiveLimitsCopyPreservesExplicitEntryLimit() {
        val limits =
            ArchiveLimits().copy(maxDirectoryEntries = 16, maxDirectoryDecompressedBytes = 2048uL)

        assertEquals(2048uL, limits.maxDirectoryDecompressedBytes)
        assertEquals(16, limits.maxDirectoryEntries)
    }

    @Test
    fun copyUpdatesValidationAndLimits() {
        val limits = ArchiveLimits().copy(maxTileCompressedBytes = 1uL)

        val validationOnly = ArchiveOpenOptions().copy(validationMode = ValidationMode.Lenient)
        val limitsOnly = ArchiveOpenOptions().copy(limits = limits)
        val combined =
            ArchiveOpenOptions().copy(validationMode = ValidationMode.Lenient, limits = limits)

        assertEquals(ValidationMode.Lenient, validationOnly.validationMode)
        assertEquals(ArchiveLimits(), validationOnly.limits)
        assertEquals(ValidationMode.Strict, limitsOnly.validationMode)
        assertEquals(limits, limitsOnly.limits)
        assertEquals(ValidationMode.Lenient, combined.validationMode)
        assertEquals(limits, combined.limits)
        assertEquals(
            ArchiveOpenOptions(validationMode = ValidationMode.Lenient, limits = limits),
            combined,
        )
    }

    @Test
    fun copyRemainsAvailableToKotlinCallers() {
        val limits = ArchiveLimits().copy(maxTileCompressedBytes = 1uL)
        val options =
            ArchiveOpenOptions().copy(validationMode = ValidationMode.Lenient, limits = limits)

        assertEquals(ValidationMode.Lenient, options.validationMode)
        assertEquals(limits, options.limits)
    }

    @Test
    fun copyPreservesCustomDecompressors() {
        val options =
            ArchiveOpenOptions()
                .withDecompressor(CompressionCodes.Brotli) { bytes, _ -> bytes }
                .copy(validationMode = ValidationMode.Lenient)

        assertTrue(CompressionCodes.Brotli in options.decompressors)
    }
}
