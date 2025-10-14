package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

/** Degrees, Minutes, Seconds (DMS) angular measurement system. */
public data object DMS {
    /** Degrees [RotationUnit]. */
    @JvmField public val Degrees: RotationUnit = RotationUnit(1.0, "°")

    /** Arc minutes [RotationUnit] (1/60th of a degree). */
    @JvmField public val ArcMinutes: RotationUnit = RotationUnit(1.0 / 60, "′")

    /** Arc seconds [RotationUnit] (1/60th of an arc minute). */
    @JvmField public val ArcSeconds: RotationUnit = RotationUnit(1.0 / 60 / 60, "″")
}
