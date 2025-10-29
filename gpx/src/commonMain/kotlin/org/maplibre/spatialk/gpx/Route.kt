package org.maplibre.spatialk.gpx

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.dom2.Element
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.Point

/**
 * Represents a route - an ordered list of waypoints representing a series of turn points leading to
 * a destination.
 *
 * See [rteType](https://www.topografix.com/GPX/1/1/#type_rteType).
 *
 * @property name The GPS name of the route.
 * @property comment A comment, and/or additional information about the route.
 * @property description A text description of the route for user display.
 * @property source The source of the data. Recommended to be a URL which provides additional
 *   information about the route.
 * @property link A link to external information about the route.
 * @property number A GPS route number.
 * @property type The type of route. This is for categorizing the route and can be user-defined
 *   (e.g., "resupply", "scenic").
 * @property points A list of route points ([Waypoint]) which are the turning points, intersections,
 *   or other critical points in the route.
 * @property extensions Extension schema elements.
 */
@Serializable
public data class Route(
    @SerialName("name") @XmlElement val name: String?,
    @SerialName("cmt") @XmlElement val comment: String?,
    @SerialName("desc") @XmlElement val description: String?,
    @SerialName("src") @XmlElement val source: String?,
    @SerialName("link") @XmlElement val link: String?,
    @SerialName("number") @XmlElement val number: Int?,
    @SerialName("type") @XmlElement val type: String?,
    @SerialName("rtept") @XmlSerialName("rtept") @XmlElement val points: List<Waypoint>,
    @XmlSerialName("extensions") @XmlElement val extensions: Element? = null,
)

/**
 * Converts this [Route] to a GeoJSON [Feature] with a [GeometryCollection] of [Point]s.
 *
 * The geometry is a [GeometryCollection] containing all the route points (`routePoints`). The
 * properties of the feature are the properties of this [Route] object itself.
 *
 * @return A GeoJSON [Feature] representing this route.
 */
public fun Route.toGeoJson(): Feature<GeometryCollection<Point>, Route> {
    return Feature(GeometryCollection(points.map { it.toGeoJson().geometry }), this)
}
