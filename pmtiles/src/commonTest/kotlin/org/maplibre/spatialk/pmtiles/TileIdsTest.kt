package org.maplibre.spatialk.pmtiles

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TileIdsTest {
    @Test
    fun matchesOfficialPmTilesExamples() {
        assertEquals(0L, TileIds.fromZxy(0, 0, 0))
        assertEquals(1L, TileIds.fromZxy(1, 0, 0))
        assertEquals(2L, TileIds.fromZxy(1, 0, 1))
        assertEquals(3L, TileIds.fromZxy(1, 1, 1))
        assertEquals(4L, TileIds.fromZxy(1, 1, 0))
        assertEquals(5L, TileIds.fromZxy(2, 0, 0))
        assertEquals(19078479L, TileIds.fromZxy(12, 3423, 1763))
    }

    @Test
    fun computesZoomStarts() {
        assertEquals(0L, TileIds.zoomStart(0))
        assertEquals(1L, TileIds.zoomStart(1))
        assertEquals(5L, TileIds.zoomStart(2))
        assertEquals(21L, TileIds.zoomStart(3))
        assertEquals(1537228672809129301L, TileIds.zoomStart(31))
    }

    @Test
    fun roundTripsAllLowZoomCoordinates() {
        for (z in 0..8) {
            val limit = 1 shl z
            for (x in 0 until limit) {
                for (y in 0 until limit) {
                    val tileId = TileIds.fromZxy(z, x, y)
                    assertEquals(TileCoord(z, x, y), TileIds.toZxy(tileId))
                    assertEquals(TileCoord(z, x, y), TileCoord(tileId))
                }
            }
        }
    }

    @Test
    fun roundTripsRandomHighZoomCoordinates() {
        val random = Random(0x5eed)
        repeat(1_000) {
            val z = random.nextInt(from = 9, until = 32)
            val limit = 1L shl z
            val x = random.nextLong(limit).toInt()
            val y = random.nextLong(limit).toInt()
            val tileId = TileIds.fromZxy(z, x, y)

            assertEquals(TileCoord(z, x, y), TileIds.toZxy(tileId))
            assertEquals(TileCoord(z, x, y), TileCoord(tileId))
        }
    }

    @Test
    fun roundTripsMaximumSupportedCoordinates() {
        val max = Int.MAX_VALUE

        assertEquals(TileCoord(31, 0, 0), TileIds.toZxy(TileIds.fromZxy(31, 0, 0)))
        assertEquals(TileCoord(31, 0, 0), TileCoord(TileIds.fromZxy(31, 0, 0)))
        assertEquals(TileCoord(31, max, 0), TileIds.toZxy(TileIds.fromZxy(31, max, 0)))
        assertEquals(TileCoord(31, 0, max), TileIds.toZxy(TileIds.fromZxy(31, 0, max)))
        assertEquals(TileCoord(31, max, max), TileIds.toZxy(TileIds.fromZxy(31, max, max)))
    }

    @Test
    fun rejectsOutOfRangeZooms() {
        assertInvalidCoordinate { TileCoord(-1, 0, 0) }
        assertInvalidCoordinate { TileCoord(32, 0, 0) }
        assertInvalidCoordinate { TileIds.zoomStart(-1) }
        assertInvalidCoordinate { TileIds.zoomStart(32) }
        assertInvalidCoordinate { TileIds.fromZxy(-1, 0, 0) }
        assertInvalidCoordinate { TileIds.fromZxy(32, 0, 0) }
    }

    @Test
    fun rejectsOutOfRangeCoordinates() {
        assertInvalidCoordinate { TileCoord(0, 1, 0) }
        assertInvalidCoordinate { TileCoord(1, -1, 0) }
        assertInvalidCoordinate { TileCoord(1, 0, -1) }
        assertInvalidCoordinate { TileCoord(1, 2, 0) }
        assertInvalidCoordinate { TileCoord(1, 0, 2) }
        assertInvalidCoordinate { TileCoord(31, Int.MIN_VALUE, 0) }
        assertInvalidCoordinate { TileIds.fromZxy(0, 1, 0) }
        assertInvalidCoordinate { TileIds.fromZxy(1, -1, 0) }
        assertInvalidCoordinate { TileIds.fromZxy(1, 0, -1) }
        assertInvalidCoordinate { TileIds.fromZxy(1, 2, 0) }
        assertInvalidCoordinate { TileIds.fromZxy(1, 0, 2) }
        assertInvalidCoordinate { TileIds.fromZxy(31, Int.MIN_VALUE, 0) }
    }

    @Test
    fun rejectsOutOfRangeTileIds() {
        assertInvalidCoordinate { TileIds.toZxy(-1) }
        assertInvalidCoordinate { TileCoord(-1) }
        assertInvalidCoordinate { TileIds.toZxy(6148914691236517205L) }
    }

    private fun assertInvalidCoordinate(block: () -> Unit) {
        val error = assertFailsWith<PmTilesException>(block = block)
        assertEquals(PmTilesErrorCode.InvalidTileCoordinate, error.code)
    }
}
