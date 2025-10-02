package org.maplibre.spatialk.units

public interface LengthUnit : UnitOfMeasure, Comparable<LengthUnit> {
    public val metersPerUnit: Double

    public override fun compareTo(other: LengthUnit): Int =
        metersPerUnit.compareTo(other.metersPerUnit)

    public operator fun times(other: LengthUnit): AreaUnit =
        if (other == this) AreaUnitImpl(metersPerUnit * metersPerUnit, "$symbol²")
        else AreaUnitImpl(metersPerUnit * metersPerUnit, "${symbol}-${other.symbol}")
}
