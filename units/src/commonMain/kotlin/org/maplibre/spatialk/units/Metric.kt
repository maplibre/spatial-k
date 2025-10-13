package org.maplibre.spatialk.units

import kotlin.jvm.JvmField
import kotlin.math.PI

/** Metric-based units not part of the International System of Units. */
public data object Metric {
    /** Centiares [AreaUnit] (1 square meter). */
    @JvmField public val Centiares: AreaUnit = AreaUnit(1.0, "ca")

    /** Deciares [AreaUnit] (10 square meters). */
    @JvmField public val Deciares: AreaUnit = AreaUnit(10.0, "da")

    /** Ares [AreaUnit] (100 square meters). */
    @JvmField public val Ares: AreaUnit = AreaUnit(100.0, "a")

    /** Decares [AreaUnit] (1000 square meters). */
    @JvmField public val Decares: AreaUnit = AreaUnit(1_000.0, "daa")

    /** Hectares [AreaUnit] (10000 square meters). */
    @JvmField public val Hectares: AreaUnit = AreaUnit(10_000.0, "ha")

    /** Gradians [RotationUnit] (400 gradians = 360 degrees). */
    @JvmField public val Gradians: RotationUnit = RotationUnit(PI / 200, "gr")
}
