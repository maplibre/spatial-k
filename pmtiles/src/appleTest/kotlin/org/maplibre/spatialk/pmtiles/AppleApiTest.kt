package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.internal.buildSingleTileArchive
import org.maplibre.spatialk.pmtiles.internal.runSuspending
import platform.Foundation.NSData

class AppleApiTest {
    @Test
    fun archiveTileDataReturnsCopyingNSData() {
        val bytes = byteArrayOf(1, 2, 3)
        val tile =
            ArchiveTile(
                tileId = 0,
                coord = TileCoord(0, 0, 0),
                bytes = bytes,
                tileType = TileType.Png,
                compression = Compression.None,
                wasDecompressed = false,
                range =
                    TileRange(
                        tileId = 0,
                        coord = TileCoord(0, 0, 0),
                        archiveRange = ByteRange(0uL, 3),
                        tileType = TileType.Png,
                        compression = Compression.None,
                        directoryDepth = 0,
                    ),
            )

        val firstData = tile.data
        bytes[0] = 9
        val secondData = tile.data

        assertContentEquals(byteArrayOf(1, 2, 3), firstData.toByteArray())
        assertContentEquals(byteArrayOf(9, 2, 3), secondData.toByteArray())
    }

    @Test
    fun opensArchiveFromByteRangeDataSource() = runSuspending {
        val tileBytes = byteArrayOf(4, 5, 6)
        val source = TestByteRangeDataSource(buildSingleTileArchive(tileBytes))

        val archive = PmTilesArchive.open(source)

        assertContentEquals(tileBytes, archive.getStoredTile(0, 0, 0)?.bytes)
        assertEquals(2, source.reads.size)
    }

    @Test
    fun dataDecompressorDecodesTiles() = runSuspending {
        val compressedBytes = byteArrayOf(7, 8, 9)
        val decompressedBytes = byteArrayOf(10, 11, 12)
        val source =
            TestByteRangeDataSource(
                buildSingleTileArchive(
                    tileBytes = compressedBytes,
                    tileCompression = Compression.Brotli.code,
                )
            )
        val options =
            ArchiveOpenOptions()
                .withDecompressor(
                    Compression.Brotli,
                    object : DataDecompressor {
                        override suspend fun decompress(
                            data: NSData,
                            limits: DecompressionLimits,
                        ): NSData {
                            assertContentEquals(compressedBytes, data.toByteArray())
                            assertEquals(
                                ArchiveLimits.Default.maxTileCompressedBytes,
                                limits.maxCompressedBytes,
                            )
                            return decompressedBytes.toNSData()
                        }
                    },
                )

        val archive = PmTilesArchive.open(source, options)
        val tile = archive.getDecompressedTile(0, 0, 0)

        requireNotNull(tile)
        assertContentEquals(decompressedBytes, tile.bytes)
        assertEquals(Compression.None, tile.compression)
    }

    @Test
    fun rejectsShortAndLongByteRangeDataSourceReads() {
        val archiveBytes = buildSingleTileArchive(byteArrayOf(7))

        val shortError =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    PmTilesArchive.open(
                        TestByteRangeDataSource(archiveBytes, lengthAdjustment = -1)
                    )
                }
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, shortError.code)

        val longError =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    PmTilesArchive.open(TestByteRangeDataSource(archiveBytes, lengthAdjustment = 1))
                }
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, longError.code)
    }

    private class TestByteRangeDataSource(
        private val bytes: ByteArray,
        private val lengthAdjustment: Int = 0,
    ) : ByteRangeDataSource {
        val reads = mutableListOf<ByteRange>()

        override suspend fun size(): ULong = bytes.size.toULong()

        override suspend fun read(offset: ULong, length: Int): NSData {
            reads += ByteRange(offset, length)
            val start = offset.toInt()
            val adjustedLength = length + lengthAdjustment
            val end = start + adjustedLength
            return bytes.copyOfRange(start, end).toNSData()
        }
    }
}
