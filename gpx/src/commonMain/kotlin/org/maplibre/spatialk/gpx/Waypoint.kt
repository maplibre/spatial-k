package org.maplibre.spatialk.gpx

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.adaptivity.xmlutil.serialization.XmlElement
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

/**
 * Represents a waypoint, point of interest, or named feature on a map. This corresponds to the
 * `wptType` in the GPX 1.1 schema.
 *
 * A waypoint is a single point on the earth, defined by its latitude and longitude. It can
 * optionally include other information such as elevation, time, and descriptive details.
 *
 * @property lat The latitude of the point. Decimal degrees, WGS84 datum. (Required)
 * @property lon The longitude of the point. Decimal degrees, WGS84 datum. (Required)
 * @property ele Elevation of the point in meters.
 * @property time The time of the waypoint creation.
 * @property magvar Magnetic variation (in degrees) at the point.
 * @property geoidheight Height of geoid (mean sea level) above WGS84 earth ellipsoid, in meters.
 * @property name The GPS name of the waypoint.
 * @property cmt GPS waypoint comment.
 * @property desc A text description of the element.
 * @property src Source of data. Included to give user some idea of reliability and accuracy of
 *   data.
 * @property link Link to additional information about the waypoint.
 * @property sym Text of GPS symbol name.
 * @property type Type of waypoint.
 * @property fix Type of GPS fix. 'none' means no fix. '2d' and '3d' are just estimates of quality.
 * @property sat Number of satellites used to calculate the GPX fix.
 * @property hdop Horizontal dilution of precision.
 * @property vdop Vertical dilution of precision.
 * @property pdop Position dilution of precision.
 * @property ageofdgpsdata Number of seconds since last DGPS update.
 * @see <a href="https://www.topografix.com/GPX/1/1/#type_wptType">GPX 1.1 Schema - wptType</a>
 */
@Serializable
@SerialName("wpt")
@OptIn(ExperimentalTime::class)
public data class Waypoint(
    val lat: Double,
    val lon: Double,
    @XmlElement val ele: Double? = null,
    @XmlElement val time: Instant? = null,
    @XmlElement val magvar: Double? = null,
    @XmlElement val geoidheight: Double? = null,
    @XmlElement val name: String? = null,
    @XmlElement val cmt: String? = null,
    @XmlElement val desc: String? = null,
    @XmlElement val src: String? = null,
    @XmlElement val link: String? = null,
    @XmlElement val sym: String? = null,
    @XmlElement val type: String? = null,
    @XmlElement val fix: String? = null,
    @XmlElement val sat: Int? = null,
    @XmlElement val hdop: Double? = null,
    @XmlElement val vdop: Double? = null,
    @XmlElement val pdop: Double? = null,
    @XmlElement val ageofdgpsdata: Double? = null,
    @XmlElement val dgpsid: Double? = null,
    // @XmlElement val extensions = null,
) {

    public fun toFeature(): Feature<Point> {
        return Feature(
            geometry = Point(Position(lon, lat)),
            properties =
                JsonObject(
                    mapOf(
                            "name" to name?.let(::JsonPrimitive),
                            "cmt" to cmt?.let(::JsonPrimitive),
                            "desc" to desc?.let(::JsonPrimitive),
                            // ...
                        )
                        .filterValues { it != null }
                        .mapValues { it.value!! }
                ),
        )
    }
}
