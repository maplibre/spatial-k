package org.maplibre.spatialk.units

import kotlin.test.Test
import kotlin.test.assertEquals
import org.maplibre.spatialk.testutil.assertBearingEquals
import org.maplibre.spatialk.testutil.assertRotationEquals
import org.maplibre.spatialk.units.Bearing.Companion.East
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.Bearing.Companion.Northeast
import org.maplibre.spatialk.units.Bearing.Companion.South
import org.maplibre.spatialk.units.Bearing.Companion.Southeast
import org.maplibre.spatialk.units.Bearing.Companion.Southwest
import org.maplibre.spatialk.units.Bearing.Companion.West
import org.maplibre.spatialk.units.DMS.Degrees
import org.maplibre.spatialk.units.extensions.arcMinutes
import org.maplibre.spatialk.units.extensions.arcSeconds
import org.maplibre.spatialk.units.extensions.degrees

class BearingTest {

    @Test
    fun testBearingPlusRotation() {
        assertBearingEquals(East, North + 90.degrees)
        assertBearingEquals(South, North + 180.degrees)
        assertBearingEquals(East, South + 990.degrees)
        assertBearingEquals(West, South + 810.degrees)

        assertBearingEquals(East, 90.degrees + North)
        assertBearingEquals(South, 180.degrees + North)
        assertBearingEquals(East, 990.degrees + South)
        assertBearingEquals(West, 810.degrees + South)
    }

    @Test
    fun testBearingMinusRotation() {
        assertBearingEquals(West, North - 90.degrees)
        assertBearingEquals(South, North - 180.degrees)
        assertBearingEquals(West, South - 990.degrees)
    }

    @Test
    fun testBearingOverflow() {
        assertBearingEquals(East, North + 450.degrees)
        assertBearingEquals(Northeast, East + 675.degrees)
        assertBearingEquals(West, North - 90.degrees)
        assertBearingEquals(Northeast, East - 405.degrees)
        assertBearingEquals(North, North + 360.degrees)
        assertBearingEquals(North, North + 720.degrees)
    }

    @Test
    fun testBearingMinusBearing() {
        assertRotationEquals(90.degrees, East - North)
        assertRotationEquals(180.degrees, South - North)
        assertRotationEquals(90.degrees, South - East)
        assertRotationEquals(270.degrees, East - South)
        assertRotationEquals(0.degrees, North - North)
        assertRotationEquals(0.degrees, East - East)
        assertRotationEquals(10.degrees, South - (North + 170.degrees))
    }

    @Test
    fun testSmallestRotation() {
        assertRotationEquals(90.degrees, North.smallestRotationTo(East))
        assertRotationEquals(45.degrees, North.smallestRotationTo(Northeast))

        assertRotationEquals((-90).degrees, East.smallestRotationTo(North))
        assertRotationEquals((-45).degrees, Northeast.smallestRotationTo(North))

        assertRotationEquals((-135).degrees, Southeast.smallestRotationTo(North))
        assertRotationEquals(135.degrees, Southwest.smallestRotationTo(North))

        assertRotationEquals(180.degrees, South.smallestRotationTo(North))
        assertRotationEquals(180.degrees, West.smallestRotationTo(East))
    }

    @Test
    fun testSmallestRotationCrossesZero() {
        // From 350° to 10° should be +20° (not -340°)
        val bearing350 = North - 10.degrees
        val bearing10 = North + 10.degrees
        assertRotationEquals(20.degrees, bearing350.smallestRotationTo(bearing10))
        assertRotationEquals((-20).degrees, bearing10.smallestRotationTo(bearing350))

        // Test with large rotation inputs that still wrap to same positions
        val bearing350Alt = North - 370.degrees
        assertRotationEquals(20.degrees, bearing350Alt.smallestRotationTo(bearing10))
    }

    @Test
    fun testToString() {
        assertEquals("N 0.00° E", North.toString())
        assertEquals("S 90.00° E", East.toString())
        assertEquals("S 0.00° W", South.toString())
        assertEquals("N 90° W", West.toString(Degrees, 0))
        assertEquals("N 45.0° E", Northeast.toString(Degrees, 1))
        assertEquals("N 10.00° E", (North + 10.degrees).toString())
        assertEquals("S 10.00° E", (North + 170.degrees).toString())
        assertEquals("S 10.00° W", (South + 10.degrees).toString())
        assertEquals("N 10.00° W", (North - 10.degrees).toString())
    }

    @Test
    fun testToDmsString() {
        assertEquals("N 0° 0′ 0.00″ E", North.toDmsString())
        assertEquals("S 90° 0′ 0.00″ E", East.toDmsString())
        assertEquals("S 0° 0′ 0.00″ W", South.toDmsString())
        assertEquals("N 90° 0′ 0.00″ W", West.toDmsString())
        assertEquals("N 45° 0′ 0.00″ E", Northeast.toDmsString())
        assertEquals("N 10° 30′ 0.00″ E", (North + 10.degrees + 30.arcMinutes).toDmsString())
        assertEquals(
            "S 10° 30′ 30.5″ E",
            (North + 169.degrees + 29.arcMinutes + 29.5.arcSeconds).toDmsString(1),
        )
        assertEquals("S 9° 0′ 0.00″ W", (South + 9.degrees).toDmsString())
        assertEquals("N 12° 0′ 0.00″ W", (North - 12.degrees).toDmsString())
    }
}
