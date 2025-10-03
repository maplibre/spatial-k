package org.maplibre.spatialk.gpx

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

/**
 * Represents a route - an ordered list of waypoints representing a series of turn points leading to
 * a destination.
 *
 * See https://www.topografix.com/GPX/1/1/#type_rteType
 *
 * @property name The GPS name of the route.
 * @property cmt A comment, and/or additional information about the route.
 * @property desc A text description of the route for user display.
 * @property src The source of the data. Recommended to be a URL which provides additional
 *   information about the route.
 * @property link A link to external information about the route.
 * @property number A GPS route number.
 * @property type The type of route. This is for categorizing the route and can be user-defined
 *   (e.g., "resupply", "scenic").
 * @property rtept A list of route points ([Waypoint]) which are the turning points, intersections,
 *   or other critical points in the route.
 */
@Serializable
public data class Route(
    @XmlElement(true) val name: String?,
    @XmlElement(true) val cmt: String?,
    @XmlElement(true) val desc: String?,
    @XmlElement(true) val src: String?,
    @XmlElement(true) val link: String?,
    @XmlElement(true) val number: Int?,
    @XmlElement(true) val type: String?,
    @XmlElement(true) val rtept: List<Waypoint>,
)
