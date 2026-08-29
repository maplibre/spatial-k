package org.maplibre.spatialk.geohash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.assertPositionEquals

class GeohashTest {
    @Test
    fun `encodes Wikipedia vectors`() {
        assertEquals(
            "u4pruydqqvj",
            Geohash.of(
                    Position(longitude = 10.40744, latitude = 57.64911),
                    length = 11,
                )
                .text,
        )
        assertEquals(
            "ezs42",
            Geohash.of(
                    Position(longitude = -5.60302734375, latitude = 42.60498046875),
                    length = 5,
                )
                .text,
        )
    }

    @Test
    fun `decodes the Wikipedia center and bounding box`() {
        val cell = Geohash.parse("ezs42")

        assertPositionEquals(
            Position(longitude = -5.60302734375, latitude = 42.60498046875),
            cell.center,
        )
        assertDoubleEquals(-5.625, cell.boundingBox.west)
        assertDoubleEquals(42.5830078125, cell.boundingBox.south)
        assertDoubleEquals(-5.5810546875, cell.boundingBox.east)
        assertDoubleEquals(42.626953125, cell.boundingBox.north)
    }

    @Test
    fun `parse folds ASCII case and emits lowercase`() {
        val cell = Geohash.parse("U4PRUYDQQVJ")

        assertEquals("u4pruydqqvj", cell.text)
        assertEquals(cell, Geohash.parse(cell.text))
        assertEquals(cell.text, cell.toString())
        assertEquals(11, cell.length)
        assertNotNull(Geohash.parseOrNull("EzS42"))
    }

    @Test
    fun `rejects invalid text`() {
        listOf("", "a", "i", "l", "o", "ezs42!", "0000000000000").forEach { text ->
            assertFailsWith<IllegalArgumentException>(text) { Geohash.parse(text) }
            assertNull(Geohash.parseOrNull(text), text)
        }
    }

    @Test
    fun `validates length and coordinates at construction`() {
        val origin = Position(longitude = 0.0, latitude = 0.0)
        assertFailsWith<IllegalArgumentException> { Geohash.of(origin, length = 0) }
        assertFailsWith<IllegalArgumentException> { Geohash.of(origin, length = 13) }
        assertFailsWith<IllegalArgumentException> {
            Geohash.of(Position(longitude = -180.0001, latitude = 0.0), length = 5)
        }
        assertFailsWith<IllegalArgumentException> {
            Geohash.of(Position(longitude = 180.0001, latitude = 0.0), length = 5)
        }
        assertFailsWith<IllegalArgumentException> {
            Geohash.of(Position(longitude = 0.0, latitude = -90.0001), length = 5)
        }
        assertFailsWith<IllegalArgumentException> {
            Geohash.of(Position(longitude = 0.0, latitude = 90.0001), length = 5)
        }
        assertFailsWith<IllegalArgumentException> {
            Geohash.of(Position(longitude = Double.NaN, latitude = 0.0), length = 5)
        }
        assertFailsWith<IllegalArgumentException> {
            Geohash.of(Position(longitude = 0.0, latitude = Double.POSITIVE_INFINITY), length = 5)
        }
    }

    @Test
    fun `accepts coordinate edges and ignores altitude`() {
        val northeast =
            Geohash.of(Position(longitude = 180.0, latitude = 90.0, altitude = 100.0), length = 12)
        val southwest = Geohash.of(Position(longitude = -180.0, latitude = -90.0), length = 12)

        assertEquals(12, northeast.length)
        assertEquals(12, southwest.length)
        assertEquals(
            northeast,
            Geohash.of(Position(longitude = 180.0, latitude = 90.0), length = 12),
        )
        assertEquals(northeast, Geohash.of(northeast.center, length = northeast.length))
        assertEquals(southwest, Geohash.of(southwest.center, length = southwest.length))
    }

