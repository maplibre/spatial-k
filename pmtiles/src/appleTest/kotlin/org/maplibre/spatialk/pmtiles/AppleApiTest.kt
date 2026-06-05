package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toByteString
import kotlinx.io.bytestring.toNSData
import org.maplibre.spatialk.pmtiles.internal.buildSingleTileArchive
import org.maplibre.spatialk.pmtiles.internal.runSuspending
import platform.Foundation.NSData

class AppleApiTest {
    @Test
    fun byteStringDataReturnsSnapshotNSData() {
        val bytes = byteArrayOf(1, 2, 3)
        val tile =
            ArchiveTile(
                tileId = 0,
                coord = TileCoord(0, 0, 0),
                payload = ByteString(bytes),
                tileType = TileTypeCodes.Png,
                compression = CompressionCodes.None,
                wasDecompressed = false,
                range =
                    TileRange(
                        tileId = 0,
                        coord = TileCoord(0, 0, 0),
                        archiveRange = ByteRange(0uL, 3),
                        tileType = TileTypeCodes.Png,
                        compression = CompressionCodes.None,
                        directoryDepth = 0,
                    ),
            )

        val firstData = tile.payload.toNSData()
        bytes[0] = 9
        val secondData = tile.payload.toNSData()

        assertEquals(ByteString(1, 2, 3), firstData.toByteString())
        assertEquals(ByteString(1, 2, 3), secondData.toByteString())
    }

    @Test
    fun opensArchiveFromByteRangeDataSource() = runSuspending {
        val tileBytes = ByteString(4, 5, 6)
        val source = TestByteRangeDataSource(buildSingleTileArchive(tileBytes))

        val archive = PmTiles.open(source)

        assertEquals(tileBytes, archive.readStoredTile(0, 0, 0)?.payload)
        assertEquals(2, source.reads.size)
    }

    @Test
    fun customDecompressorDecodesTiles() = runSuspending {
        val compressedBytes = ByteString(7, 8, 9)
        val decompressedBytes = ByteString(10, 11, 12)
        val source =
            TestByteRangeDataSource(
                buildSingleTileArchive(
                    tileBytes = compressedBytes,
                    tileCompression = CompressionCodes.Brotli.code,
                )
            )
        val options = ArchiveOpenOptions.build {
            decompressor(CompressionCodes.Brotli) { bytes, limits ->
                assertEquals(compressedBytes, bytes)
                assertEquals(ArchiveLimits().maxTileCompressedBytes, limits.maxCompressedBytes)
                decompressedBytes
            }
        }

        val archive = PmTiles.open(source, options)
        val tile = archive.readDecompressedTile(0, 0, 0)

        requireNotNull(tile)
        assertEquals(decompressedBytes, tile.payload)
        assertEquals(CompressionCodes.None, tile.compression)
    }

    @Test
    fun archiveLimitsWithDirectoryBudgetRecomputesDerivedEntryLimit() {
        val maxDirectoryDecompressedBytes = (32 * 1024 * 1024).toULong()

        val limits =
            ArchiveLimits().withMaxDirectoryDecompressedBytes(maxDirectoryDecompressedBytes)

        assertEquals((32 * 1024 * 1024) / 17, limits.maxDirectoryEntries)
    }

    @Test
    fun rejectsShortAndLongByteRangeDataSourceReads() {
        val archiveBytes = buildSingleTileArchive(ByteString(7))

        val shortError =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    PmTiles.open(TestByteRangeDataSource(archiveBytes, lengthAdjustment = -1))
                }
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, shortError.code)

        val longError =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    PmTiles.open(TestByteRangeDataSource(archiveBytes, lengthAdjustment = 1))
                }
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, longError.code)
    }

    private class TestByteRangeDataSource(
        private val bytes: ByteString,
        private val lengthAdjustment: Int = 0,
    ) : ByteRangeDataSource {
        val reads = mutableListOf<ByteRange>()

        override fun size(): ULong = bytes.size.toULong()

        override suspend fun read(offset: ULong, length: ULong): NSData {
            val byteCount = length.toInt()
            reads += ByteRange(offset, byteCount)
            val start = offset.toInt()
            val adjustedLength = byteCount + lengthAdjustment
            val end = start + adjustedLength
            return bytes.substring(start, end).toNSData()
        }
    }
}
