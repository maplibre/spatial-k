package org.maplibre.spatialk.gpx

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.dom2.Element
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.gpx.serializers.UtcDefaultInstantSerializer

/**
 * Represents a waypoint, point of interest, or named feature on a map. This corresponds to the
 * `wptType` in the GPX 1.1 schema.
 *
 * A waypoint is a single point on the earth, defined by its latitude and longitude. It can
 * optionally include other information such as elevation, time, and descriptive details.
 *
 * See [wptType](https://www.topografix.com/GPX/1/1/#type_wptType).
 *
 * @property latitude The latitude of the point. Decimal degrees, WGS84 datum. (Required)
 * @property longitude The longitude of the point. Decimal degrees, WGS84 datum. (Required)
 * @property elevation Elevation of the point in meters.
 * @property timestamp The time of the waypoint creation.
 * @property magneticVariation Magnetic variation (in degrees) at the point.
 * @property geoIdHeight Height of geoid (mean sea level) above WGS84 earth ellipsoid, in meters.
 * @property name The GPS name of the waypoint.
 * @property comment GPS waypoint comment.
 * @property description A text description of the element.
 * @property source Source of data. Included to give user some idea of reliability and accuracy of
 *   data.
 * @property link Link to additional information about the waypoint.
 * @property symbol Text of GPS symbol name.
 * @property type Type of waypoint.
 * @property fix Type of GPS fix. 'none' means no fix. '2d' and '3d' are just estimates of quality.
 * @property satelliteCount Number of satellites used to calculate the GPX fix.
 * @property horizontalDop Horizontal dilution of precision.
 * @property verticalDop Vertical dilution of precision.
 * @property positionDop Position dilution of precision.
 * @property dgpsAge Number of seconds since last DGPS update.
 * @property dgpsId ID of DGPS station used in differential correction.
 * @property extensions Extension schema elements.
 */
@Serializable
@OptIn(ExperimentalTime::class)
public data class Waypoint(
    @SerialName("lat") val latitude: Double,
    @SerialName("lon") val longitude: Double,
    @SerialName("ele") @XmlElement val elevation: Double? = null,
    @Serializable(with = UtcDefaultInstantSerializer::class)
    @SerialName("time")
    @XmlElement
    val timestamp: Instant? = null,
    @SerialName("magvar") @XmlElement val magneticVariation: Double? = null,
    @SerialName("geoidheight") @XmlElement val geoIdHeight: Double? = null,
    @XmlElement val name: String? = null,
    @SerialName("cmt") @XmlElement val comment: String? = null,
    @SerialName("desc") @XmlElement val description: String? = null,
    @SerialName("src") @XmlElement val source: String? = null,
    @XmlSerialName("link") @XmlElement val link: Link? = null,
    @SerialName("sym") @XmlElement val symbol: String? = null,
    @XmlElement val type: String? = null,
    @XmlElement val fix: String? = null,
    @SerialName("sat") @XmlElement val satelliteCount: Int? = null,
    @SerialName("hdop") @XmlElement val horizontalDop: Double? = null,
    @SerialName("vdop") @XmlElement val verticalDop: Double? = null,
    @SerialName("pdop") @XmlElement val positionDop: Double? = null,
    @SerialName("ageofdgpsdata") @XmlElement val dgpsAge: Double? = null,
    @SerialName("dgpsid") @XmlElement val dgpsId: Double? = null,
    @XmlSerialName("extensions") @XmlElement val extensions: Element? = null,
)

/**
 * Converts a [Waypoint] into a GeoJSON [Feature] with a [Point] geometry.
 *
 * The `lon`, `lat`, and optional `ele` of the waypoint are used to create the [Point] geometry. The
 * entire [Waypoint] object itself is set as the properties of the feature.
 *
 * @return A GeoJSON [Feature] representation of the waypoint.
 */
public fun Waypoint.toGeoJson(): Feature<Point, Waypoint> {
    return Feature(geometry = Point(Position(longitude, latitude, elevation)), properties = this)
}

/**
 * Converts a list of [Waypoint] objects into a GeoJSON [FeatureCollection].
 *
 * Each [Waypoint] in the list is transformed into a GeoJSON [Feature] with a [Point] geometry. The
 * original [Waypoint] object is set as the properties of its corresponding feature.
 *
 * @return A [FeatureCollection] containing a list of features, where each feature represents a
 *   waypoint from the input list.
 * @receiver A list of [Waypoint] objects to be converted.
 * @see Waypoint.toGeoJson
 */
public fun List<Waypoint>.toGeoJson(): FeatureCollection<Point, Waypoint> {
    return FeatureCollection(map { it.toGeoJson() })
}
