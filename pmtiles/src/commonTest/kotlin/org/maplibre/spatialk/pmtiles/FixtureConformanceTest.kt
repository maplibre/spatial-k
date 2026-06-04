package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.testutil.readResourceBytes

class FixtureConformanceTest {
    @Test
    fun opensValidUpstreamFixtures() = runTest {
        validFixtures.forEach { fixture ->
            val archive = PmTiles.open(TestByteRangeSource(readFixture(fixture.path)))

            assertEquals(fixture.tileType, archive.tileType, fixture.path)
            assertEquals(fixture.internalCompression, archive.internalCompression, fixture.path)
            assertEquals(fixture.tileCompression, archive.tileCompression, fixture.path)
            assertEquals(fixture.minZoom, archive.header.minZoom, fixture.path)
            assertEquals(fixture.maxZoom, archive.header.maxZoom, fixture.path)
            assertEquals(fixture.isClustered, archive.header.isClustered, fixture.path)
            assertEquals(
                fixture.addressedTiles,
                archive.header.counts.addressedTiles.rawValue,
                fixture.path,
            )
            assertEquals(
                fixture.tileEntries,
                archive.header.counts.tileEntries.rawValue,
                fixture.path,
            )
            assertEquals(
                fixture.tileContents,
                archive.header.counts.tileContents.rawValue,
                fixture.path,
            )
            assertEquals(0, archive.warnings.size, fixture.path)
        }
    }

