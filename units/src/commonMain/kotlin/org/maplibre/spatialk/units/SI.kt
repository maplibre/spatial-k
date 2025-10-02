package org.maplibre.spatialk.units

import kotlin.jvm.JvmStatic
import org.maplibre.spatialk.units.SI.Kilometers
import org.maplibre.spatialk.units.SI.Meters
import org.maplibre.spatialk.units.SI.SquareKilometers
import org.maplibre.spatialk.units.SI.SquareMeters

/** The International System of Units */
public data object SI {
    // Area
    @JvmStatic public val SquareMillimeters: AreaUnit = AreaUnitImpl(0.000001, "mm²")

    @JvmStatic public val SquareCentimeters: AreaUnit = AreaUnitImpl(0.0001, "cm²")

    @JvmStatic public val SquareMeters: AreaUnit = AreaUnitImpl(1.0, "m²")

    @JvmStatic public val SquareKilometers: AreaUnit = AreaUnitImpl(1_000_000.0, "km²")

    // Length
    @JvmStatic
    public val Millimeters: LengthUnit =
        LengthUnitImpl(0.001, "mm", squaredUnit = SquareMillimeters)

    @JvmStatic
    public val Centimeters: LengthUnit = LengthUnitImpl(0.01, "cm", squaredUnit = SquareCentimeters)

    @JvmStatic public val Meters: LengthUnit = LengthUnitImpl(1.0, "m", squaredUnit = SquareMeters)

    @JvmStatic
    public val Kilometers: LengthUnit =
        LengthUnitImpl(1_000.0, "km", squaredUnit = SquareKilometers)
}

public inline val Number.squareMeters: Area
    get() = toArea(SquareMeters)

public inline val Area.inSquareMeters: Double
    get() = toDouble(SquareMeters)

public inline val Number.squareKilometers: Area
    get() = toArea(SquareKilometers)

public inline val Area.inSquareKilometers: Double
    get() = toDouble(SquareKilometers)

public inline val Number.meters: Length
    get() = toLength(Meters)

public inline val Length.inMeters: Double
    get() = toDouble(Meters)

public inline val Number.kilometers: Length
    get() = toLength(Kilometers)

public inline val Length.inKilometers: Double
    get() = toDouble(Kilometers)
