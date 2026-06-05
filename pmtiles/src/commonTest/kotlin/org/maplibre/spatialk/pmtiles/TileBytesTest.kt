package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString
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
        val tileBytes = ByteString(1, 2, 3, 4)
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
        assertEquals(tileBytes, stored.payload)
        assertEquals(tileBytes.size, stored.payload.size)
        assertEquals(CompressionCodes.Brotli, stored.compression)
        assertEquals(false, stored.wasDecompressed)
        assertEquals(TileTypeCodes.Png, stored.tileType)
        assertEquals(tileBytes, coordRead.payload)
        assertEquals(tileBytes, idRead.payload)
        assertEquals(stored, coordRead)
        assertEquals(stored, idRead)

        val mutableBytes = stored.payload.toByteArray()
        mutableBytes[0] = 9
        assertEquals(tileBytes, stored.payload)
    }

    @Test
    fun batchReadCoalescesContiguousTilesAndPreservesInputOrder() = runTest {
        val tileData = ByteString(1, 2, 3, 4, 5)
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
        assertEquals(ByteString(3, 4, 5), tiles[0].tile?.payload)
        assertEquals(false, tiles[1].isFound)
        assertEquals(ByteString(1, 2), tiles[2].tile?.payload)
        assertEquals(listOf(ByteRange(tileDataOffset, tileData.size)), source.reads)
    }

    @Test
    fun batchReadOnlyCoalescesAcrossGapsWhenConfigured() = runTest {
        val tileData = ByteString(1, 2, 0, 0, 0, 6, 7)
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
        val tileData = ByteString(1, 2, 3, 4, 5)
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
        val compressedTileData = ByteString(1, 2, 3, 4)
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
                            buildByteString(bytes.size) {
                                repeat(bytes.size) { index ->
                                    append((bytes[index].toInt() + 10).toByte())
                                }
                            }
                        }
                    },
            )
        source.reads.clear()

        val tiles = archive.readDecompressedTiles(listOf(TileIds.toZxy(0), TileIds.toZxy(1)))

        assertEquals(ByteString(11, 12), tiles[0].tile?.payload)
        assertEquals(ByteString(13, 14), tiles[1].tile?.payload)
        assertEquals(CompressionCodes.None, tiles[0].tile?.compression)
        assertEquals(true, tiles[0].tile?.wasDecompressed)
        assertEquals(listOf(ByteRange(tileDataOffset, compressedTileData.size)), source.reads)
    }

    @Test
    fun decompressedReadLeavesNoneCompressedTilesUnchanged() = runTest {
        val tileBytes = ByteString(5, 6, 7)
        val archive =
            PmTiles.open(TestByteRangeSource(buildSingleTileArchive(tileBytes = tileBytes)))

        val tile = archive.readDecompressedTile(0, 0, 0)

        requireNotNull(tile)
        assertEquals(tileBytes, tile.payload)
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
        assertEquals(helloBytes, tile.payload)
        assertEquals(CompressionCodes.None, tile.compression)
        assertEquals(CompressionCodes.Gzip, tile.range.compression)
        assertEquals(true, tile.wasDecompressed)
    }

    @Test
    fun returnsNullForMissingTilePayload() = runTest {
        val archive =
            PmTiles.open(TestByteRangeSource(buildSingleTileArchive(tileBytes = ByteString(1))))
        val missingCoord = TileIds.toZxy(1)

        assertNull(archive.readStoredTile(missingCoord.z, missingCoord.x, missingCoord.y))
        assertNull(archive.readDecompressedTile(missingCoord.z, missingCoord.x, missingCoord.y))
    }

    @Test
    fun unsupportedTileCompressionFailsOnlyWhenDecompressionIsRequested() = runTest {
        val tileBytes = ByteString(8, 9, 10)
        val bytes =
            buildSingleTileArchive(
                tileBytes = tileBytes,
                tileCompression = CompressionCodes.Brotli.code,
            )

        val archive = PmTiles.open(TestByteRangeSource(bytes))
        assertEquals(tileBytes, archive.readStoredTile(0, 0, 0)?.payload)

        val error =
            assertFailsWith<PmTilesException> {
                archive.readDecompressedTile(0, 0, 0)
            }

        assertEquals(PmTilesErrorCode.UnsupportedCompression, error.code)
    }

    @Test
    fun customDecompressorDecodesTiles() = runTest {
        val compressedBytes = ByteString(1, 2, 3)
        val decompressedBytes = ByteString(4, 5, 6)
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
                            assertEquals(compressedBytes, bytes)
                            decompressedBytes
                        }
                    },
            )

        val tile = archive.readDecompressedTile(0, 0, 0)

        requireNotNull(tile)
        assertEquals(decompressedBytes, tile.payload)
        assertEquals(CompressionCodes.None, tile.compression)
        assertEquals(true, tile.wasDecompressed)
    }

    @Test
    fun enforcesCompressedTileByteLimit() = runTest {
        val tileBytes = ByteString(11, 12, 13)
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
