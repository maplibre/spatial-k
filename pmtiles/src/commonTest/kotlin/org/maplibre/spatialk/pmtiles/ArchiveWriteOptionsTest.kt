package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.maplibre.spatialk.pmtiles.internal.effectiveCompressors

class ArchiveWriteOptionsTest {
    @Test
    fun archiveWriteLimitsBuilderUpdatesSelectedFields() {
        val limits = ArchiveWriteLimits.build {
            maxRootDirectoryBytes = 1024uL
            maxMetadataBytes = 2048uL
            maxDirectoryBytes = 4096uL
            maxTileBytes = 8192uL
            maxArchiveBytes = 16_384uL
        }

        assertEquals(1024uL, limits.maxRootDirectoryBytes)
        assertEquals(2048uL, limits.maxMetadataBytes)
        assertEquals(4096uL, limits.maxDirectoryBytes)
        assertEquals(8192uL, limits.maxTileBytes)
        assertEquals(16_384uL, limits.maxArchiveBytes)
        assertEquals(1024uL, limits.toBuilder().build().maxRootDirectoryBytes)
    }

    @Test
    fun archiveWriteLimitsRejectInvalidValues() {
        assertFailsWith<IllegalArgumentException> {
            ArchiveWriteLimits.build { maxRootDirectoryBytes = 0uL }
        }
        assertFailsWith<IllegalArgumentException> {
            ArchiveWriteLimits.build { maxRootDirectoryBytes = 16_258uL }
        }
        assertFailsWith<IllegalArgumentException> {
            ArchiveWriteLimits.build { maxArchiveBytes = 0uL }
        }
    }

    @Test
    fun archiveWriteOptionsBuilderUpdatesSelectedFields() {
        val limits = ArchiveWriteLimits.build { maxTileBytes = 1uL }
        val options = ArchiveWriteOptions.build {
            internalCompression = CompressionCodes.Gzip
            tileCompression = CompressionCodes.Brotli
            this.limits = limits
            deduplicateTilePayloads = false
        }

        assertEquals(CompressionCodes.Gzip, options.internalCompression)
        assertEquals(CompressionCodes.Brotli, options.tileCompression)
        assertEquals(1uL, options.limits.maxTileBytes)
        assertEquals(false, options.deduplicateTilePayloads)
    }

    @Test
    fun builderRegistersCustomCompressors() {
        val options = ArchiveWriteOptions.build {
            internalCompression = CompressionCodes.Brotli
            compressor(CompressionCodes.Brotli) { bytes, _ -> bytes }
        }

        assertEquals(CompressionCodes.Brotli, options.internalCompression)
        assertTrue(CompressionCodes.Brotli in options.compressors)
    }

    @Test
    fun toBuilderStartsFromExistingOptions() {
        val seeded = ArchiveWriteOptions.build {
            tileCompression = CompressionCodes.Gzip
            deduplicateTilePayloads = false
            compressor(CompressionCodes.Gzip) { bytes, _ -> bytes }
        }
        val options = seeded.toBuilder().build()

        assertEquals(CompressionCodes.Gzip, options.tileCompression)
        assertEquals(false, options.deduplicateTilePayloads)
        assertTrue(CompressionCodes.Gzip in options.compressors)
    }

    @Test
    fun effectiveCompressorsIncludeNoneAndCustomEntries() {
        val options = ArchiveWriteOptions.build {
            compressor(CompressionCodes.Brotli) { bytes, _ -> bytes }
        }

        val compressors = options.effectiveCompressors()

        assertTrue(CompressionCodes.None in compressors)
        assertTrue(CompressionCodes.Brotli in compressors)
    }
}
