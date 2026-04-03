package org.maplibre.spatialk.polyline

import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.maplibre.spatialk.geojson.Position

class PolylineEncodingTest {

    // Google's canonical example: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
    private val googleExamplePositions = listOf(
        Position(latitude = 38.5, longitude = -120.2),
        Position(latitude = 40.7, longitude = -120.95),
        Position(latitude = 43.252, longitude = -126.453),
    )
    private val googleExampleEncoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"

    private val multiPrecisionPositions = listOf(
        Position(latitude = 0.0, longitude = 0.0),
        Position(latitude = 1.2345678, longitude = 2.3456789),
        Position(latitude = -3.3, longitude = -3.3),
        Position(latitude = 0.0, longitude = 0.0),
    )
    private val encodedPolyline5 = "??acpFociM`ttZntma@_pcS_pcS"
    private val encodedPolyline6 = "??ogjjA}kdnCnqwsG|uqwI_ilhE_ilhE"

    // --- encode ---

    @Test
    fun `encode Google canonical example`() {
        assertEquals(googleExampleEncoded, PolylineEncoding.encode(googleExamplePositions))
    }

    @Test
    fun `encode with precision 5`() {
        assertEquals(encodedPolyline5, PolylineEncoding.encode(multiPrecisionPositions, 5))
    }

    @Test
    fun `encode with precision 6`() {
        assertEquals(encodedPolyline6, PolylineEncoding.encode(multiPrecisionPositions, 6))
    }

    @Test
    fun `encode empty list`() {
        assertEquals("", PolylineEncoding.encode(emptyList()))
    }

    @Test
    fun `encode single position`() {
        val positions = listOf(Position(latitude = 48.8566, longitude = 2.3522))
        val encoded = PolylineEncoding.encode(positions)
        assertEquals(positions.round(5), PolylineEncoding.decode(encoded))
    }

    @Test
    fun `encode positions with negative coordinates`() {
        val positions = listOf(
            Position(latitude = -33.8688, longitude = -70.6693),
            Position(latitude = -34.6037, longitude = -58.3816),
        )
        assertEquals(positions.round(5), PolylineEncoding.decode(PolylineEncoding.encode(positions)))
    }

    @Test
    fun `encode boundary coordinates`() {
        val positions = listOf(
            Position(latitude = 90.0, longitude = 180.0),
            Position(latitude = -90.0, longitude = -180.0),
        )
        assertEquals(positions.round(5), PolylineEncoding.decode(PolylineEncoding.encode(positions)))
    }

    // --- decode ---

    @Test
    fun `decode Google canonical example`() {
        assertEquals(googleExamplePositions, PolylineEncoding.decode(googleExampleEncoded))
    }

    @Test
    fun `decode with precision 5`() {
        assertEquals(multiPrecisionPositions.round(5), PolylineEncoding.decode(encodedPolyline5, 5))
    }

    @Test
    fun `decode with precision 6`() {
        assertEquals(multiPrecisionPositions.round(6), PolylineEncoding.decode(encodedPolyline6, 6))
    }

    @Test
    fun `decode empty string`() {
        assertEquals(emptyList(), PolylineEncoding.decode(""))
    }

    @Test
    fun `decode single position`() {
        val positions = listOf(Position(latitude = 0.0, longitude = 0.0))
        assertEquals(positions, PolylineEncoding.decode(PolylineEncoding.encode(positions)))
    }

    // --- round-trip ---

    @Test
    fun `round-trip encode then decode precision 5`() {
        assertEquals(
            multiPrecisionPositions.round(5),
            PolylineEncoding.decode(PolylineEncoding.encode(multiPrecisionPositions, 5), 5),
        )
    }

    @Test
    fun `round-trip encode then decode precision 6`() {
        assertEquals(
            multiPrecisionPositions.round(6),
            PolylineEncoding.decode(PolylineEncoding.encode(multiPrecisionPositions, 6), 6),
        )
    }

    // --- malformed input (bugs 1 & 2) ---

    @Test
    fun `decode truncated string with single value throws IllegalArgumentException`() {
        // "?" encodes delta 0 for latitude, then there is no longitude value — must throw
        assertFailsWith<IllegalArgumentException> {
            PolylineEncoding.decode("?")
        }
    }

    @Test
    fun `decode string truncated mid-value throws IllegalArgumentException`() {
        // Take the Google example and drop the last character, creating a truncated multi-byte value
        val truncated = googleExampleEncoded.dropLast(1)
        assertFailsWith<IllegalArgumentException> {
            PolylineEncoding.decode(truncated)
        }
    }

    @Test
    fun `decodeOrNull returns null on truncated input`() {
        assertNull(PolylineEncoding.decodeOrNull("?"))
    }

    @Test
    fun `decodeOrNull returns result on valid input`() {
        assertEquals(
            googleExamplePositions,
            PolylineEncoding.decodeOrNull(googleExampleEncoded),
        )
    }

    // --- helpers ---

    private fun List<Position>.round(precision: Int) = map {
        val factor = 10.0.pow(precision)
        Position(
            latitude = (it.latitude * factor).roundToLong() / factor,
            longitude = (it.longitude * factor).roundToLong() / factor,
        )
    }
}
