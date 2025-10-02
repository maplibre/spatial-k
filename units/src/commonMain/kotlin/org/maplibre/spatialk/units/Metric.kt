package org.maplibre.spatialk.units

import kotlin.jvm.JvmField

/** Metric-based units not part of the The International System of Units. */
public data object Metric {
    @JvmField public val Centiares: AreaUnit = AreaUnitImpl(1.0, "ca")

    @JvmField public val Deciares: AreaUnit = AreaUnitImpl(10.0, "da")

    @JvmField public val Ares: AreaUnit = AreaUnitImpl(100.0, "a")

    @JvmField public val Decares: AreaUnit = AreaUnitImpl(1_000.0, "daa")

    @JvmField public val Hectares: AreaUnit = AreaUnitImpl(10_000.0, "ha")
}
