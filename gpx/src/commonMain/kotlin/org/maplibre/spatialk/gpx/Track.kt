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
 * Represents a GPX track (`trk` element), an ordered list of points describing a path.
 *
 * A track is made up of one or more track segments.
 *
 * See [trkType](https://www.topografix.com/GPX/1/1/#type_trkType).
 *
 * @property name The GPS name of the track.
 * @property comment A comment or description for the track.
 * @property description A user-supplied description of the track.
 * @property source The source of the data.
 * @property link A URL link associated with the track.
 * @property number A GPS track number.
 * @property type The type of activity for the track (e.g., "cycling", "running").
 * @property segments A list of track segments that make up the track.
 * @property extensions Extension schema elements.
 */
@Serializable
public data class Track(
    @SerialName("name") @XmlElement val name: String? = null,
    @SerialName("cmt") @XmlElement val comment: String? = null,
    @SerialName("desc") @XmlElement val description: String? = null,
    @SerialName("src") @XmlElement val source: String? = null,
    @SerialName("link") @XmlElement val link: Link? = null,
    @SerialName("number") @XmlElement val number: Int? = null,
    @SerialName("type") @XmlElement val type: String? = null,
    @SerialName("trkseg")
    @XmlSerialName("trkseg")
    @XmlElement
    val segments: List<TrackSegment> = listOf(),
    @XmlSerialName("extensions") @XmlElement val extensions: Element? = null,
)

/**
 * Converts this [Track] object into a GeoJSON [Feature] object.
 *
 * The geometry of the feature will be a [GeometryCollection] of [Point]s, representing all the
 * track points from all segments of the track. The original [Track] object is stored in the
 * feature's `properties` field.
 *
 * @return A GeoJSON [Feature] with a [GeometryCollection] geometry and this [Track] as its
 *   properties.
 */
public fun Track.toGeoJson(): Feature<GeometryCollection<Point>, Track> {
    return Feature(
        GeometryCollection(
            segments.flatMap { segment ->
                segment.points.map { point -> point.toGeoJson().geometry }
            }
        ),
        this,
    )
}

/**
 * A Track Segment holds a list of Track Points which are logically connected in order. To represent
 * a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new
 * Track Segment for each continuous span of track data.
 *
 * See [trksegType](https://www.topografix.com/GPX/1/1/#type_trksegType).
 *
 * @property points A list of track points.
 * @property extensions Extension schema elements.
 */
@Serializable
public data class TrackSegment(
    @XmlSerialName("trkpt") @XmlElement val points: List<Waypoint>,
    @XmlSerialName("extensions") @XmlElement val extensions: Element? = null,
)

/**
 * Converts this [TrackSegment] into a GeoJSON [Feature].
 *
 * The feature's geometry is a [GeometryCollection] containing all the [Point] geometries from the
 * track points (`trkpt`) within this segment. The feature's properties will be this [TrackSegment]
 * object itself.
 *
 * @return A GeoJSON [Feature] representing this track segment.
 */
public fun TrackSegment.toGeoJson(): Feature<GeometryCollection<Point>, TrackSegment> {
    return Feature(GeometryCollection(points.map { it.toGeoJson().geometry }), this)
}
