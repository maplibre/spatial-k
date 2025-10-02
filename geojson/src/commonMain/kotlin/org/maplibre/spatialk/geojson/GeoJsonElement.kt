package org.maplibre.spatialk.geojson

/**
 * Represents any element defined by the GeoJson specification. Can be a [GeoJsonObject],
 * [BoundingBox], or [Position].
 */
public sealed interface GeoJsonElement {
    /** @return A GeoJSON representation of this object. */
    public fun toJson(): String
}
