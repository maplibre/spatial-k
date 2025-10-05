package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

public abstract class World(
    /**
     * Radius for use with the Harvesine formula. Approximated using a spherical (non-ellipsoid)
     * world.
     */
    public val averageRadius: Length
) {
    @JvmField public val Radians: LengthUnit = International.Radians.asLengthUnitOn(this)
    @JvmField public val Degrees: LengthUnit = DMS.Degrees.asLengthUnitOn(this)
    @JvmField public val ArcMinutes: LengthUnit = DMS.ArcMinutes.asLengthUnitOn(this)
    @JvmField public val ArcSeconds: LengthUnit = DMS.ArcSeconds.asLengthUnitOn(this)
}
