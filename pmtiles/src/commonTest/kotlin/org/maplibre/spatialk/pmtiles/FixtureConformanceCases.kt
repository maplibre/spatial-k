package org.maplibre.spatialk.pmtiles

import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.runSuspending

internal class FixtureConformanceCases(private val readFixture: (String) -> ByteArray) {
    fun opensValidUpstreamFixtures() = runSuspending {
        validFixtures.forEach { fixture ->
            val archive = PmTilesArchive.open(TestByteRangeSource(readFixture(fixture.path)))

            assertEquals(fixture.tileType, archive.tileType, fixture.path)
            assertEquals(fixture.internalCompression, archive.internalCompression, fixture.path)
            assertEquals(fixture.tileCompression, archive.tileCompression, fixture.path)
            assertEquals(fixture.minZoom, archive.header.minZoom, fixture.path)
            assertEquals(fixture.maxZoom, archive.header.maxZoom, fixture.path)
            assertEquals(fixture.clustered, archive.header.clustered, fixture.path)
            assertEquals(
                fixture.addressedTiles,
                archive.header.counts.rawAddressedTiles,
                fixture.path,
            )
            assertEquals(fixture.tileEntries, archive.header.counts.rawTileEntries, fixture.path)
            assertEquals(fixture.tileContents, archive.header.counts.rawTileContents, fixture.path)
            assertEquals(0, archive.warningCount, fixture.path)
        }
    }

    fun parsesFixtureMetadata() = runSuspending {
        val vector =
            PmTilesArchive.open(
                TestByteRangeSource(
                    readFixture("upstream/pmtiles-spec-v3/protomaps-vector-odbl-firenze.pmtiles")
                )
            )
        val vectorMetadata = vector.metadata()
        assertEquals("Protomaps Basemap", vectorMetadata.name)
        assertEquals(TilesetKind.BaseLayer, vectorMetadata.type)
        assertNotNull(vectorMetadata.vectorLayersJson)

        val webp =
            PmTilesArchive.open(
                TestByteRangeSource(
                    readFixture("upstream/pmtiles-spec-v3/usgs-mt-whitney-8-15-webp-512.pmtiles")
                )
            )
        val webpMetadata = webp.metadata()
        assertEquals(true, webp.rawMetadataJson().contains(""""format":"webp""""))
        assertEquals(TilesetKind("raster"), webpMetadata.type)
    }

    fun decodesGzipMvtTileFixture() = runSuspending {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    readFixture("upstream/pmtiles-js-test-data/test-fixture-1.pmtiles")
                ),
                options = ArchiveOpenOptions(tileReadMode = TileReadMode.DecompressedBytes),
            )

        val tile = assertNotNull(archive.getTile(0, 0, 0))

        assertEquals(TileType.Mvt, tile.tileType)
        assertEquals(Compression.None, tile.compression)
        assertEquals(true, tile.wasDecompressed)
        assertEquals(0x1a, tile.bytes.first().toInt() and 0xff)
    }

    fun readsRasterFixtureThroughLeafDirectories() = runSuspending {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    readFixture(
                        "upstream/pmtiles-spec-v3/stamen-toner-raster-cc-by-odbl-z3.pmtiles"
                    )
                )
            )

        val range = assertNotNull(archive.getTileRange(3, 4, 3))
        val tile = assertNotNull(archive.getTileCompressed(3, 4, 3))

        assertEquals(TileType.Png, range.tileType)
        assertEquals(Compression.None, range.compression)
        assertEquals(TileType.Png, tile.tileType)
        assertContentEquals(
            byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47),
            tile.bytes.copyOfRange(0, 4),
        )
    }

    fun rejectsInvalidUpstreamFixtures() {
        invalidFixtures.forEach { fixture ->
            val error =
                assertFailsWith<PmTilesException>(fixture.path) {
                    runSuspending {
                        PmTilesArchive.open(TestByteRangeSource(readFixture(fixture.path)))
                    }
                }
            assertEquals(fixture.errorCode, error.code, fixture.path)
        }
    }

    fun opensPinnedGeneratedGoPmtilesFixture() = runSuspending {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    readFixture("generated/go-pmtiles-unclustered-clustered.pmtiles")
                )
            )

        assertEquals(Clustered.Yes, archive.header.clustered)
        assertEquals(Compression.Gzip, archive.internalCompression)
        assertEquals(true, archive.containsTile(1, 0, 0))
        assertEquals(true, archive.containsTile(1, 0, 1))
        assertEquals(false, archive.containsTile(1, 1, 0))
    }
}

