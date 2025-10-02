package org.maplibre.spatialk.units

public interface LengthUnit : UnitOfMeasure, Comparable<LengthUnit> {
    public val metersPerUnit: Double

    public override fun compareTo(other: LengthUnit): Int =
        metersPerUnit.compareTo(other.metersPerUnit)

    public operator fun times(other: LengthUnit): AreaUnit =
        if (other == this) AreaUnitImpl(metersPerUnit * metersPerUnit, "$symbol²")
        else AreaUnitImpl(metersPerUnit * metersPerUnit, "${symbol}-${other.symbol}")
}

internal data class LengthUnitImpl(
    override val metersPerUnit: Double,
    override val symbol: String,
    private val squaredUnit: AreaUnit? = null,
) : LengthUnit {
    override operator fun times(other: LengthUnit): AreaUnit =
        if (squaredUnit != null && other == this) squaredUnit else super.times(other)
}
