package org.maplibre.spatialk.units

public interface AreaUnit : UnitOfMeasure, Comparable<AreaUnit> {
    public val metersSquaredPerUnit: Double

    public override fun compareTo(other: AreaUnit): Int =
        metersSquaredPerUnit.compareTo(other.metersSquaredPerUnit)
}
