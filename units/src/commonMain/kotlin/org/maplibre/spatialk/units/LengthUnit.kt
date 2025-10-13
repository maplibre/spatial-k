package org.maplibre.spatialk.units

import kotlin.jvm.JvmOverloads

/**
 * Represents a unit of [Length] measurement.
 *
 * @property metersPerUnit Conversion factor from this unit to meters.
 * @see Length
 */
public open class LengthUnit
@JvmOverloads
public constructor(
    public val metersPerUnit: Double,
    public override val symbol: String,
    private val squaredUnit: AreaUnit? = null,
) : UnitOfMeasure, Comparable<LengthUnit> {

    public final override fun compareTo(other: LengthUnit): Int =
        metersPerUnit.compareTo(other.metersPerUnit)

    /** Multiplies two [LengthUnit]s to produce an [AreaUnit]. */
    public operator fun times(other: LengthUnit): AreaUnit =
        if (other == this) squaredUnit ?: AreaUnit(metersPerUnit * metersPerUnit, "$symbol²")
        else AreaUnit(metersPerUnit * metersPerUnit, "${symbol}-${other.symbol}")
}
