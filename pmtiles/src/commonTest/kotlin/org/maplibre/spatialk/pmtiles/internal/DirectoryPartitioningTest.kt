package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveWriteLimits
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.Compressor
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException

class DirectoryPartitioningTest {
    @Test
    fun keepsDirectRootWhenCompressedRootFitsTarget() = runTest {
        val entries = tileEntries(3)
        val built = buildDirectories(entries, ArchiveWriteOptions())

        assertEquals(entries, built.rootEntries)
        assertEquals(encodeDirectory(entries), built.compressedRoot)
        assertEquals(emptyList(), built.compressedLeaves)
        assertEquals(0uL, built.leafDirectoriesLength)
    }

    @Test
    fun createsLeafDirectoryWhenDirectRootDoesNotFit() = runTest {
        val entries = tileEntries(10)
        val directRootSize = encodeDirectory(entries).size.toULong()
        val built =
            buildDirectories(
                entries,
                ArchiveWriteOptions.build {
                    limits = ArchiveWriteLimits.build {
                        maxRootDirectoryBytes = directRootSize - 1uL
                    }
                },
            )

        assertEquals(1, built.rootEntries.size)
        assertEquals(1, built.compressedLeaves.size)
        assertEquals(
            DirectoryEntry(0, 0uL, built.compressedLeaves.single().size, 0),
            built.rootEntries.single(),
        )
        assertEquals(encodeDirectory(entries), built.compressedLeaves.single())
        assertEquals(built.compressedLeaves.single().size.toULong(), built.leafDirectoriesLength)
        assertTrue(built.compressedRoot.size.toULong() < directRootSize)
    }

    @Test
    fun createsMultipleLeavesWithSectionRelativeOffsets() = runTest {
        val entries = tileEntries(4097)
        val directRootSize = encodeDirectory(entries).size.toULong()
        val built =
            buildDirectories(
                entries,
                ArchiveWriteOptions.build {
                    limits = ArchiveWriteLimits.build {
                        maxRootDirectoryBytes = minOf(16_257uL, directRootSize - 1uL)
                    }
                },
            )

        assertEquals(2, built.compressedLeaves.size)
        assertEquals(0uL, built.rootEntries[0].offset)
        assertEquals(built.compressedLeaves[0].size.toULong(), built.rootEntries[1].offset)
        assertEquals(
            built.compressedLeaves.sumOf { it.size.toULong() },
            built.leafDirectoriesLength,
        )
    }

    @Test
    fun partitionsBeforeRejectingOversizedUnpartitionedDirectory() = runTest {
        val entries = tileEntries(4097)
        val firstLeafBytes = encodeDirectory(entries.take(4096))
        val directRootSize = encodeDirectory(entries).size.toULong()
        assertTrue(directRootSize > firstLeafBytes.size.toULong())

        val built =
            buildDirectories(
                entries,
                ArchiveWriteOptions.build {
                    limits = ArchiveWriteLimits.build {
                        maxDirectoryBytes = firstLeafBytes.size.toULong()
                    }
                },
            )

        assertEquals(2, built.rootEntries.size)
        assertEquals(2, built.compressedLeaves.size)
        assertEquals(firstLeafBytes, built.compressedLeaves.first())
        assertEquals(
            built.compressedLeaves.sumOf { it.size.toULong() },
            built.leafDirectoriesLength,
        )
    }

    @Test
    fun failsWhenRootStillExceedsConfiguredTargetAfterLeafPartitioning() = runTest {
        val entries = tileEntries(10)
        val error =
            assertFailsWith<PmTilesException> {
                buildDirectories(
                    entries,
                    ArchiveWriteOptions.build {
                        limits = ArchiveWriteLimits.build { maxRootDirectoryBytes = 1uL }
                    },
                )
            }

        assertEquals(PmTilesErrorCodes.LimitExceeded, error.code)
    }

    @Test
    fun usesRegisteredCompressorForDirectories() = runTest {
        val compressed = ByteString(1, 2, 3)
        val built =
            buildDirectories(
                tileEntries(2),
                ArchiveWriteOptions.build {
                    internalCompression = CompressionCodes.Brotli
                    compressor(CompressionCodes.Brotli, Compressor { _, _ -> compressed })
                },
            )

        assertEquals(compressed, built.compressedRoot)
    }

    @Test
    fun rejectsUnsupportedInternalCompression() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                buildDirectories(
                    tileEntries(2),
                    ArchiveWriteOptions.build { internalCompression = CompressionCodes.Brotli },
                )
            }

        assertEquals(PmTilesErrorCodes.UnsupportedCompression, error.code)
    }

    private fun tileEntries(count: Int): List<DirectoryEntry> =
        List(count) { index ->
            DirectoryEntry(
                tileId = index.toLong(),
                offset = index.toULong(),
                length = 1,
                runLength = 1,
            )
        }
}
