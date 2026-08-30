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
    fun `encodes generated shortlink vectors`() {
        GeohashFixtures.osmEncode.forEach { case ->
            val label = "${case.name} ${case.text}"
            assertEquals(
                case.text,
                OsmShortlink.of(
                        Position(longitude = case.longitude, latitude = case.latitude),
                        zoom = case.zoom,
                    )
                    .text,
                label,
            )
        }
    }

    @Test
    fun `parses imported shortlink vectors`() {
        GeohashFixtures.osmParse.forEach { case ->
            val label = "${case.source} ${case.input}"
            val shortlink = OsmShortlink.parse(case.input)
            assertEquals(case.text, shortlink.text, label)
            assertEquals(case.text, shortlink.toString(), label)
            if (case.zoom != null) {
                assertEquals(case.zoom, shortlink.zoom, label)
                assertEquals(shortlink, OsmShortlink.of(shortlink.center, shortlink.zoom), label)
            }
            if (case.longitude != null && case.latitude != null) {
                assertPositionEquals(
                    Position(longitude = case.longitude, latitude = case.latitude),
                    shortlink.center,
                    message = label,
                )
            }
            if (
                case.west != null && case.south != null && case.east != null && case.north != null
            ) {
                assertDoubleEquals(case.west, shortlink.boundingBox.west, message = label)
                assertDoubleEquals(case.south, shortlink.boundingBox.south, message = label)
                assertDoubleEquals(case.east, shortlink.boundingBox.east, message = label)
                assertDoubleEquals(case.north, shortlink.boundingBox.north, message = label)
            }
        }
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
                "https://osm.org:\uFF11\uFF12/go/0EEQjE--",
            )
            .forEach { text ->
                assertFailsWith<IllegalArgumentException>(text) { OsmShortlink.parse(text) }
                assertNull(OsmShortlink.parseOrNull(text), text)
            }
    }
}
