package org.maplibre.spatialk.units

internal data class AreaUnitImpl(
    override val metersSquaredPerUnit: Double,
    override val symbol: String,
) : AreaUnit
