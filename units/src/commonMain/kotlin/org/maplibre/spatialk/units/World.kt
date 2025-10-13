package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

/**
 * Base class representing a celestial body with angular units converted to linear distance.
 *
 * @see Earth
 */
public abstract class World(
    /**
     * Radius for use with the Haversine formula. Approximated using a spherical (non-ellipsoid)
     * model.
     */
    public val averageRadius: Length
) {
    /** Radians [LengthUnit] on this world's surface. */
    @JvmField public val Radians: LengthUnit = International.Radians.asLengthUnitOn(this)

    /** Degrees [LengthUnit] on this world's surface. */
    @JvmField public val Degrees: LengthUnit = DMS.Degrees.asLengthUnitOn(this)

    /** Arc minutes [LengthUnit] on this world's surface. */
    @JvmField public val ArcMinutes: LengthUnit = DMS.ArcMinutes.asLengthUnitOn(this)

    /** Arc seconds [LengthUnit] on this world's surface. */
    @JvmField public val ArcSeconds: LengthUnit = DMS.ArcSeconds.asLengthUnitOn(this)
}
