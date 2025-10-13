package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

/** British Imperial and US Customary units. */
public data object Imperial {
    // Area
    /** Square inches [AreaUnit]. */
    @JvmField public val SquareInches: AreaUnit = AreaUnit(.00064516, "in²")

    /** Square feet [AreaUnit]. */
    @JvmField public val SquareFeet: AreaUnit = AreaUnit(0.09290304, "ft²")

    /** Square yards [AreaUnit]. */
    @JvmField public val SquareYards: AreaUnit = AreaUnit(0.83612736, "yd²")

    /** Square miles [AreaUnit]. */
    @JvmField public val SquareMiles: AreaUnit = AreaUnit(2_589_988.110336, "mi²")

    /** Square rods [AreaUnit]. */
    @JvmField public val SquareRods: AreaUnit = AreaUnit(25.29285264, "rd²")

    /** Acres [AreaUnit]. */
    @JvmField public val Acres: AreaUnit = AreaUnit(4_046.8564224, "acre")

    // Length
    /** Inches [LengthUnit]. */
    @JvmField public val Inches: LengthUnit = LengthUnit(0.0254, "in", squaredUnit = SquareInches)

    /** Feet [LengthUnit]. */
    @JvmField public val Feet: LengthUnit = LengthUnit(0.3048, "ft", squaredUnit = SquareFeet)

    /** Yards [LengthUnit]. */
    @JvmField public val Yards: LengthUnit = LengthUnit(0.9144, "yd", squaredUnit = SquareYards)

    /** Miles [LengthUnit]. */
    @JvmField public val Miles: LengthUnit = LengthUnit(1_609.344, "mi", squaredUnit = SquareMiles)

    /** Surveyor's links [LengthUnit]. */
    @JvmField public val Links: LengthUnit = LengthUnit(0.201168, "link")

    /** Rods [LengthUnit] (also known as perches or poles). */
    @JvmField public val Rods: LengthUnit = LengthUnit(5.0292, "rod", squaredUnit = SquareRods)

    /** Surveyor's chains [LengthUnit]. */
    @JvmField public val Chains: LengthUnit = LengthUnit(20.1168, "ch")

    /** Furlongs [LengthUnit]. */
    @JvmField public val Furlongs: LengthUnit = LengthUnit(201.168, "fur")

    /** Leagues [LengthUnit]. */
    @JvmField public val Leagues: LengthUnit = LengthUnit(4828.032, "lea")

    /** Fathoms [LengthUnit] (nautical depth measurement). */
    @JvmField public val Fathoms: LengthUnit = LengthUnit(1.852, "fathom")

    /** Cables [LengthUnit] (nautical measurement). */
    @JvmField public val Cables: LengthUnit = LengthUnit(185.2, "cable")

    /** Nautical miles [LengthUnit]. */
    @JvmField public val NauticalMiles: LengthUnit = LengthUnit(1852.0, "nmi")
}
