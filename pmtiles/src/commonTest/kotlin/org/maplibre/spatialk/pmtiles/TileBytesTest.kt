package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.internal.DirectoryEntry
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchiveWithSections
import org.maplibre.spatialk.pmtiles.internal.buildSingleTileArchive
import org.maplibre.spatialk.pmtiles.internal.encodeDirectory
import org.maplibre.spatialk.pmtiles.internal.helloBytes
import org.maplibre.spatialk.pmtiles.internal.helloGzipBytes

class TileBytesTest {
    @Test
    fun readsCompressedTileBytesExactly() = runTest {
        val tileBytes = byteArrayOf(1, 2, 3, 4)
        val source =
            TestByteRangeSource(
                buildSingleTileArchive(
                    tileBytes = tileBytes,
                    tileCompression = Compression.Brotli.code,
                    tileType = TileType.Png.code,
                )
            )
        val archive = PmTilesArchive.open(source)

        val compressed = archive.getTileCompressed(0, 0, 0)
        val defaultRead = archive.getTile(0, 0, 0)
        val coordRead = archive.getTile(TileCoord(0, 0, 0))
        val idRead = archive.getTileById(0)

        requireNotNull(compressed)
        requireNotNull(defaultRead)
        requireNotNull(coordRead)
        requireNotNull(idRead)
        assertContentEquals(tileBytes, compressed.bytes)
        assertEquals(Compression.Brotli, compressed.compression)
        assertEquals(false, compressed.wasDecompressed)
        assertEquals(TileType.Png, compressed.tileType)
        assertContentEquals(tileBytes, defaultRead.bytes)
        assertContentEquals(tileBytes, coordRead.bytes)
        assertContentEquals(tileBytes, idRead.bytes)
    }