private data class ValidFixture(
    val path: String,
    val tileType: TileType,
    val internalCompression: Compression,
    val tileCompression: Compression,
    val minZoom: Int,
    val maxZoom: Int,
    val clustered: Clustered,
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
            path = "upstream/pmtiles-spec-v3/protomaps-vector-odbl-firenze.pmtiles",
            tileType = TileType.Mvt,
            internalCompression = Compression.Gzip,
            tileCompression = Compression.Gzip,
            minZoom = 0,
            maxZoom = 15,
            clustered = Clustered.Yes,
            addressedTiles = 92uL,
            tileEntries = 92uL,
            tileContents = 92uL,
        ),
        ValidFixture(
            path = "upstream/pmtiles-spec-v3/stamen-toner-raster-cc-by-odbl-z3.pmtiles",
            tileType = TileType.Png,
            internalCompression = Compression.Gzip,
            tileCompression = Compression.None,
            minZoom = 0,
            maxZoom = 3,
            clustered = Clustered.Yes,
            addressedTiles = 85uL,
            tileEntries = 84uL,
            tileContents = 80uL,
        ),
        ValidFixture(
            path = "upstream/pmtiles-spec-v3/usgs-mt-whitney-8-15-webp-512.pmtiles",
            tileType = TileType.Webp,
            internalCompression = Compression.Gzip,
            tileCompression = Compression.None,
            minZoom = 8,
            maxZoom = 15,
            clustered = Clustered.Yes,
            addressedTiles = 50uL,
            tileEntries = 50uL,
            tileContents = 50uL,
        ),
        ValidFixture(
            path = "upstream/pmtiles-js-test-data/test-fixture-1.pmtiles",
            tileType = TileType.Mvt,
            internalCompression = Compression.Gzip,
            tileCompression = Compression.Gzip,
            minZoom = 0,
            maxZoom = 0,
            clustered = Clustered.No,
            addressedTiles = 1uL,
            tileEntries = 1uL,
            tileContents = 1uL,
        ),
        ValidFixture(
            path = "upstream/pmtiles-js-test-data/test-fixture-2.pmtiles",
            tileType = TileType.Mvt,
            internalCompression = Compression.Gzip,
            tileCompression = Compression.Gzip,
            minZoom = 0,
            maxZoom = 0,
            clustered = Clustered.No,
            addressedTiles = 1uL,
            tileEntries = 1uL,
            tileContents = 1uL,
        ),
        ValidFixture(
            path = "upstream/pmtiles-js-test-data/test-fixture-mlt.pmtiles",
            tileType = TileType.Mlt,
            internalCompression = Compression.Gzip,
            tileCompression = Compression.Gzip,
            minZoom = 0,
            maxZoom = 0,
            clustered = Clustered.Yes,
            addressedTiles = 0uL,
            tileEntries = 0uL,
            tileContents = 0uL,
        ),
        ValidFixture(
            path = "upstream/go-pmtiles/test-fixture-1.pmtiles",
            tileType = TileType.Mvt,
            internalCompression = Compression.Gzip,
            tileCompression = Compression.Gzip,
            minZoom = 0,
            maxZoom = 0,
            clustered = Clustered.No,
            addressedTiles = 1uL,
            tileEntries = 1uL,
            tileContents = 1uL,
        ),
        ValidFixture(
            path = "upstream/go-pmtiles/unclustered.pmtiles",
            tileType = TileType.Png,
            internalCompression = Compression.None,
            tileCompression = Compression.None,
            minZoom = 1,
            maxZoom = 1,
            clustered = Clustered.No,
            addressedTiles = 2uL,
            tileEntries = 2uL,
            tileContents = 2uL,
        ),
    )

private val invalidFixtures =
    listOf(
        InvalidFixture(
            "upstream/pmtiles-js-test-data/empty.pmtiles",
            PmTilesErrorCode.InvalidHeader,
        ),
        InvalidFixture(
            "upstream/pmtiles-js-test-data/invalid.pmtiles",
            PmTilesErrorCode.InvalidMagic,
        ),
        InvalidFixture(
            "upstream/pmtiles-js-test-data/invalid-v4.pmtiles",
            PmTilesErrorCode.UnsupportedVersion,
        ),
    )
