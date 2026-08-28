package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeometryCoordinateReservedKeys
import org.maplibre.spatialk.geojson.serialization.LineStringSerializer
import org.maplibre.spatialk.geojson.serialization.validateForeignMembers

/**
 * A [LineString] geometry represents a curve in coordinate space with two or more [Position]
 * objects.
 *
 * See [RFC 7946 Section 3.1.4](https://tools.ietf.org/html/rfc7946#section-3.1.4) for the full
 * specification.
 *
 * @throws IllegalArgumentException if the coordinates contain fewer than two [Position] objects
 * @see MultiLineString
 */
@Serializable(with = LineStringSerializer::class)
public data class LineString
@JvmOverloads
constructor(
    /** The [Position]s of this [LineString]. */
    public val coordinates: List<Position>,
    /** The [BoundingBox] of this [LineString]. */
    override val bbox: BoundingBox? = null,
    /** Members not defined by RFC 7946. */
    override val foreignMembers: JsonObject = EmptyForeignMembers,
) : SingleGeometry, LineStringGeometry {

    /**
     * Create a [LineString] by a number of [Position] objects.
     *
     * @param coordinates The [Position] objects that make up this [LineString].
     * @param bbox The [BoundingBox] of this [LineString].
     * @param foreignMembers Members not defined by RFC 7946.
     * @throws IllegalArgumentException if fewer than two coordinates have been specified
     */
    @JvmOverloads
    public constructor(
        vararg coordinates: Position,
        bbox: BoundingBox? = null,
        foreignMembers: JsonObject = EmptyForeignMembers,
    ) : this(coordinates.toList(), bbox, foreignMembers)

    /**
     * Create a [LineString] by the [Position] objects of a number of [Point] objects.
     *
     * @param points The [Point] objects whose [Position] objects make up this [LineString].
     * @param bbox The [BoundingBox] of this [LineString].
     * @param foreignMembers Members not defined by RFC 7946.
     * @throws IllegalArgumentException if fewer than two [Point] objects have been specified
     */
    @JvmOverloads
    public constructor(
        vararg points: Point,
        bbox: BoundingBox? = null,
        foreignMembers: JsonObject = EmptyForeignMembers,
    ) : this(points.map { it.coordinates }, bbox, foreignMembers)

    /**
     * Create a [LineString] by an array of [DoubleArray] objects that each represent a [Position].
     *
     * @param coordinates The array of double arrays representing [Position] objects.
     * @param bbox The [BoundingBox] of this [LineString].
     * @param foreignMembers Members not defined by RFC 7946.
     * @throws IllegalArgumentException if the coordinates contain fewer than two [Position]
     *   objects, or if any array of doubles does not represent a valid [Position]
     */
    @JvmOverloads
    public constructor(
        coordinates: Array<DoubleArray>,
        bbox: BoundingBox? = null,
        foreignMembers: JsonObject = EmptyForeignMembers,
    ) : this(coordinates.map(::Position), bbox, foreignMembers)

    init {
        require(coordinates.size >= 2) { "LineString must contain at least two positions" }
        validateForeignMembers(foreignMembers, GeometryCoordinateReservedKeys)
    }

    /** Factory methods for creating and serializing [LineString] objects. */
    public companion object {
        /**
         * Deserialize a [LineString] from a JSON string.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [LineString].
         * @throws SerializationException if the JSON string is invalid or cannot be deserialized.
         * @throws IllegalArgumentException if the geometry does not meet structural requirements.
         */
        @JvmStatic
        public fun fromJson(@Language("json") json: String): LineString =
            GeoJson.decodeFromString(json)

        /**
         * Deserialize a [LineString] from a JSON string, or null if parsing fails.
         *
         * @param json The JSON string to parse.
         * @return The deserialized [LineString], or null if parsing fails.
         */
        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): LineString? =
            GeoJson.decodeFromStringOrNull(json)

        @PublishedApi
        @JvmStatic
        internal fun toJson(lineString: LineString): String = lineString.toJson()
    }
}
