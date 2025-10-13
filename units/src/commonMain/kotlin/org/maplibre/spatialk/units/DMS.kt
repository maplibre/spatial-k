package org.maplibre.spatialk.units

import kotlin.jvm.JvmField
import kotlin.math.PI

/** Degrees, Minutes, Seconds (DMS) angular measurement system. */
public data object DMS {
    /** Degrees [RotationUnit]. */
    @JvmField public val Degrees: RotationUnit = RotationUnit(PI / 180, "°")

    /** Arc minutes [RotationUnit] (1/60th of a degree). */
    @JvmField public val ArcMinutes: RotationUnit = RotationUnit(PI / (180 * 60), "′")

    /** Arc seconds [RotationUnit] (1/60th of an arc minute). */
    @JvmField public val ArcSeconds: RotationUnit = RotationUnit(PI / (180 * 60 * 60), "″")
}
