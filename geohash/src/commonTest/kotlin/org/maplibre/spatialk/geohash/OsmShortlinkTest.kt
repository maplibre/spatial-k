package org.maplibre.spatialk.geohash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertDoubleEquals
import org.maplibre.spatialk.testutil.assertPositionEquals

class OsmShortlinkTest {
    @Test
    fun `parses the London shortlink`() {
        val shortlink = OsmShortlink.parse("https://osm.org/go/0EEQjE--")

        assertEquals("0EEQjE--", shortlink.text)
        assertEquals(9, shortlink.zoom)
        assertPositionEquals(
            Position(longitude = 0.0556182861328125, latitude = 51.51111602783203),
            shortlink.center,
        )
        assertDoubleEquals(0.054931640625, shortlink.boundingBox.west)
        assertDoubleEquals(51.510772705078125, shortlink.boundingBox.south)
        assertDoubleEquals(0.056304931640625, shortlink.boundingBox.east)
        assertDoubleEquals(51.51145935058594, shortlink.boundingBox.north)
        assertEquals(shortlink, OsmShortlink.of(shortlink.center, shortlink.zoom))
        assertEquals(shortlink.text, shortlink.toString())
    }

    @Test
    fun `encodes the OpenStreetMap Rails vector`() {
        val shortlink =
            OsmShortlink.of(
                Position(longitude = 0.054, latitude = 51.510),
                zoom = 9,
            )

        assertEquals("0EEQhq--", shortlink.text)
    }

    @Test
    fun `parses bare codes paths and supported URLs`() {
        val expected = OsmShortlink.parse("0EEQjE--")

        assertEquals(expected, OsmShortlink.parse("/go/0EEQjE--"))
        assertEquals(expected, OsmShortlink.parse("/go/0EEQjE--?m="))
        assertEquals(expected, OsmShortlink.parse("http://www.osm.org/go/0EEQjE--#map=9"))
        assertEquals(
            expected,
            OsmShortlink.parse("https://openstreetmap.org/go/0EEQjE--?m="),
        )
        assertEquals(
            expected,
            OsmShortlink.parse("HTTPS://WWW.OPENSTREETMAP.ORG/go/0EEQjE--"),
        )
        assertEquals(expected, OsmShortlink.parse("https://osm.org:443/go/0EEQjE--"))
    }

    @Test
    fun `normalizes historical shortlink characters`() {
        assertEquals("0EEQjE--", OsmShortlink.parse("0EEQjE==").text)
        assertEquals("0OP4tR~rx", OsmShortlink.parse("0OP4tR@rx").text)
    }

    @Test
    fun `supports the full zoom range`() {
        val position = Position(longitude = 12.345678, latitude = -45.678912, altitude = 50.0)
        val minimum = OsmShortlink.of(position, zoom = 0)
        val maximum = OsmShortlink.of(position, zoom = OsmShortlink.MaxZoom)

        assertEquals(0, minimum.zoom)
        assertEquals(OsmShortlink.MaxZoom, maximum.zoom)
        assertEquals(minimum, OsmShortlink.parse(minimum.text))
        assertEquals(maximum, OsmShortlink.parse(maximum.text))
        assertEquals(
            maximum,
            OsmShortlink.of(
                Position(
                    longitude = position.longitude,
                    latitude = position.latitude,
                ),
                zoom = OsmShortlink.MaxZoom,
            ),
        )
    }

    @Test
    fun `wraps OpenStreetMap edges the way Rails does`() {
        assertEquals(
            OsmShortlink.of(Position(longitude = -180.0, latitude = 0.0), zoom = 9),
            OsmShortlink.of(Position(longitude = 180.0, latitude = 0.0), zoom = 9),
        )
        assertEquals(
            OsmShortlink.of(Position(longitude = 0.0, latitude = -90.0), zoom = 9),
            OsmShortlink.of(Position(longitude = 0.0, latitude = 90.0), zoom = 9),
        )
    }

    @Test
    fun `rejects invalid zoom coordinates codes and URLs`() {
        val origin = Position(longitude = 0.0, latitude = 0.0)
        assertFailsWith<IllegalArgumentException> { OsmShortlink.of(origin, zoom = -1) }
        assertFailsWith<IllegalArgumentException> {
            OsmShortlink.of(origin, zoom = OsmShortlink.MaxZoom + 1)
        }
        assertFailsWith<IllegalArgumentException> {
            OsmShortlink.of(Position(longitude = Double.NaN, latitude = 0.0), zoom = 9)
        }
        assertFailsWith<IllegalArgumentException> {
            OsmShortlink.of(Position(longitude = 0.0, latitude = 90.1), zoom = 9)
        }

        listOf(
                "",
                "/go/",
                "AA",
                "ABC+--",
                "ABC---",
                "AB-C",
                "AAAAAAAAAAA",
                "https://example.com/go/0EEQjE--",
                "https://osm.org/map/0EEQjE--",
                "https://osm.org:evil/go/0EEQjE--",
                "https://osm.org:/go/0EEQjE--",
            )
            .forEach { text ->
                assertFailsWith<IllegalArgumentException>(text) { OsmShortlink.parse(text) }
                assertNull(OsmShortlink.parseOrNull(text), text)
            }
    }
}
