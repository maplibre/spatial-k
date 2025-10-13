package org.maplibre.spatialk.units

import org.maplibre.spatialk.units.extensions.inMeters

/**
 * Represents a unit of [Rotation] measurement.
 *
 * @property radiansPerUnit Conversion factor from this unit to radians.
 * @see Rotation
 */
public open class RotationUnit(
    public val radiansPerUnit: Double,
    public override val symbol: String,
) : UnitOfMeasure, Comparable<RotationUnit> {

    /**
     * Converts this angular unit to a [LengthUnit] on the surface of a sphere, using the given
     * [World]'s average radius.
     */
    public fun asLengthUnitOn(world: World): LengthUnit =
        LengthUnit(radiansPerUnit * world.averageRadius.inMeters, symbol)

    public final override fun compareTo(other: RotationUnit): Int =
        radiansPerUnit.compareTo(other.radiansPerUnit)
}
