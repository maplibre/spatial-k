package org.maplibre.spatialk.units

import kotlin.jvm.JvmField
import kotlin.math.PI

/**
 * Angular units measuring the length of an arc on the surface of the earth; approximated using the
 * average radius of the Earth (= 6,371.0088 km).
 */
public data object Geodesy {
    @JvmField public val Radians: LengthUnit = LengthUnit(6_371_008.8, "rad")

    @JvmField public val Degrees: LengthUnit = LengthUnit(Radians.metersPerUnit * PI / 180, "°")

    @JvmField
    public val Minutes: LengthUnit = LengthUnit(Degrees.metersPerUnit * PI / (180 * 60), "arcmin")

    @JvmField
    public val Seconds: LengthUnit =
        LengthUnit(Minutes.metersPerUnit * PI / (180 * 60 * 60), "arcsec")
}
