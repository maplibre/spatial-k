package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

/** British Imperial and US Customary units of length. */
public data object Imperial {
    // Area
    @JvmField public val SquareInches: AreaUnit = AreaUnitImpl(.00064516, "in²")

    @JvmField public val SquareFeet: AreaUnit = AreaUnitImpl(0.09290304, "ft²")

    @JvmField public val SquareYards: AreaUnit = AreaUnitImpl(0.83612736, "yd²")

    @JvmField public val SquareMiles: AreaUnit = AreaUnitImpl(2_589_988.110336, "mi²")

    @JvmField public val SquareRods: AreaUnit = AreaUnitImpl(25.29285264, "rd²")

    @JvmField public val Acres: AreaUnit = AreaUnitImpl(4_046.8564224, "acre")

    // Length
    @JvmField
    public val Inches: LengthUnit = LengthUnitImpl(0.0254, "in", squaredUnit = SquareInches)

    @JvmField public val Feet: LengthUnit = LengthUnitImpl(0.3048, "ft", squaredUnit = SquareFeet)

    @JvmField public val Yards: LengthUnit = LengthUnitImpl(0.9144, "yd", squaredUnit = SquareYards)

    @JvmField
    public val Miles: LengthUnit = LengthUnitImpl(1_609.344, "mi", squaredUnit = SquareMiles)

    @JvmField public val Links: LengthUnit = LengthUnitImpl(0.201168, "link")

    @JvmField public val Rods: LengthUnit = LengthUnitImpl(5.0292, "rod", squaredUnit = SquareRods)

    @JvmField public val Chains: LengthUnit = LengthUnitImpl(20.1168, "ch")

    @JvmField public val Furlongs: LengthUnit = LengthUnitImpl(201.168, "fur")

    @JvmField public val Leagues: LengthUnit = LengthUnitImpl(4828.032, "lea")

    @JvmField public val Fathoms: LengthUnit = LengthUnitImpl(1.852, "fathom")

    @JvmField public val Cables: LengthUnit = LengthUnitImpl(185.2, "cable")

    @JvmField public val NauticalMiles: LengthUnit = LengthUnitImpl(1852.0, "nmi")
}
