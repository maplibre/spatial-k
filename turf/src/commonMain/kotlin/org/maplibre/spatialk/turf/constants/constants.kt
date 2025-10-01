@file:JvmName("Constants")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.constants

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.meters

/**
 * Radius of the Earth used with the Harvesine formula. Approximated using a spherical
 * (non-ellipsoid) Earth.
 */
public val EARTH_AVERAGE_RADIUS: Length = 6371008.8.meters

/** Radius of the Earth at the equator using the WGS84 ellipsoid. */
public val EARTH_EQUATOR_RADIUS: Length = 6378137.meters

internal const val ANTIMERIDIAN_POS = 180.0
internal const val ANTIMERIDIAN_NEG = -180.0
