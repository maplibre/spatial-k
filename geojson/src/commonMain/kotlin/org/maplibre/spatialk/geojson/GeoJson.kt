package org.maplibre.spatialk.geojson

/**
 * A GeoJSON object represents a [Geometry], [Feature], or
 * [collection of Features][FeatureCollection].
 *
 * @property bbox An optional bounding box used to represent the limits of the object's geometry.
 */
public sealed interface GeoJson {
    public val bbox: BoundingBox?

    /** @return A JSON representation of this object. */
    public fun json(): String
}
