package org.maplibre.spatialk.geojson

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import org.maplibre.spatialk.geojson.serialization.GeoJson

/**
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.3">
 *   https://tools.ietf.org/html/rfc7946#section-3.1.3</a>
 * @see Point
 */
@Serializable
@SerialName("MultiPoint")
public data class MultiPoint
@JvmOverloads
constructor(
    /** a list of [Position]s. */
    public val coordinates: List<Position>,
    /** a bounding box */
    override val bbox: BoundingBox? = null,
) : Geometry() {

    /** Create a MultiPoint by a number of [Position]s. */
    @JvmOverloads
    public constructor(
        vararg coordinates: Position,
        bbox: BoundingBox? = null,
    ) : this(coordinates.toList(), bbox)

    /**
     * Create a MultiPoint by an array of [DoubleArray]s that each represent a position.
     *
     * @throws IllegalArgumentException if any array of doubles does not represent a valid position
     */
    @JvmOverloads
    public constructor(
        coordinates: Array<DoubleArray>,
        bbox: BoundingBox? = null,
    ) : this(coordinates.map(::Position), bbox)

    override fun json(): String = GeoJson.encodeToString(this)

    public companion object {
        @JvmStatic
        public fun fromJson(@Language("json") json: String): MultiPoint = fromJson<MultiPoint>(json)

        @JvmStatic
        public fun fromJsonOrNull(@Language("json") json: String): MultiPoint? =
            try {
                fromJson(json)
            } catch (_: Exception) {
                null
            }
    }
}
