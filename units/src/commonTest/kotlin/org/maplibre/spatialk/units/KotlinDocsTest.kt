@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.units

import kotlin.math.PI
import kotlin.test.Test
import org.maplibre.spatialk.units.extensions.*

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class KotlinDocsTest {
    @Test
    fun conversion() {
        // --8<-- [start:conversion]
        val distance: Length = 123.miles
        println(distance.inKilometers)

        val area: Area = 45.acres
        println(area.inSquareMeters)
        // --8<-- [end:conversion]
    }

    @Test
    fun arithmetic() {
        // --8<-- [start:arithmetic]
        val manhattanBlock: Area = (1.miles / 20.0) * (1.miles / 7.0)
        val chicagoBlock: Area = 330.feet * 660.feet
        val ratio: Double = manhattanBlock / chicagoBlock
        // --8<-- [end:arithmetic]
    }

    @Test
    fun bearings() {
        // --8<-- [start:bearings]
        val heading: Bearing = Bearing.North
        val turnedRight: Bearing = heading + 90.degrees
        val turnedLeft: Bearing = heading - 45.degrees
        val diff: Rotation = turnedRight - turnedLeft
        // --8<-- [end:bearings]
    }

    @Test
    fun customUnits() {
        // --8<-- [start:customUnits]
        // how many football fields could fit on the earth's oceans?
        val americanFootballField = AreaUnit(109.728 * 48.8, "football fields")
        val earthRadius: Length = 6371.kilometers
        val earthSurface: Area = 4 * PI * earthRadius * earthRadius
        val oceanSurface: Area = 0.7 * earthSurface
        val result = oceanSurface.roundToLong(americanFootballField)
        // --8<-- [end:customUnits]
    }
}
