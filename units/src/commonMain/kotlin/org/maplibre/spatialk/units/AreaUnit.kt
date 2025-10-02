package org.maplibre.spatialk.units

public open class AreaUnit(
    public val metersSquaredPerUnit: Double,
    public override val symbol: String,
) : UnitOfMeasure, Comparable<AreaUnit> {
    public final override fun compareTo(other: AreaUnit): Int =
        metersSquaredPerUnit.compareTo(other.metersSquaredPerUnit)
}
