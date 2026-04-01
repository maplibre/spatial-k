package org.maplibre.spatialk.polyline

import kotlin.math.pow
import kotlin.math.roundToLong
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals

class PolylineEncodingTest {

    private val positions = listOf(
        Position(latitude = 0.0, longitude = 0.0),
        Position(latitude = 1.2345678, longitude = 2.3456789),
        Position(latitude = -3.3, longitude = -3.3),
        Position(latitude = 0.0, longitude = 0.0),
    )
    private val encodedPolyline5 = "??acpFociM`ttZntma@_pcS_pcS"
    private val encodedPolyline6 = "??ogjjA}kdnCnqwsG|uqwI_ilhE_ilhE"

    @Test
    fun `Test encode with precision 5`() {
        val encoded = PolylineEncoding.encode(positions, 5)
        assertEquals(encodedPolyline5, encoded)
    }

    @Test
    fun `Test decode with precision 5`() {
        val decodedPositions = PolylineEncoding.decode(encodedPolyline5, 5)
        assertEquals(positions.round(5), decodedPositions)
    }

    @Test
    fun `Test encode with precision 6`() {
        val encoded = PolylineEncoding.encode(positions, 6)
        assertEquals(encodedPolyline6, encoded)
    }

    @Test
    fun `Test decode with precision 6`() {
        val decodedPositions = PolylineEncoding.decode(encodedPolyline6, 6)
        assertEquals(positions.round(6), decodedPositions)
    }

    private fun List<Position>.round(precision: Int) = map {
        val factor = 10.0.pow(precision)
        Position(
            latitude = (it.latitude * factor).roundToLong() / factor,
            longitude = (it.longitude * factor).roundToLong() / factor,
        )
    }
}
