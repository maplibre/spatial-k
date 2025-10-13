package org.maplibre.spatialk.units

/**
 * Represents a unit of [Area] measurement.
 *
 * @property metersSquaredPerUnit Conversion factor from this unit to square meters.
 * @see Area
 */
public open class AreaUnit(
    public val metersSquaredPerUnit: Double,
    public override val symbol: String,
) : UnitOfMeasure, Comparable<AreaUnit> {
    public final override fun compareTo(other: AreaUnit): Int =
        metersSquaredPerUnit.compareTo(other.metersSquaredPerUnit)
}
