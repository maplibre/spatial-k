@file:Suppress("UnusedVariable", "unused")

package org.maplibre.spatialk.units

import kotlin.math.PI
import kotlin.test.Test
import org.maplibre.spatialk.units.catalog.Thailand
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

        val bearing: Bearing = Bearing.Northwest
        val clockwiseFromNorth: Double = (bearing - Bearing.North).inDegrees
        val signedFromNorth: Double = Bearing.North.smallestRotationTo(bearing).inDegrees
        // --8<-- [end:bearings]
    }

    @Test
    fun customFormats() {
        // --8<-- [start:customFormats]
        // Thai land area, such as 1 ไร่ 3 งาน 50 ตร.วา
        val thaiLand = AreaFormat {
            compound(" ", omitTrailingZeroParts = true) {
                part(Thailand.Rai)
                part(Thailand.Ngan)
                part(Thailand.SquareWa)
            }
            unit(Units.SquareMeters, aliases = listOf("m2"))
        }
        val plot: Area = thaiLand.parse("1 ไร่ 3 งาน 50 ตร.วา")
        val unknown: Area? = thaiLand.parseOrNull("unknown") // null
        println(thaiLand.format(3_200.squareMeters, Thailand.Rai, decimalPlaces = 0)) // 2 ไร่
        // --8<-- [end:customFormats]
    }

    @Test
    fun formatPresets() {
        // --8<-- [start:formatPresets]
        val angle: Rotation = RotationFormat.Dms.parse("12° 30′ 15″")
        val maxheight: Length = LengthFormat.Osm.parse("4.2") // meters by default

        // OSM tags railway track gauge in millimeters, like gauge=1435
        val gaugeFormat = LengthFormat(LengthFormat.Osm) { defaultUnit = Units.Millimeters }
        val gauge: Length = gaugeFormat.parse("1435")
        // --8<-- [end:formatPresets]
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
