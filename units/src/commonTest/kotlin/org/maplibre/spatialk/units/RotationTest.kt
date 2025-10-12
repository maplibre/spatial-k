package org.maplibre.spatialk.units

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.testutil.assertRotationEquals
import org.maplibre.spatialk.units.DMS.ArcMinutes
import org.maplibre.spatialk.units.DMS.ArcSeconds
import org.maplibre.spatialk.units.DMS.Degrees
import org.maplibre.spatialk.units.Metric.Gradians
import org.maplibre.spatialk.units.extensions.*

class RotationTest {

    // Basic unit conversion tests
    @Test
    fun testRadiansToDegrees() {
        assertEquals(180.0, PI.radians.inDegrees, 0.0001)
        assertEquals(360.0, (2 * PI).radians.inDegrees, 0.0001)
        assertEquals(0.0, 0.0.radians.inDegrees, 0.0001)
        assertEquals(-180.0, (-PI).radians.inDegrees, 0.0001)
        assertEquals(720.0, (4 * PI).radians.inDegrees, 0.0001)
        assertEquals(10.0, 0.174533.radians.inDegrees, 0.0001)
    }

    @Test
    fun testDegreesToRadians() {
        assertEquals(PI, 180.degrees.inRadians, 0.0001)
        assertEquals(2 * PI, 360.degrees.inRadians, 0.0001)
        assertEquals(PI / 2, 90.degrees.inRadians, 0.0001)
        assertEquals(PI / 4, 45.degrees.inRadians, 0.0001)
        assertEquals(-PI / 2, (-90).degrees.inRadians, 0.0001)
        assertEquals(3 * PI, 540.degrees.inRadians, 0.0001)
        assertEquals(0.174533, 10.0.degrees.inRadians, 0.0001)
    }

    @Test
    fun testGradians() {
        assertRotationEquals(90.degrees, 100.gradians)
    }

    @Test
    fun testDegreesMinutesSeconds() {
        assertEquals(60.0, 1.degrees.toDouble(ArcMinutes), 0.0001)
        assertEquals(3600.0, 1.degrees.toDouble(ArcSeconds), 0.0001)
        assertEquals(1.0, 60.arcMinutes.toDouble(Degrees), 0.0001)
    }

    // Rotation arithmetic tests
    @Test
    fun testRotationAddition() {
        assertEquals(180.degrees, 90.degrees + 90.degrees)
        assertEquals(360.degrees, 180.degrees + 180.degrees)
        assertEquals(450.degrees, 360.degrees + 90.degrees)
        assertEquals((-45).degrees, (-90).degrees + 45.degrees)
    }

    @Test
    fun testRotationSubtraction() {
        assertEquals(90.degrees, 180.degrees - 90.degrees)
        assertEquals(0.degrees, 90.degrees - 90.degrees)
        assertEquals((-90).degrees, 0.degrees - 90.degrees)
        assertEquals((-270).degrees, 90.degrees - 360.degrees)
    }

    @Test
    fun testRotationNegation() {
        assertEquals(-90.0, -(90.degrees).inDegrees, 0.0001)
        assertEquals(90.0, -((-90).degrees).inDegrees, 0.0001)
        assertEquals(-450.0, -((450).degrees).inDegrees, 0.0001)
        assertEquals(180.0, -((-180).degrees).inDegrees, 0.0001)
    }

    @Test
    fun testRotationModulo() {
        assertEquals(90.0, 450.degrees.mod(360.degrees).inDegrees, 0.0001)
        assertEquals(0.0, 720.degrees.mod(360.degrees).inDegrees, 0.0001)
        assertEquals(270.0, (-90).degrees.mod(360.degrees).inDegrees, 0.0001)
        assertEquals(90.0, 810.degrees.mod(360.degrees).inDegrees, 0.0001)
        assertEquals(180.0, (-540).degrees.mod(360.degrees).inDegrees, 0.0001)
    }

    @Test
    fun testToString() {
        assertEquals("0.00 rad", 0.degrees.toString())
        assertEquals("-1000.00°", (-1000).degrees.toString(Degrees))
        assertEquals("720.00°", 720.degrees.toString(Degrees))
        assertEquals("12°", 12.345.degrees.toString(unit = Degrees, decimalPlaces = 0))
        assertEquals("101 gr", 91.degrees.toString(unit = Gradians, decimalPlaces = 0))
    }

    @Test
    fun testToDmsString() {
        assertEquals("45° 30′ 0.00″", (45.degrees + 30.arcMinutes).toDmsString())
        assertEquals(
            "45° 30′ 30.5″",
            (45.degrees + 30.arcMinutes + 30.46.arcSeconds).toDmsString(1),
        )
        assertEquals("0° 0′ 0.00″", 0.degrees.toDmsString())
        assertEquals("-45° 30′ 0.00″", ((-45).degrees - 30.arcMinutes).toDmsString())
        assertEquals(
            "-45° 30′ 30.5″",
            ((-45).degrees - 30.arcMinutes - 30.46.arcSeconds).toDmsString(1),
        )
        assertEquals("90° 0′ 0.00″", 90.degrees.toDmsString(2))
    }
}
