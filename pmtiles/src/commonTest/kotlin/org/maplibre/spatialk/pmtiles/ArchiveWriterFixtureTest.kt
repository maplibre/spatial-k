package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.readResourceBytes

class ArchiveWriterFixtureTest {
    @Test
    fun rewritesGoUnclusteredPngFixtureAsClusteredArchive() = runTest {
        val source = openFixture("go-pmtiles-unclustered.pmtiles")
        val fixtureTiles = source.readStoredTilesThroughZoom(maxZoom = 1)
        val sourcePayloads = fixtureTiles.associate { it.coord to it.payload }
        val metadataJson = source.rawMetadataJson().ifBlank { "{}" }

        val bytes =
            PmTiles.writeToByteString(
                tiles =
                    fixtureTiles.map { tile ->
                        ArchiveWriteTile.stored(tile.coord, tile.payload)
                    },
                config =
                    ArchiveWriteConfig.build {
                        tileType = source.tileType
                        bounds = source.header.bounds.toWriteBounds()
                        center = source.header.center.toWriteCenter()
                        this.metadataJson = metadataJson
                    },
                options = ArchiveWriteOptions.build { tileCompression = source.tileCompression },
            )
        val rewritten = PmTiles.open(TestByteRangeSource(bytes))

        assertEquals(TileTypeCodes.Png, rewritten.tileType)
        assertEquals(true, rewritten.header.isClustered)
        assertEquals(fixtureTiles.size.toULong(), rewritten.header.counts.addressedTiles)
        assertEquals(fixtureTiles.size.toULong(), rewritten.header.counts.tileEntries)
        assertEquals(fixtureTiles.size.toULong(), rewritten.header.counts.tileContents)
        assertEquals(metadataJson, rewritten.rawMetadataJson())
        assertHeaderLocation(source.header, rewritten.header)
        sourcePayloads.forEach { (coord, payload) ->
            assertEquals(payload, assertNotNull(rewritten.readStoredTile(coord)).payload, "$coord")
        }
    }

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
    fun writesMvtArchiveWithRealVectorMetadataFromUpstreamFixture() = runTest {
        val source = openFixture("protomaps-vector-odbl-firenze.pmtiles")
        val storedTile = assertNotNull(source.readStoredTile(0, 0, 0))
        val metadataJson = source.rawMetadataJson()

        val bytes =
            PmTiles.writeToByteString(
                tiles = listOf(ArchiveWriteTile.stored(storedTile.coord, storedTile.payload)),
                config =
                    ArchiveWriteConfig.build {
                        tileType = TileTypeCodes.Mvt
                        this.metadataJson = metadataJson
                    },
                options = ArchiveWriteOptions.build { tileCompression = source.tileCompression },
            )
        val rewritten = PmTiles.open(TestByteRangeSource(bytes))
        val metadata = rewritten.metadata()

        assertEquals(TileTypeCodes.Mvt, rewritten.tileType)
        assertEquals(CompressionCodes.Gzip, rewritten.tileCompression)
        assertEquals(metadataJson, rewritten.rawMetadataJson())
        assertNotNull(metadata.vectorLayersJson)
        assertEquals(storedTile.payload, assertNotNull(rewritten.readStoredTile(0, 0, 0)).payload)
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

    private fun LonLatBounds.toWriteBounds(): ArchiveWriteBounds =
        ArchiveWriteBounds(west = west, south = south, east = east, north = north)

    private fun TileCenter.toWriteCenter(): ArchiveWriteCenter =
        ArchiveWriteCenter(longitude = longitude, latitude = latitude, zoom = zoom)

    private fun assertHeaderLocation(expected: ArchiveHeader, actual: ArchiveHeader) {
        assertDoubleEquals(expected.bounds.west, actual.bounds.west)
        assertDoubleEquals(expected.bounds.south, actual.bounds.south)
        assertDoubleEquals(expected.bounds.east, actual.bounds.east)
        assertDoubleEquals(expected.bounds.north, actual.bounds.north)
        assertDoubleEquals(expected.center.longitude, actual.center.longitude)
        assertDoubleEquals(expected.center.latitude, actual.center.latitude)
        assertEquals(expected.center.zoom, actual.center.zoom)
    }
}
