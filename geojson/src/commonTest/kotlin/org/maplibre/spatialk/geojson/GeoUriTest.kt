package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.geojson.serialization.GeoUriParser

class GeoUriTest {
    @Test
    fun parsePosition_acceptsTwoCoords() {
        assertEquals(
            Position(latitudeDegrees = 1.0, longitudeDegrees = 2.0),
            GeoUriParser.parsePosition("geo:1.0,2.0"),
        )
    }

    @Test
    fun parsePosition_acceptsThreeCoords() {
        assertEquals(
            Position(latitudeDegrees = 1.0, longitudeDegrees = 2.0, altitudeMeters = 3.0),
            GeoUriParser.parsePosition("geo:1.0,2.0,3.0"),
        )
    }

    @Test
    fun parsePosition_rejectsOneCoord() {
        assertFailsWith<IllegalArgumentException> { GeoUriParser.parsePosition("geo:1.0") }
    }

    @Test
    fun parsePosition_rejectsFourCoords() {
        assertFailsWith<IllegalArgumentException> {
            GeoUriParser.parsePosition("geo:1.0,2.0,3.0,4.0")
        }
    }

    @Test
    fun parsePosition_rejectsInvalidScheme() {
        assertFailsWith<IllegalArgumentException> {
            GeoUriParser.parsePosition("geeeeooo:1.0,2.0,3.0")
        }
    }

    @Test
    fun parsePosition_acceptsParams() {
        assertEquals(
            Position(latitudeDegrees = 1.0, longitudeDegrees = 2.0, altitudeMeters = 3.0),
            GeoUriParser.parsePosition("geo:1.0,2.0,3.0;foo=bar;x=y;u"),
        )
    }

    @Test
    fun parsePosition_acceptsZeroUncertainty() {
        assertEquals(
            Position(latitudeDegrees = 1.0, longitudeDegrees = 2.0, altitudeMeters = 3.0),
            GeoUriParser.parsePosition("geo:1.0,2.0,3.0;u=0.0"),
        )
    }

    @Test
    fun parsePosition_rejectsNonZeroUncertainty() {
        assertFailsWith<IllegalArgumentException> {
            GeoUriParser.parsePosition("geo:1.0,2.0,3.0;u=0.5")
        }
    }

    @Test
    fun parsePosition_acceptsCrsWgs84() {
        assertEquals(
            Position(latitudeDegrees = 1.0, longitudeDegrees = 2.0, altitudeMeters = 3.0),
            GeoUriParser.parsePosition("geo:1.0,2.0,3.0;crs=wgs84"),
        )
    }

    @Test
    fun parsePosition_rejectsOtherCrs() {
        assertFailsWith<IllegalArgumentException> {
            GeoUriParser.parsePosition("geo:1.0,2.0,3.0;crs=wgs72")
        }
    }

    @Test
    fun parsePosition_rejectsMalformedParams() {
        assertFailsWith<IllegalArgumentException> {
            GeoUriParser.parsePosition("geeeeooo:1.0,2.0,3.0;malformed=")
        }
    }
}