    @Test
    fun batchReadCoalescesContiguousTilesAndPreservesInputOrder() = runTest {
        val tileData = byteArrayOf(1, 2, 3, 4, 5)
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                DirectoryEntry(tileId = 1, offset = 2uL, length = 3, runLength = 1),
            )
        val tileDataOffset = 20_000uL
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                tileDataOffset = tileDataOffset,
                tileDataLength = tileData.size.toULong(),
                clustered = 1u,
            )
        val source =
            TestByteRangeSource(
                buildArchiveWithSections(
                    fields = fields,
                    rootBytes = rootBytes,
                    tileBytes = tileData,
                    minimumArchiveSize = tileDataOffset + tileData.size.toULong(),
                )
            )
        val archive = PmTilesArchive.open(source)
        source.reads.clear()

        val tiles =
            archive.getTiles(
                listOf(
                    TileIds.toZxy(1),
                    TileIds.toZxy(2),
                    TileIds.toZxy(0),
                )
            )

        assertEquals(3, tiles.size)
        assertContentEquals(byteArrayOf(3, 4, 5), tiles[0]?.bytes)
        assertNull(tiles[1])
        assertContentEquals(byteArrayOf(1, 2), tiles[2]?.bytes)
        assertEquals(listOf(ByteRange(tileDataOffset, tileData.size)), source.reads)
    }

    @Test
    fun batchReadOnlyCoalescesAcrossGapsWhenConfigured() = runTest {
        val tileData = byteArrayOf(1, 2, 0, 0, 0, 6, 7)
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                DirectoryEntry(tileId = 1, offset = 5uL, length = 2, runLength = 1),
            )
        val tileDataOffset = 20_000uL
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                tileDataOffset = tileDataOffset,
                tileDataLength = tileData.size.toULong(),
                clustered = 1u,
            )
        val source =
            TestByteRangeSource(
                buildArchiveWithSections(
                    fields = fields,
                    rootBytes = rootBytes,
                    tileBytes = tileData,
                    minimumArchiveSize = tileDataOffset + tileData.size.toULong(),
                )
            )
        val archive = PmTilesArchive.open(source)
        val coords = listOf(TileIds.toZxy(0), TileIds.toZxy(1))
        source.reads.clear()

        archive.getTiles(coords)

        assertEquals(
            listOf(
                ByteRange(tileDataOffset, 2),
                ByteRange(tileDataOffset + 5uL, 2),
            ),
            source.reads,
        )

        source.reads.clear()
        archive.getTiles(
            coords,
            coalescing = TileReadCoalescing(maxCoalescedBytes = 16, maxGapBytes = 3),
        )

        assertEquals(listOf(ByteRange(tileDataOffset, tileData.size)), source.reads)
    }

    @Test
    fun batchReadSplitsContiguousTilesOverMaxCoalescedBytes() = runTest {
        val tileData = byteArrayOf(1, 2, 3, 4, 5)
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                DirectoryEntry(tileId = 1, offset = 2uL, length = 3, runLength = 1),
            )
        val tileDataOffset = 20_000uL
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                tileDataOffset = tileDataOffset,
                tileDataLength = tileData.size.toULong(),
                clustered = 1u,
            )
        val source =
            TestByteRangeSource(
                buildArchiveWithSections(
                    fields = fields,
                    rootBytes = rootBytes,
                    tileBytes = tileData,
                    minimumArchiveSize = tileDataOffset + tileData.size.toULong(),
                )
            )
        val archive = PmTilesArchive.open(source)
        source.reads.clear()

        archive.getTiles(
            listOf(TileIds.toZxy(0), TileIds.toZxy(1)),
            coalescing = TileReadCoalescing(maxCoalescedBytes = 4),
        )

        assertEquals(
            listOf(
                ByteRange(tileDataOffset, 2),
                ByteRange(tileDataOffset + 2uL, 3),
            ),
            source.reads,
        )
    }

    @Test
    fun batchDecompressedReadDecodesEachTile() = runTest {
        val compressedTileData = byteArrayOf(1, 2, 3, 4)
        val rootBytes =
            encodeDirectory(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                DirectoryEntry(tileId = 1, offset = 2uL, length = 2, runLength = 1),
            )
        val tileDataOffset = 20_000uL
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                tileDataOffset = tileDataOffset,
                tileDataLength = compressedTileData.size.toULong(),
                tileCompression = Compression.Brotli.code,
                clustered = 1u,
            )
        val source =
            TestByteRangeSource(
                buildArchiveWithSections(
                    fields = fields,
                    rootBytes = rootBytes,
                    tileBytes = compressedTileData,
                    minimumArchiveSize = tileDataOffset + compressedTileData.size.toULong(),
                )
            )
        val archive =
            PmTilesArchive.open(
                source,
                options =
                    ArchiveOpenOptions().withDecompressor(Compression.Brotli) { bytes, _ ->
                        bytes.map { (it.toInt() + 10).toByte() }.toByteArray()
                    },
            )
        source.reads.clear()

        val tiles = archive.getTilesDecompressed(listOf(TileIds.toZxy(0), TileIds.toZxy(1)))

        assertContentEquals(byteArrayOf(11, 12), tiles[0]?.bytes)
        assertContentEquals(byteArrayOf(13, 14), tiles[1]?.bytes)
        assertEquals(Compression.None, tiles[0]?.compression)
        assertEquals(true, tiles[0]?.wasDecompressed)
        assertEquals(listOf(ByteRange(tileDataOffset, compressedTileData.size)), source.reads)
    }

    @Test
    fun rejectsInvalidTileReadCoalescing() {
        assertFailsWith<IllegalArgumentException> {
            TileReadCoalescing(maxCoalescedBytes = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            TileReadCoalescing(maxGapBytes = -1)
        }
    }

    @Test
    fun decompressedReadLeavesNoneCompressedTilesUnchanged() = runTest {
        val tileBytes = byteArrayOf(5, 6, 7)
        val archive =
            PmTilesArchive.open(TestByteRangeSource(buildSingleTileArchive(tileBytes = tileBytes)))

        val tile = archive.getTileDecompressed(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(tileBytes, tile.bytes)
        assertEquals(Compression.None, tile.compression)
        assertEquals(false, tile.wasDecompressed)
    }

    @Test
    fun decompressedReadDecodesGzipTiles() = runTest {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    buildSingleTileArchive(
                        tileBytes = helloGzipBytes,
                        tileCompression = Compression.Gzip.code,
                    )
                )
            )

        val tile = archive.getTileDecompressed(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(helloBytes, tile.bytes)
        assertEquals(Compression.None, tile.compression)
        assertEquals(Compression.Gzip, tile.range.compression)
        assertEquals(true, tile.wasDecompressed)
    }

    @Test
    fun returnsNullForMissingTilePayload() = runTest {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(buildSingleTileArchive(tileBytes = byteArrayOf(1)))
            )
        val missingCoord = TileIds.toZxy(1)

        assertNull(archive.getTile(missingCoord.z, missingCoord.x, missingCoord.y))
        assertNull(archive.getTileCompressed(missingCoord.z, missingCoord.x, missingCoord.y))
    }

    @Test
    fun unsupportedTileCompressionFailsOnlyWhenDecompressionIsRequested() = runTest {
        val tileBytes = byteArrayOf(8, 9, 10)
        val bytes =
            buildSingleTileArchive(
                tileBytes = tileBytes,
                tileCompression = Compression.Brotli.code,
            )

        val archive = PmTilesArchive.open(TestByteRangeSource(bytes))
        assertContentEquals(tileBytes, archive.getTileCompressed(0, 0, 0)?.bytes)

        val error =
            assertFailsWith<PmTilesException> {
                archive.getTileDecompressed(0, 0, 0)
            }

        assertEquals(PmTilesErrorCode.UnsupportedCompression, error.code)
    }

    @Test
    fun customDecompressorDecodesTiles() = runTest {
        val compressedBytes = byteArrayOf(1, 2, 3)
        val decompressedBytes = byteArrayOf(4, 5, 6)
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    buildSingleTileArchive(
                        tileBytes = compressedBytes,
                        tileCompression = Compression.Brotli.code,
                    )
                ),
                options =
                    ArchiveOpenOptions()
                        .withDecompressor(
                            Compression.Brotli,
                            Decompressor { bytes, _ ->
                                assertContentEquals(compressedBytes, bytes)
                                decompressedBytes
                            },
                        ),
            )

        val tile = archive.getTileDecompressed(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(decompressedBytes, tile.bytes)
        assertEquals(Compression.None, tile.compression)
        assertEquals(true, tile.wasDecompressed)
    }

    @Test
    fun enforcesCompressedTileByteLimit() = runTest {
        val tileBytes = byteArrayOf(11, 12, 13)
        val error =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(
                    TestByteRangeSource(buildSingleTileArchive(tileBytes = tileBytes)),
                    options =
                        ArchiveOpenOptions(
                            limits =
                                ArchiveLimits.Default.copy(
                                    maxTileCompressedBytes = tileBytes.size - 1
                                )
                        ),
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun compressedBombFails() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTilesArchive.open(
                        TestByteRangeSource(
                            buildSingleTileArchive(
                                tileBytes = helloGzipBytes,
                                tileCompression = Compression.Gzip.code,
                            )
                        ),
                        options =
                            ArchiveOpenOptions(
                                limits =
                                    ArchiveLimits.Default.copy(
                                        maxTileDecompressedBytes = helloBytes.size - 1
                                    )
                            ),
                    )
                archive.getTileDecompressed(0, 0, 0)
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }
}
