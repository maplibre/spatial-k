package org.maplibre.spatialk.units

import org.maplibre.spatialk.units.extensions.meters

/** Earth as a [World] with standard WGS84-based measurements. */
public data object Earth : World(averageRadius = 6371008.8.meters) {
    /** Radius of the Earth at the equator using the WGS84 ellipsoid. */
    public val equatorRadius: Length = 6378137.meters
}
