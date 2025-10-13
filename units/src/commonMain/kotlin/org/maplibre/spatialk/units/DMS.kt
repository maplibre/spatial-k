package org.maplibre.spatialk.units

import kotlin.jvm.JvmField
import kotlin.math.PI

public data object DMS {
    @JvmField public val Degrees: RotationUnit = RotationUnit(PI / 180, "°")
    @JvmField public val ArcMinutes: RotationUnit = RotationUnit(PI / (180 * 60), "′")
    @JvmField public val ArcSeconds: RotationUnit = RotationUnit(PI / (180 * 60 * 60), "″")
}
