package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.testutil.readResourceBytes

class ArchiveWriterFixtureTest {
    @Test
    fun rewritesStoredGzipMvtTileFromUpstreamFixture() = runTest {
        val source = openFixture("pmtiles-js-test-fixture-1.pmtiles")
        val storedTile = assertNotNull(source.readStoredTile(0, 0, 0))
        val decompressedTile = assertNotNull(source.readDecompressedTile(0, 0, 0))
        val sourceMetadataJson = source.rawMetadataJson()

        val bytes =
            PmTiles.writeToByteString(
                tiles = listOf(ArchiveWriteTile.stored(storedTile.coord, storedTile.payload)),
                config =
                    ArchiveWriteConfig.build {
                        tileType = source.tileType
                        metadataJson = sourceMetadataJson
                    },
                options = ArchiveWriteOptions.build { tileCompression = source.tileCompression },
            )
        val rewritten = PmTiles.open(TestByteRangeSource(bytes))
        val rewrittenStoredTile = assertNotNull(rewritten.readStoredTile(0, 0, 0))
        val rewrittenDecompressedTile = assertNotNull(rewritten.readDecompressedTile(0, 0, 0))

        assertEquals(TileTypeCodes.Mvt, rewritten.tileType)
        assertEquals(CompressionCodes.None, rewritten.internalCompression)
        assertEquals(CompressionCodes.Gzip, rewritten.tileCompression)
        assertEquals(true, rewritten.header.isClustered)
        assertEquals(1uL, rewritten.header.counts.addressedTiles)
        assertEquals(1uL, rewritten.header.counts.tileEntries)
        assertEquals(1uL, rewritten.header.counts.tileContents)
        assertEquals(sourceMetadataJson, rewritten.rawMetadataJson())
        assertEquals(storedTile.payload, rewrittenStoredTile.payload)
        assertEquals(decompressedTile.payload, rewrittenDecompressedTile.payload)
        assertEquals(true, rewrittenDecompressedTile.wasDecompressed)
    }

    @Test
    fun rewritesRasterFixtureThroughLeafDirectoriesWhenRootTargetIsSmall() = runTest {
        val source = openFixture("stamen-toner-raster-cc-by-odbl-z3.pmtiles")
        val fixtureTiles = source.readStoredTilesThroughZoom(maxZoom = 3)
        val sourceMetadataJson = source.rawMetadataJson()
        val sampleCoord = TileCoord(3, 4, 3)
        val sampleFixtureTile = assertNotNull(source.readStoredTile(sampleCoord))

        val bytes =
            PmTiles.writeToByteString(
                tiles =
                    fixtureTiles.map { tile ->
                        ArchiveWriteTile.stored(tile.coord, tile.payload)
                    },
                config =
                    ArchiveWriteConfig.build {
                        tileType = source.tileType
                        metadataJson = sourceMetadataJson
                    },
                options =
                    ArchiveWriteOptions.build {
                        tileCompression = source.tileCompression
                        limits = ArchiveWriteLimits.build { maxRootDirectoryBytes = 8uL }
                    },
            )
        val rewritten = PmTiles.open(TestByteRangeSource(bytes))
        val sampleRange = assertNotNull(rewritten.findTileRange(sampleCoord))
        val sampleRewrittenTile = assertNotNull(rewritten.readStoredTile(sampleCoord))

        assertEquals(TileTypeCodes.Png, rewritten.tileType)
        assertEquals(CompressionCodes.None, rewritten.internalCompression)
        assertEquals(CompressionCodes.None, rewritten.tileCompression)
        assertEquals(true, rewritten.header.isClustered)
        assertTrue(rewritten.header.leafDirectories.length > 0uL)
        assertEquals(1, sampleRange.directoryDepth)
        assertEquals(fixtureTiles.size.toULong(), rewritten.header.counts.addressedTiles)
        assertTrue(rewritten.header.counts.tileContents < rewritten.header.counts.addressedTiles)
        assertEquals(sampleFixtureTile.payload, sampleRewrittenTile.payload)
    }

    private suspend fun openFixture(path: String): PmTilesArchive =
        PmTiles.open(TestByteRangeSource(readFixture(path)))

    private suspend fun PmTilesArchive.readStoredTilesThroughZoom(maxZoom: Int): List<ArchiveTile> {
        val tiles = mutableListOf<ArchiveTile>()
        for (z in 0..maxZoom) {
            val dimension = 1 shl z
            for (x in 0 until dimension) {
                for (y in 0 until dimension) {
                    readStoredTile(z, x, y)?.let { tiles += it }
                }
            }
        }
        return tiles
    }

    private fun readFixture(path: String): ByteString = ByteString(readResourceBytes(path))
}