    @Test
    fun `packed order equals text order across mixed lengths`() {
        val alphabet = "0123456789bcdefghjkmnpqrstuvwxyz"
        val texts = buildList {
            addAll(listOf("0", "z", "e", "ez", "ezs", "ezs4", "ezs42"))
            alphabet.forEach { add("ezs42$it") }
            addAll(listOf("b", "bc", "u4pruydqqvj", "zzzzzzzzzzzz"))
        }
        val cells = texts.map(Geohash::parse)

        assertEquals(texts.sorted(), cells.sorted().map(Geohash::text))
        assertEquals(cells.size, cells.toSet().size)
        assertTrue(Geohash.parse("ezs42") < Geohash.parse("ezs420"))
    }

    @Test
    fun `returns parent and truncates to an enclosing cell`() {
        val cell = Geohash.parse("u4pruydqqvj")

        assertEquals("u4pruydqqv", cell.parent?.text)
        assertEquals("u4pru", cell.truncatedTo(5).text)
        assertEquals(cell, cell.truncatedTo(cell.length))
        assertNull(Geohash.parse("u").parent)
        assertFailsWith<IllegalArgumentException> { cell.truncatedTo(0) }
        assertFailsWith<IllegalArgumentException> { cell.truncatedTo(cell.length + 1) }
    }

    @Test
    fun `returns children in base32ghs order`() {
        val cell = Geohash.parse("ezs42")
        val children = cell.children
        val alphabet = "0123456789bcdefghjkmnpqrstuvwxyz"

        assertEquals(32, children.size)
        assertEquals(alphabet.map { "ezs42$it" }, children.map(Geohash::text))
        children.forEach { child ->
            assertEquals(cell, child.parent)
            assertTrue(child in cell)
            assertEquals(cell, child.truncatedTo(cell.length))
        }
        assertEquals(emptyList(), Geohash.parse("zzzzzzzzzzzz").children)
    }

    @Test
    fun `returns libgeohash neighbors in compass order`() {
        val neighbors = Geohash.parse("ezs42").neighbors

        assertEquals("ezs48", neighbors.north?.text)
        assertEquals("ezs49", neighbors.northEast?.text)
        assertEquals("ezs43", neighbors.east.text)
        assertEquals("ezs41", neighbors.southEast?.text)
        assertEquals("ezs40", neighbors.south?.text)
        assertEquals("ezefp", neighbors.southWest?.text)
        assertEquals("ezefr", neighbors.west.text)
        assertEquals("ezefx", neighbors.northWest?.text)
        assertEquals(
            listOf("ezs48", "ezs49", "ezs43", "ezs41", "ezs40", "ezefp", "ezefr", "ezefx"),
            neighbors.toList().map(Geohash::text),
        )
    }

    @Test
    fun `wraps longitude and rejects offsets past the poles`() {
        val cell = Geohash.parse("b")
        assertEquals(cell, cell.offsetBy(east = 1, north = 0)?.offsetBy(east = -1, north = 0))
        assertEquals(
            cell,
            cell
                .offsetBy(east = Int.MAX_VALUE, north = 0)
                ?.offsetBy(east = -Int.MAX_VALUE, north = 0),
        )

        val top = Geohash.of(Position(longitude = 0.0, latitude = 90.0), length = 3).neighbors
        assertNull(top.north)
        assertNull(top.northEast)
        assertNull(top.northWest)
        assertEquals(5, top.toList().size)

        val bottom = Geohash.of(Position(longitude = 0.0, latitude = -90.0), length = 3).neighbors
        assertNull(bottom.south)
        assertNull(bottom.southEast)
        assertNull(bottom.southWest)
        assertEquals(5, bottom.toList().size)
    }

    @Test
    fun `contains positions and descendant cells`() {
        val cell = Geohash.parse("ezs42")
        val parent = cell.parent!!

        assertTrue(cell.center in cell)
        assertFalse(cell.neighbors.east.center in cell)
        assertTrue(cell in parent)
        assertTrue(parent in parent)
        assertFalse(parent in cell)
        assertFailsWith<IllegalArgumentException> {
            Position(longitude = Double.NaN, latitude = 0.0) in cell
        }
    }
}
