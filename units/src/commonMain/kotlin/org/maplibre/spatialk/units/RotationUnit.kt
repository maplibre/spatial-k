package org.maplibre.spatialk.units

import org.maplibre.spatialk.units.extensions.inMeters

public open class RotationUnit(
    public val radiansPerUnit: Double,
    public override val symbol: String,
) : UnitOfMeasure, Comparable<RotationUnit> {

    /**
     * This angular unit as a unit of length on the surface of a sphere, approximated using
     * [World.averageRadius].
     */
    public fun asLengthUnitOn(world: World): LengthUnit =
        LengthUnit(radiansPerUnit * world.averageRadius.inMeters, symbol)

    public final override fun compareTo(other: RotationUnit): Int =
        radiansPerUnit.compareTo(other.radiansPerUnit)
}
