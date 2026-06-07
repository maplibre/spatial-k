package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.io.bytestring.ByteString

class WriterModelsTest {
    @Test
    fun archiveWriteTileFactoriesSetExplicitPayloadMode() {
        val coord = TileCoord(z = 1, x = 0, y = 1)
        val payload = ByteString(1, 2, 3)

        val stored = ArchiveWriteTile.stored(coord, payload)
        val uncompressed = ArchiveWriteTile.uncompressed(coord, payload)

        assertEquals(coord, stored.coord)
        assertEquals(payload, stored.payload)
        assertEquals(TilePayloadMode.Stored, stored.payloadMode)
        assertEquals(TilePayloadMode.Uncompressed, uncompressed.payloadMode)
    }

    @Test
    fun writeBoundsRejectInvalidCoordinates() {
        assertFailsWith<PmTilesException> {
            ArchiveWriteBounds(west = 181.0, south = 0.0, east = 180.0, north = 1.0)
        }
        assertFailsWith<PmTilesException> {
            ArchiveWriteBounds(west = 10.0, south = 0.0, east = 9.0, north = 1.0)
        }
        assertFailsWith<PmTilesException> {
            ArchiveWriteBounds(west = 0.0, south = Double.NaN, east = 1.0, north = 1.0)
        }
    }

    @Test
    fun writeCenterRejectsInvalidCoordinatesAndZoom() {
        assertFailsWith<PmTilesException> {
            ArchiveWriteCenter(longitude = 0.0, latitude = 91.0, zoom = 0)
        }
        assertFailsWith<PmTilesException> {
            ArchiveWriteCenter(longitude = 0.0, latitude = 0.0, zoom = 32)
        }
    }

    @Test
    fun archiveWriteConfigBuilderUpdatesSelectedFields() {
        val bounds = ArchiveWriteBounds(west = -1.0, south = -2.0, east = 3.0, north = 4.0)
        val center = ArchiveWriteCenter(longitude = 1.0, latitude = 2.0, zoom = 3)
        val config = ArchiveWriteConfig.build {
            tileType = TileTypeCodes.Png
            this.bounds = bounds
            this.center = center
            metadataJson = """{"name":"demo"}"""
        }

        assertEquals(TileTypeCodes.Png, config.tileType)
        assertEquals(bounds, config.bounds)
        assertEquals(center, config.center)
        assertEquals("""{"name":"demo"}""", config.metadataJson)
        assertEquals(TileTypeCodes.Png, config.toBuilder().build().tileType)
    }

    @Test
    fun archiveWriteConfigDefaultsAreValid() {
        val config = ArchiveWriteConfig()

        assertEquals(TileTypeCodes.Unknown, config.tileType)
        assertEquals(ArchiveWriteBounds(), config.bounds)
        assertEquals(ArchiveWriteCenter(), config.center)
        assertEquals("{}", config.metadataJson)
    }
}
