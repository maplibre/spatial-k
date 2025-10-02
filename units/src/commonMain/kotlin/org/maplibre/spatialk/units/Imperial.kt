package org.maplibre.spatialk.units

import kotlin.jvm.JvmStatic
import org.maplibre.spatialk.units.Imperial.Acres
import org.maplibre.spatialk.units.Imperial.Feet
import org.maplibre.spatialk.units.Imperial.Miles
import org.maplibre.spatialk.units.Imperial.SquareFeet
import org.maplibre.spatialk.units.Imperial.SquareMiles

/** British Imperial and US Customary units of length. */
public data object Imperial {
    // Area
    @JvmStatic public val SquareInches: AreaUnit = AreaUnitImpl(.00064516, "in²")

    @JvmStatic public val SquareFeet: AreaUnit = AreaUnitImpl(0.09290304, "ft²")

    @JvmStatic public val SquareYards: AreaUnit = AreaUnitImpl(0.83612736, "yd²")

    @JvmStatic public val SquareMiles: AreaUnit = AreaUnitImpl(2_589_988.110336, "mi²")

    @JvmStatic public val SquareRods: AreaUnit = AreaUnitImpl(25.29285264, "rd²")

    @JvmStatic public val Acres: AreaUnit = AreaUnitImpl(4_046.8564224, "acre")

    // Length
    @JvmStatic
    public val Inches: LengthUnit = LengthUnitImpl(0.0254, "in", squaredUnit = SquareInches)

    @JvmStatic public val Feet: LengthUnit = LengthUnitImpl(0.3048, "ft", squaredUnit = SquareFeet)

    @JvmStatic
    public val Yards: LengthUnit = LengthUnitImpl(0.9144, "yd", squaredUnit = SquareYards)

    @JvmStatic
    public val Miles: LengthUnit = LengthUnitImpl(1_609.344, "mi", squaredUnit = SquareMiles)

    @JvmStatic public val Links: LengthUnit = LengthUnitImpl(0.201168, "link")

    @JvmStatic public val Rods: LengthUnit = LengthUnitImpl(5.0292, "rod", squaredUnit = SquareRods)

    @JvmStatic public val Chains: LengthUnit = LengthUnitImpl(20.1168, "ch")

    @JvmStatic public val Furlongs: LengthUnit = LengthUnitImpl(201.168, "fur")

    @JvmStatic public val Leagues: LengthUnit = LengthUnitImpl(4828.032, "lea")

    @JvmStatic public val Fathoms: LengthUnit = LengthUnitImpl(1.852, "fathom")

    @JvmStatic public val Cables: LengthUnit = LengthUnitImpl(185.2, "cable")

    @JvmStatic public val NauticalMiles: LengthUnit = LengthUnitImpl(1852.0, "nmi")
}

public inline val Number.feet: Length
    get() = toLength(Feet)

public inline val Number.miles: Length
    get() = toLength(Miles)

public inline val Number.squareFeet: Area
    get() = toArea(SquareFeet)

public inline val Number.squareMiles: Area
    get() = toArea(SquareMiles)

public inline val Number.acres: Area
    get() = toArea(Acres)

public inline val Length.inFeet: Double
    get() = toDouble(Feet)

public inline val Length.inMiles: Double
    get() = toDouble(Miles)

public inline val Area.inSquareFeet: Double
    get() = toDouble(SquareFeet)

public inline val Area.inSquareMiles: Double
    get() = toDouble(SquareMiles)

public inline val Area.inAcres: Double
    get() = toDouble(Acres)
