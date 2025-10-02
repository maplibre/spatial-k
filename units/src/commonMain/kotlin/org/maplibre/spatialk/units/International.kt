package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

/** The International System of Units */
public data object International {
    // Area
    @JvmField public val SquareMillimeters: AreaUnit = AreaUnit(0.000001, "mm²")

    @JvmField public val SquareCentimeters: AreaUnit = AreaUnit(0.0001, "cm²")

    @JvmField public val SquareMeters: AreaUnit = AreaUnit(1.0, "m²")

    @JvmField public val SquareKilometers: AreaUnit = AreaUnit(1_000_000.0, "km²")

    // Length
    @JvmField
    public val Millimeters: LengthUnit = LengthUnit(0.001, "mm", squaredUnit = SquareMillimeters)

    @JvmField
    public val Centimeters: LengthUnit = LengthUnit(0.01, "cm", squaredUnit = SquareCentimeters)

    @JvmField public val Meters: LengthUnit = LengthUnit(1.0, "m", squaredUnit = SquareMeters)

    @JvmField
    public val Kilometers: LengthUnit = LengthUnit(1_000.0, "km", squaredUnit = SquareKilometers)
}
