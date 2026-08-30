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
    fun `encodes imported vectors`() {
        GeohashFixtures.geohashEncode.forEach { case ->
            val label = "${case.source} ${case.text}"
            assertEquals(
                case.text,
                Geohash.of(
                        Position(longitude = case.longitude, latitude = case.latitude),
                        length = case.length,
                    )
                    .text,
                label,
            )
        }
    }

    @Test
    fun `decodes imported vectors`() {
        GeohashFixtures.geohashDecode.forEach { case ->
            val label = "${case.source} ${case.text}"
            val cell = Geohash.parse(case.text)
            assertPositionEquals(
                Position(longitude = case.longitude, latitude = case.latitude),
                cell.center,
                message = label,
            )
            if (
                case.west != null && case.south != null && case.east != null && case.north != null
            ) {
                assertDoubleEquals(case.west, cell.boundingBox.west, message = label)
                assertDoubleEquals(case.south, cell.boundingBox.south, message = label)
                assertDoubleEquals(case.east, cell.boundingBox.east, message = label)
                assertDoubleEquals(case.north, cell.boundingBox.north, message = label)
            }
        }
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
    fun `returns imported neighbors in compass order`() {
        GeohashFixtures.geohashNeighbors.forEach { case ->
            val label = "${case.source} ${case.text}"
            val neighbors = Geohash.parse(case.text).neighbors
            assertEquals(case.north, neighbors.north?.text, label)
            assertEquals(case.northEast, neighbors.northEast?.text, label)
            assertEquals(case.east, neighbors.east.text, label)
            assertEquals(case.southEast, neighbors.southEast?.text, label)
            assertEquals(case.south, neighbors.south?.text, label)
            assertEquals(case.southWest, neighbors.southWest?.text, label)
            assertEquals(case.west, neighbors.west.text, label)
            assertEquals(case.northWest, neighbors.northWest?.text, label)
            assertEquals(
                listOf(
                    case.north,
                    case.northEast,
                    case.east,
                    case.southEast,
                    case.south,
                    case.southWest,
                    case.west,
                    case.northWest,
                ),
                neighbors.toList().map(Geohash::text),
                label,
            )
        }
    }

    @Test
    fun `walks imported neighbor steps`() {
        GeohashFixtures.geohashNeighborSteps.forEach { case ->
            val label = "${case.source} ${case.text} east=${case.east} north=${case.north}"
            assertEquals(
                case.expected,
                Geohash.parse(case.text).offsetBy(east = case.east, north = case.north)?.text,
                label,
            )
        }
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
