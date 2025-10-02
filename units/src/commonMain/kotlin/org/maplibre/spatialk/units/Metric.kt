package org.maplibre.spatialk.units

import kotlin.jvm.JvmStatic

/** Metric-based units not part of the The International System of Units. */
public data object Metric {
    @JvmStatic public val Centiares: AreaUnit = AreaUnitImpl(1.0, "ca")

    @JvmStatic public val Deciares: AreaUnit = AreaUnitImpl(10.0, "da")

    @JvmStatic public val Ares: AreaUnit = AreaUnitImpl(100.0, "a")

    @JvmStatic public val Decares: AreaUnit = AreaUnitImpl(1_000.0, "daa")

    @JvmStatic public val Hectares: AreaUnit = AreaUnitImpl(10_000.0, "ha")
}
