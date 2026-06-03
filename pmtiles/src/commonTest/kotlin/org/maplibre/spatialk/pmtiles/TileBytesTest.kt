package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.buildSingleTileArchive
import org.maplibre.spatialk.pmtiles.internal.runSuspending

class TileBytesTest {
    @Test
    fun readsCompressedTileBytesExactly() = runSuspending {
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
    fun decompressedModeLeavesNoneCompressedTilesUnchanged() = runSuspending {
        val tileBytes = byteArrayOf(5, 6, 7)
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(buildSingleTileArchive(tileBytes = tileBytes)),
                options = ArchiveOpenOptions(tileReadMode = TileReadMode.DecompressedBytes),
            )

        val tile = archive.getTile(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(tileBytes, tile.bytes)
        assertEquals(Compression.None, tile.compression)
        assertEquals(false, tile.wasDecompressed)
    }

    @Test
    fun returnsNullForMissingTilePayload() = runSuspending {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(buildSingleTileArchive(tileBytes = byteArrayOf(1)))
            )
        val missingCoord = TileIds.toZxy(1)

        assertNull(archive.getTile(missingCoord.z, missingCoord.x, missingCoord.y))
        assertNull(archive.getTileCompressed(missingCoord.z, missingCoord.x, missingCoord.y))
    }

    @Test
    fun unsupportedTileCompressionFailsOnlyWhenDecompressionIsRequested() {
        val tileBytes = byteArrayOf(8, 9, 10)
        val bytes =
            buildSingleTileArchive(
                tileBytes = tileBytes,
                tileCompression = Compression.Brotli.code,
            )

        runSuspending {
            val archive = PmTilesArchive.open(TestByteRangeSource(bytes))
            assertContentEquals(tileBytes, archive.getTileCompressed(0, 0, 0)?.bytes)
        }

        val error =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    val archive =
                        PmTilesArchive.open(
                            TestByteRangeSource(bytes),
                            options =
                                ArchiveOpenOptions(tileReadMode = TileReadMode.DecompressedBytes),
                        )
                    archive.getTile(0, 0, 0)
                }
            }

        assertEquals(PmTilesErrorCode.UnsupportedCompression, error.code)
    }

    @Test
    fun enforcesCompressedTileByteLimit() {
        val tileBytes = byteArrayOf(11, 12, 13)
        val error =
            assertFailsWith<PmTilesException> {
                runSuspending {
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
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }
}
