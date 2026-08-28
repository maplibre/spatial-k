package org.maplibre.spatialk.geojson.dsl

import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.EmptyForeignMembers
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Position

/**
 * Builder for constructing [MultiLineString] objects using a DSL.
 *
 * @property bbox An optional [BoundingBox] for this [MultiLineString].
 * @property foreignMembers Members not defined by RFC 7946.
 * @see MultiLineString
 * @see buildMultiLineString
 * @see addLineString
 */
@GeoJsonDsl
public class MultiLineStringBuilder {
    public var bbox: BoundingBox? = null
    public var foreignMembers: JsonObject = EmptyForeignMembers
    private val coordinates: MutableList<List<Position>> = mutableListOf()

    /**
     * Adds a [LineString] to this [MultiLineString].
     *
     * @param lineString The [LineString] to add.
     */
    public fun add(lineString: LineString) {
        coordinates.add(lineString.coordinates)
    }

    /**
     * Builds the [MultiLineString] from the configured values.
     *
     * @return The constructed [MultiLineString].
     */
    public fun build(): MultiLineString = MultiLineString(coordinates, bbox, foreignMembers)
}
