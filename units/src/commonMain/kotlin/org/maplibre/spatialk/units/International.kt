package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

/** The International System of Units (SI). */
public data object International {
    // Area
    /** Square millimeters [AreaUnit]. */
    @JvmField public val SquareMillimeters: AreaUnit = AreaUnit(0.000001, "mm²")

    /** Square centimeters [AreaUnit]. */
    @JvmField public val SquareCentimeters: AreaUnit = AreaUnit(0.0001, "cm²")

    /** Square meters [AreaUnit]. */
    @JvmField public val SquareMeters: AreaUnit = AreaUnit(1.0, "m²")

    /** Square kilometers [AreaUnit]. */
    @JvmField public val SquareKilometers: AreaUnit = AreaUnit(1_000_000.0, "km²")

    // Length
    /** Millimeters [LengthUnit]. */
    @JvmField
    public val Millimeters: LengthUnit = LengthUnit(0.001, "mm", squaredUnit = SquareMillimeters)

    /** Centimeters [LengthUnit]. */
    @JvmField
    public val Centimeters: LengthUnit = LengthUnit(0.01, "cm", squaredUnit = SquareCentimeters)

    /** Meters [LengthUnit]. */
    @JvmField public val Meters: LengthUnit = LengthUnit(1.0, "m", squaredUnit = SquareMeters)

    /** Kilometers [LengthUnit]. */
    @JvmField
    public val Kilometers: LengthUnit = LengthUnit(1_000.0, "km", squaredUnit = SquareKilometers)

    /** Radians [RotationUnit]. */
    @JvmField public val Radians: RotationUnit = RotationUnit(1.0, "rad")
}
