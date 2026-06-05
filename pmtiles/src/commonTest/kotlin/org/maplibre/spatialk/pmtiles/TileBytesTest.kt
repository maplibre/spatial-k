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
    fun readsStoredTileBytesExactly() = runTest {
        val tileBytes = byteArrayOf(1, 2, 3, 4)
        val source =
            TestByteRangeSource(
                buildSingleTileArchive(
                    tileBytes = tileBytes,
                    tileCompression = CompressionCodes.Brotli.code,
                    tileType = TileTypeCodes.Png.code,
                )
            )
        val archive = PmTiles.open(source)

        val stored = archive.readStoredTile(0, 0, 0)
        val coordRead = archive.readStoredTile(TileCoord(0, 0, 0))
        val idRead = archive.readStoredTile(0)

        requireNotNull(stored)
        requireNotNull(coordRead)
        requireNotNull(idRead)
        assertContentEquals(tileBytes, stored.payload.toByteArray())
        assertEquals(tileBytes.size.toULong(), stored.byteCount)
        assertEquals(CompressionCodes.Brotli, stored.compression)
        assertEquals(false, stored.wasDecompressed)
        assertEquals(TileTypeCodes.Png, stored.tileType)
        assertContentEquals(tileBytes, coordRead.payload.toByteArray())
        assertContentEquals(tileBytes, idRead.payload.toByteArray())
        assertEquals(stored, coordRead)
        assertEquals(stored, idRead)

        val mutableBytes = stored.payload.toByteArray()
        mutableBytes[0] = 9
        assertContentEquals(tileBytes, stored.payload.toByteArray())
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
        val archive = PmTiles.open(source)
        source.reads.clear()

        val tiles =
            archive.readStoredTiles(
                listOf(
                    TileIds.toZxy(1),
                    TileIds.toZxy(2),
                    TileIds.toZxy(0),
                )
            )

        assertEquals(3, tiles.size)
        assertContentEquals(byteArrayOf(3, 4, 5), tiles[0].tile?.payload?.toByteArray())
        assertEquals(false, tiles[1].isFound)
        assertContentEquals(byteArrayOf(1, 2), tiles[2].tile?.payload?.toByteArray())
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
        val archive = PmTiles.open(source)
        val coords = listOf(TileIds.toZxy(0), TileIds.toZxy(1))
        source.reads.clear()

        archive.readStoredTiles(coords)

        assertEquals(
            listOf(
                ByteRange(tileDataOffset, 2),
                ByteRange(tileDataOffset + 5uL, 2),
            ),
            source.reads,
        )

        source.reads.clear()
        archive.readStoredTiles(
            coords,
            coalescing = TileReadCoalescing(maxCoalescedBytes = 16uL, maxGapBytes = 3uL),
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
        val archive = PmTiles.open(source)
        source.reads.clear()

        archive.readStoredTiles(
            listOf(TileIds.toZxy(0), TileIds.toZxy(1)),
            coalescing = TileReadCoalescing(maxCoalescedBytes = 4uL),
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
                tileCompression = CompressionCodes.Brotli.code,
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
            PmTiles.open(
                source,
                options =
                    ArchiveOpenOptions.build {
                        decompressor(CompressionCodes.Brotli) { bytes, _ ->
                            bytes.map { (it.toInt() + 10).toByte() }.toByteArray()
                        }
                    },
            )
        source.reads.clear()

        val tiles = archive.readDecompressedTiles(listOf(TileIds.toZxy(0), TileIds.toZxy(1)))

        assertContentEquals(byteArrayOf(11, 12), tiles[0].tile?.payload?.toByteArray())
        assertContentEquals(byteArrayOf(13, 14), tiles[1].tile?.payload?.toByteArray())
        assertEquals(CompressionCodes.None, tiles[0].tile?.compression)
        assertEquals(true, tiles[0].tile?.wasDecompressed)
        assertEquals(listOf(ByteRange(tileDataOffset, compressedTileData.size)), source.reads)
    }

    @Test
    fun decompressedReadLeavesNoneCompressedTilesUnchanged() = runTest {
        val tileBytes = byteArrayOf(5, 6, 7)
        val archive =
            PmTiles.open(TestByteRangeSource(buildSingleTileArchive(tileBytes = tileBytes)))

        val tile = archive.readDecompressedTile(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(tileBytes, tile.payload.toByteArray())
        assertEquals(CompressionCodes.None, tile.compression)
        assertEquals(false, tile.wasDecompressed)
    }

    @Test
    fun decompressedReadDecodesGzipTiles() = runTest {
        val archive =
            PmTiles.open(
                TestByteRangeSource(
                    buildSingleTileArchive(
                        tileBytes = helloGzipBytes,
                        tileCompression = CompressionCodes.Gzip.code,
                    )
                )
            )

        val tile = archive.readDecompressedTile(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(helloBytes, tile.payload.toByteArray())
        assertEquals(CompressionCodes.None, tile.compression)
        assertEquals(CompressionCodes.Gzip, tile.range.compression)
        assertEquals(true, tile.wasDecompressed)
    }

    @Test
    fun returnsNullForMissingTilePayload() = runTest {
        val archive =
            PmTiles.open(TestByteRangeSource(buildSingleTileArchive(tileBytes = byteArrayOf(1))))
        val missingCoord = TileIds.toZxy(1)

        assertNull(archive.readStoredTile(missingCoord.z, missingCoord.x, missingCoord.y))
        assertNull(archive.readDecompressedTile(missingCoord.z, missingCoord.x, missingCoord.y))
    }

    @Test
    fun unsupportedTileCompressionFailsOnlyWhenDecompressionIsRequested() = runTest {
        val tileBytes = byteArrayOf(8, 9, 10)
        val bytes =
            buildSingleTileArchive(
                tileBytes = tileBytes,
                tileCompression = CompressionCodes.Brotli.code,
            )

        val archive = PmTiles.open(TestByteRangeSource(bytes))
        assertContentEquals(tileBytes, archive.readStoredTile(0, 0, 0)?.payload?.toByteArray())

        val error =
            assertFailsWith<PmTilesException> {
                archive.readDecompressedTile(0, 0, 0)
            }

        assertEquals(PmTilesErrorCode.UnsupportedCompression, error.code)
    }

    @Test
    fun customDecompressorDecodesTiles() = runTest {
        val compressedBytes = byteArrayOf(1, 2, 3)
        val decompressedBytes = byteArrayOf(4, 5, 6)
        val archive =
            PmTiles.open(
                TestByteRangeSource(
                    buildSingleTileArchive(
                        tileBytes = compressedBytes,
                        tileCompression = CompressionCodes.Brotli.code,
                    )
                ),
                options =
                    ArchiveOpenOptions.build {
                        decompressor(CompressionCodes.Brotli) { bytes, _ ->
                            assertContentEquals(compressedBytes, bytes)
                            decompressedBytes
                        }
                    },
            )

        val tile = archive.readDecompressedTile(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(decompressedBytes, tile.payload.toByteArray())
        assertEquals(CompressionCodes.None, tile.compression)
        assertEquals(true, tile.wasDecompressed)
    }

    @Test
    fun enforcesCompressedTileByteLimit() = runTest {
        val tileBytes = byteArrayOf(11, 12, 13)
        val error =
            assertFailsWith<PmTilesException> {
                PmTiles.open(
                    TestByteRangeSource(buildSingleTileArchive(tileBytes = tileBytes)),
                    options =
                        ArchiveOpenOptions.build {
                            limits = ArchiveLimits.build {
                                maxTileCompressedBytes = (tileBytes.size - 1).toULong()
                            }
                        },
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun compressedBombFails() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTiles.open(
                        TestByteRangeSource(
                            buildSingleTileArchive(
                                tileBytes = helloGzipBytes,
                                tileCompression = CompressionCodes.Gzip.code,
                            )
                        ),
                        options =
                            ArchiveOpenOptions.build {
                                limits = ArchiveLimits.build {
                                    maxTileDecompressedBytes = (helloBytes.size - 1).toULong()
                                }
                            },
                    )
                archive.readDecompressedTile(0, 0, 0)
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }
}
