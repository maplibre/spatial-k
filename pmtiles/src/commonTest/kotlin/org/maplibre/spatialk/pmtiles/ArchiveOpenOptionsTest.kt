package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArchiveOpenOptionsTest {
    @Test
    fun tileReadCoalescingWithMethodsUpdateSelectedFields() {
        val coalescing = TileReadCoalescing().withMaxGapBytes(64uL).withMaxCoalescedBytes(2048uL)

        assertEquals(2048uL, coalescing.maxCoalescedBytes)
        assertEquals(64uL, coalescing.maxGapBytes)
        assertEquals(
            coalescing,
            TileReadCoalescing(maxCoalescedBytes = 2048uL, maxGapBytes = 64uL),
        )
    }

    @Test
    fun archiveLimitsWithMethodsUpdateSelectedFields() {
        val limits =
            ArchiveLimits()
                .withMaxMetadataBytes(1024uL)
                .withMaxDirectoryDecompressedBytes(2048uL)
                .withMaxTileCompressedBytes(4096uL)
                .withMaxDirectoryDepth(1)

        assertEquals(1024uL, limits.maxMetadataBytes)
        assertEquals(2048uL, limits.maxDirectoryDecompressedBytes)
        assertEquals(2048 / 17, limits.maxDirectoryEntries)
        assertEquals(4096uL, limits.maxTileCompressedBytes)
        assertEquals(1, limits.maxDirectoryDepth)
        assertEquals(ArchiveLimits().maxInitialReadBytes, limits.maxInitialReadBytes)
    }

    @Test
    fun archiveLimitsWithDirectoryBytesPreservesCompatibleEntryLimit() {
        val limits =
            ArchiveLimits().withMaxDirectoryEntries(16).withMaxDirectoryDecompressedBytes(2048uL)

        assertEquals(2048uL, limits.maxDirectoryDecompressedBytes)
        assertEquals(16, limits.maxDirectoryEntries)
    }

    @Test
    fun withMethodsUpdateValidationAndLimits() {
        val limits = ArchiveLimits().copy(maxTileCompressedBytes = 1uL)

        val validationOnly = ArchiveOpenOptions().with(ValidationMode.Lenient)
        val limitsOnly = ArchiveOpenOptions().with(limits)
        val combined = ArchiveOpenOptions().with(ValidationMode.Lenient, limits)

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
    fun withMethodsPreserveCustomDecompressors() {
        val options =
            ArchiveOpenOptions()
                .withDecompressor(KnownCompression.Brotli) { bytes, _ -> bytes }
                .with(ValidationMode.Lenient)

        assertTrue(Compression(KnownCompression.Brotli) in options.decompressors)
    }
}
