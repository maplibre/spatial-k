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
    fun archiveLimitsBuilderUpdatesSelectedFields() {
        val limits = ArchiveLimits.build {
            maxMetadataBytes = 1024uL
            maxDirectoryDecompressedBytes = 2048uL
            maxDirectoryEntries = 2048 / 17
            maxTileCompressedBytes = 4096uL
            maxDirectoryDepth = 1
        }

        assertEquals(1024uL, limits.maxMetadataBytes)
        assertEquals(2048uL, limits.maxDirectoryDecompressedBytes)
        assertEquals(2048 / 17, limits.maxDirectoryEntries)
        assertEquals(4096uL, limits.maxTileCompressedBytes)
        assertEquals(1, limits.maxDirectoryDepth)
        assertEquals(ArchiveLimits().maxInitialReadBytes, limits.maxInitialReadBytes)
    }

    @Test
    fun archiveLimitsBuilderPreservesExplicitEntryLimit() {
        val limits = ArchiveLimits.build {
            maxDirectoryEntries = 16
            maxDirectoryDecompressedBytes = 2048uL
        }

        assertEquals(2048uL, limits.maxDirectoryDecompressedBytes)
        assertEquals(16, limits.maxDirectoryEntries)
    }

    @Test
    fun builderUpdatesValidationAndLimits() {
        val limits = ArchiveLimits.build { maxTileCompressedBytes = 1uL }

        val validationOnly = ArchiveOpenOptions.build { validationMode = ValidationMode.Lenient }
        val limitsOnly = ArchiveOpenOptions.build { this.limits = limits }
        val combined = ArchiveOpenOptions.build {
            validationMode = ValidationMode.Lenient
            this.limits = limits
        }

        assertEquals(ValidationMode.Lenient, validationOnly.validationMode)
        assertEquals(ArchiveLimits().maxInitialReadBytes, validationOnly.limits.maxInitialReadBytes)
        assertEquals(ValidationMode.Strict, limitsOnly.validationMode)
        assertEquals(1uL, limitsOnly.limits.maxTileCompressedBytes)
        assertEquals(ValidationMode.Lenient, combined.validationMode)
        assertEquals(1uL, combined.limits.maxTileCompressedBytes)
    }

    @Test
    fun builderCreatesOptions() {
        val limits = ArchiveLimits.build { maxTileCompressedBytes = 1uL }
        val options = ArchiveOpenOptions.build {
            validationMode = ValidationMode.Lenient
            this.limits = limits
        }

        assertEquals(ValidationMode.Lenient, options.validationMode)
        assertEquals(1uL, options.limits.maxTileCompressedBytes)
    }

    @Test
    fun builderRegistersCustomDecompressors() {
        val options = ArchiveOpenOptions.build {
            validationMode = ValidationMode.Lenient
            decompressor(CompressionCodes.Brotli) { bytes, _ -> bytes }
        }

        assertEquals(ValidationMode.Lenient, options.validationMode)
        assertTrue(CompressionCodes.Brotli in options.decompressors)
    }

    @Test
    fun toBuilderStartsFromExistingOptions() {
        val options =
            ArchiveOpenOptions()
                .toBuilder()
                .apply {
                    validationMode = ValidationMode.Lenient
                    decompressor(CompressionCodes.Brotli) { bytes, _ -> bytes }
                }
                .build()

        assertEquals(ValidationMode.Lenient, options.validationMode)
        assertTrue(CompressionCodes.Brotli in options.decompressors)
    }
}