    @Test
    fun parsesFixtureMetadata() = runTest {
        val vector =
            PmTiles.open(TestByteRangeSource(readFixture("protomaps-vector-odbl-firenze.pmtiles")))
        val vectorMetadata = vector.metadata()
        assertEquals("Protomaps Basemap", vectorMetadata.name)
        assertEquals(TilesetKind(KnownTilesetKind.BaseLayer), vectorMetadata.type)
        assertNotNull(vectorMetadata.vectorLayersJson)

        val webp =
            PmTiles.open(TestByteRangeSource(readFixture("usgs-mt-whitney-8-15-webp-512.pmtiles")))
        val webpMetadata = webp.metadata()
        assertEquals(true, webp.rawMetadataJson().contains(""""format":"webp""""))
        assertEquals(TilesetKind("raster"), webpMetadata.type)
    }

    @Test
    fun decodesGzipMvtTileFixture() = runTest {
        val archive =
            PmTiles.open(TestByteRangeSource(readFixture("pmtiles-js-test-fixture-1.pmtiles")))

        val tile = assertNotNull(archive.readDecompressedTile(0, 0, 0))

        assertEquals(TileType(KnownTileType.Mvt), tile.tileType)
        assertEquals(Compression(KnownCompression.None), tile.compression)
        assertEquals(true, tile.wasDecompressed)
        assertEquals(0x1a, tile.bytes.first().toInt() and 0xff)
    }

    @Test
    fun readsRasterFixtureThroughLeafDirectories() = runTest {
        val archive =
            PmTiles.open(
                TestByteRangeSource(readFixture("stamen-toner-raster-cc-by-odbl-z3.pmtiles"))
            )

        val range = assertNotNull(archive.findTileRange(3, 4, 3))
        val tile = assertNotNull(archive.readStoredTile(3, 4, 3))

        assertEquals(TileType(KnownTileType.Png), range.tileType)
        assertEquals(Compression(KnownCompression.None), range.compression)
        assertEquals(TileType(KnownTileType.Png), tile.tileType)
        assertContentEquals(
            byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47),
            tile.bytes.copyOfRange(0, 4),
        )
    }

    @Test
    fun rejectsInvalidUpstreamFixtures() = runTest {
        invalidFixtures.forEach { fixture ->
            val error =
                assertFailsWith<PmTilesException>(fixture.path) {
                    PmTiles.open(TestByteRangeSource(readFixture(fixture.path)))
                }
            assertEquals(fixture.errorCode, error.code, fixture.path)
        }
    }

    @Test
    fun opensEmptyRootFixtureInLenientMode() = runTest {
        val path = "pmtiles-js-test-fixture-mlt.pmtiles"
        val strictError =
            assertFailsWith<PmTilesException>(path) {
                PmTiles.open(TestByteRangeSource(readFixture(path)))
            }
        val archive =
            PmTiles.open(
                TestByteRangeSource(readFixture(path)),
                options = ArchiveOpenOptions(validationMode = ValidationMode.Lenient),
            )

        assertEquals(PmTilesErrorCode.InvalidDirectory, strictError.code)
        assertEquals(TileType(KnownTileType.Mlt), archive.tileType)
        assertEquals(Compression(KnownCompression.Gzip), archive.internalCompression)
        assertEquals(Compression(KnownCompression.Gzip), archive.tileCompression)
        assertEquals(true, archive.header.isClustered)
        assertTrue(
            archive.warnings.any { it.code == ArchiveWarningCode.EmptyRootDirectory },
            "Expected EmptyRootDirectory warning.",
        )
    }

    @Test
    fun opensPinnedGeneratedGoPmtilesFixture() = runTest {
        val archive =
            PmTiles.open(
                TestByteRangeSource(
                    readFixture("generated-go-pmtiles-unclustered-clustered.pmtiles")
                )
            )

        assertEquals(true, archive.header.isClustered)
        assertEquals(Compression(KnownCompression.Gzip), archive.internalCompression)
        assertEquals(true, archive.containsTile(1, 0, 0))
        assertEquals(true, archive.containsTile(1, 0, 1))
        assertEquals(false, archive.containsTile(1, 1, 0))
    }

    private fun readFixture(path: String): ByteArray = readResourceBytes("fixtures/$path")
}

private data class ValidFixture(
    val path: String,
    val tileType: TileType,
    val internalCompression: Compression,
    val tileCompression: Compression,
    val minZoom: Int,
    val maxZoom: Int,
    val isClustered: Boolean,
    val addressedTiles: ULong,
    val tileEntries: ULong,
    val tileContents: ULong,
)

private data class InvalidFixture(
    val path: String,
    val errorCode: PmTilesErrorCode,
)

private val validFixtures =
    listOf(
        ValidFixture(
            path = "protomaps-vector-odbl-firenze.pmtiles",
            tileType = TileType(KnownTileType.Mvt),
            internalCompression = Compression(KnownCompression.Gzip),
            tileCompression = Compression(KnownCompression.Gzip),
            minZoom = 0,
            maxZoom = 15,
            isClustered = true,
            addressedTiles = 92uL,
            tileEntries = 92uL,
            tileContents = 92uL,
        ),
        ValidFixture(
            path = "stamen-toner-raster-cc-by-odbl-z3.pmtiles",
            tileType = TileType(KnownTileType.Png),
            internalCompression = Compression(KnownCompression.Gzip),
            tileCompression = Compression(KnownCompression.None),
            minZoom = 0,
            maxZoom = 3,
            isClustered = true,
            addressedTiles = 85uL,
            tileEntries = 84uL,
            tileContents = 80uL,
        ),
        ValidFixture(
            path = "usgs-mt-whitney-8-15-webp-512.pmtiles",
            tileType = TileType(KnownTileType.Webp),
            internalCompression = Compression(KnownCompression.Gzip),
            tileCompression = Compression(KnownCompression.None),
            minZoom = 8,
            maxZoom = 15,
            isClustered = true,
            addressedTiles = 50uL,
            tileEntries = 50uL,
            tileContents = 50uL,
        ),
        ValidFixture(
            path = "pmtiles-js-test-fixture-1.pmtiles",
            tileType = TileType(KnownTileType.Mvt),
            internalCompression = Compression(KnownCompression.Gzip),
            tileCompression = Compression(KnownCompression.Gzip),
            minZoom = 0,
            maxZoom = 0,
            isClustered = false,
            addressedTiles = 1uL,
            tileEntries = 1uL,
            tileContents = 1uL,
        ),
        ValidFixture(
            path = "pmtiles-js-test-fixture-2.pmtiles",
            tileType = TileType(KnownTileType.Mvt),
            internalCompression = Compression(KnownCompression.Gzip),
            tileCompression = Compression(KnownCompression.Gzip),
            minZoom = 0,
            maxZoom = 0,
            isClustered = false,
            addressedTiles = 1uL,
            tileEntries = 1uL,
            tileContents = 1uL,
        ),
        ValidFixture(
            path = "go-pmtiles-unclustered.pmtiles",
            tileType = TileType(KnownTileType.Png),
            internalCompression = Compression(KnownCompression.None),
            tileCompression = Compression(KnownCompression.None),
            minZoom = 1,
            maxZoom = 1,
            isClustered = false,
            addressedTiles = 2uL,
            tileEntries = 2uL,
            tileContents = 2uL,
        ),
    )

private val invalidFixtures =
    listOf(
        InvalidFixture(
            "pmtiles-js-empty.pmtiles",
            PmTilesErrorCode.InvalidHeader,
        ),
        InvalidFixture(
            "pmtiles-js-invalid.pmtiles",
            PmTilesErrorCode.InvalidMagic,
        ),
        InvalidFixture(
            "pmtiles-js-invalid-v4.pmtiles",
            PmTilesErrorCode.UnsupportedVersion,
        ),
    )
