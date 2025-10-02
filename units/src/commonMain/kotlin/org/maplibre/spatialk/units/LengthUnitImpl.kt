package org.maplibre.spatialk.units

internal data class LengthUnitImpl(
    override val metersPerUnit: Double,
    override val symbol: String,
    private val squaredUnit: AreaUnit? = null,
) : LengthUnit {
    override operator fun times(other: LengthUnit): AreaUnit =
        if (squaredUnit != null && other == this) squaredUnit else super.times(other)
}
