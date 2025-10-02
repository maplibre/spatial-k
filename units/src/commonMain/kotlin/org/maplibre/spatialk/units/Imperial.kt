package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

/** British Imperial and US Customary units of length. */
public data object Imperial {
    // Area
    @JvmField public val SquareInches: AreaUnit = AreaUnit(.00064516, "in²")

    @JvmField public val SquareFeet: AreaUnit = AreaUnit(0.09290304, "ft²")

    @JvmField public val SquareYards: AreaUnit = AreaUnit(0.83612736, "yd²")

    @JvmField public val SquareMiles: AreaUnit = AreaUnit(2_589_988.110336, "mi²")

    @JvmField public val SquareRods: AreaUnit = AreaUnit(25.29285264, "rd²")

    @JvmField public val Acres: AreaUnit = AreaUnit(4_046.8564224, "acre")

    // Length
    @JvmField public val Inches: LengthUnit = LengthUnit(0.0254, "in", squaredUnit = SquareInches)

    @JvmField public val Feet: LengthUnit = LengthUnit(0.3048, "ft", squaredUnit = SquareFeet)

    @JvmField public val Yards: LengthUnit = LengthUnit(0.9144, "yd", squaredUnit = SquareYards)

    @JvmField public val Miles: LengthUnit = LengthUnit(1_609.344, "mi", squaredUnit = SquareMiles)

    @JvmField public val Links: LengthUnit = LengthUnit(0.201168, "link")

    @JvmField public val Rods: LengthUnit = LengthUnit(5.0292, "rod", squaredUnit = SquareRods)

    @JvmField public val Chains: LengthUnit = LengthUnit(20.1168, "ch")

    @JvmField public val Furlongs: LengthUnit = LengthUnit(201.168, "fur")

    @JvmField public val Leagues: LengthUnit = LengthUnit(4828.032, "lea")

    @JvmField public val Fathoms: LengthUnit = LengthUnit(1.852, "fathom")

    @JvmField public val Cables: LengthUnit = LengthUnit(185.2, "cable")

    @JvmField public val NauticalMiles: LengthUnit = LengthUnit(1852.0, "nmi")
}
