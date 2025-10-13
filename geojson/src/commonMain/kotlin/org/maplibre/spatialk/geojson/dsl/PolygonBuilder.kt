package org.maplibre.spatialk.geojson.dsl

import kotlin.collections.plus
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

/**
 * Builder for constructing [Polygon] objects using a DSL.
 *
 * @property bbox An optional [BoundingBox] for this [Polygon].
 * @see Polygon
 * @see buildPolygon
 * @see addRing
 */
@GeoJsonDsl
public class PolygonBuilder() {
    public var bbox: BoundingBox? = null
    private val coordinates: MutableList<List<Position>> = mutableListOf()

    /**
     * Adds a ring to this [Polygon]. The ring will be automatically closed if not already closed.
     *
     * @param ring The list of [Position]s forming the ring.
     * @throws IllegalArgumentException if the ring is empty.
     */
    public fun add(ring: List<Position>) {
        require(ring.isNotEmpty()) { "Polygon ring cannot be empty" }
        coordinates += if (ring.first() == ring.last()) ring else ring + listOf(ring.first())
    }

    /**
     * Adds a ring to this [Polygon]. The ring will be automatically closed if not already closed.
     *
     * @param ring The [LineString] whose coordinates will be used as a ring.
     */
    public fun add(ring: LineString) {
        add(ring.coordinates)
    }

    /**
     * Builds the [Polygon] from the configured values.
     *
     * @return The constructed [Polygon].
     * @throws IllegalArgumentException if no rings have been added or any ring contains fewer than
     *   4 [Position] objects.
     */
    public fun build(): Polygon = Polygon(coordinates, bbox)
}
