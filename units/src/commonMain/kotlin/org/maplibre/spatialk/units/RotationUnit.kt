package org.maplibre.spatialk.units

import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.inRadians

/**
 * Represents a unit of [Rotation] measurement.
 *
 * @property degreesPerUnit Conversion factor from this unit to degrees.
 * @see Rotation
 */
public open class RotationUnit(
    public val degreesPerUnit: Double,
    public override val symbol: String,
) : UnitOfMeasure, Comparable<RotationUnit> {

    /**
     * Converts this angular unit to a [LengthUnit] on the surface of a sphere, using the given
     * [World]'s average radius.
     */
    public fun asLengthUnitOn(world: World): LengthUnit =
        LengthUnit(degreesPerUnit.degrees.inRadians * world.averageRadius.inMeters, symbol)

    public final override fun compareTo(other: RotationUnit): Int =
        degreesPerUnit.compareTo(other.degreesPerUnit)
}
