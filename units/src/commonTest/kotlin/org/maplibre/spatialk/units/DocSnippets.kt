@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.units

import kotlin.math.PI
import kotlin.test.Test

// These snippets are primarily intended to be included in docs/turf.md. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

class DocSnippets {
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
        val manhattanBlock: Area = (1.miles / 20) * (1.miles / 7)
        val chicagoBlock: Area = 330.feet * 660.feet
        val ratio: Double = manhattanBlock / chicagoBlock
        // --8<-- [end:arithmetic]
    }

    // --8<-- [start:customUnits1]
    data object AmericanFootballField : AreaUnit {
        override val metersSquaredPerUnit: Double = 109.728 * 48.8
        override val symbol: String = "football fields"
    }

    // --8<-- [end:customUnits1]

    @Test
    fun customUnits() {
        // --8<-- [start:customUnits2]
        // how many football fields could fit on the earth's oceans?
        val earthRadius: Length = 6371.kilometers
        val earthSurface: Area = 4 * PI * earthRadius * earthRadius
        val oceanSurface: Area = 0.7 * earthSurface
        oceanSurface.roundToLong(AmericanFootballField)
        // --8<-- [end:customUnits2]
    }
}
