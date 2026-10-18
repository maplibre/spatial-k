package org.maplibre.spatialk.geojson

/**
 * Returns a position with longitude normalized into [-180, 180), mapping 180° to -180°. Latitude,
 * altitude, and any additional coordinate elements are unchanged. A non-finite longitude becomes
 * NaN.
 */
public fun Position.wrapped(): Position =
    Position(coordinates.copyOf().also { it[0] = wrapLongitude(it[0]) })

/**
 * Returns non-crossing boxes with longitudes in [-180, 180], preserving all other coordinate
 * elements, including altitude bounds.
 *
 * Accepts RFC crossings (170° to -170°) and continuous bounds (170° to 190°). A crossing returns
 * wrapped west to 180°, followed by -180° to wrapped east. Touching ±180° adds no zero-width box.
 *
 * An already non-crossing box within [-180, 180] returns `listOf(this)`; a box wholly in another
 * world copy returns one normalized box. A continuous span of at least 360° returns one full-world
 * box from -180° to 180°.
 *
 * When east is less than west, the eastward span is taken modulo 360°. Equivalent endpoints such as
 * 180° to -180° describe a zero-width box.
 *
 * @throws IllegalArgumentException if either longitude is non-finite.
 */
public fun BoundingBox.splitAtAntimeridian(): List<BoundingBox> {
    val west = coordinates[0]
    val east = coordinates[size / 2]
    require(west.isFinite() && east.isFinite()) { "Longitudes must be finite" }

    if (west in -180.0..180.0 && east in west..180.0) return listOf(this)
    if (east - west >= 360.0) return listOf(withLongitudes(-180.0, 180.0))

    val wrappedWest = wrapLongitude(west)
    val wrappedEast = wrapLongitude(east)
    val end = if (wrappedEast == -180.0 && wrappedWest > -180.0) 180.0 else wrappedEast
    return if (end >= wrappedWest) listOf(withLongitudes(wrappedWest, end))
    else listOf(withLongitudes(wrappedWest, 180.0), withLongitudes(-180.0, end))
}

private fun wrapLongitude(longitude: Double): Double {
    val remainder = longitude % 360.0
    return when {
        remainder < -180.0 -> remainder + 360.0
        remainder >= 180.0 -> remainder - 360.0
        else -> remainder
    }
}

private fun BoundingBox.withLongitudes(west: Double, east: Double): BoundingBox =
    BoundingBox(
        coordinates.copyOf().also {
            it[0] = west
            it[size / 2] = east
        },
    )
