package org.maplibre.spatialk.gpx

import kotlinx.serialization.Serializable
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
 * See https://www.topografix.com/GPX/1/1/#type_trkType
 *
 * @property name The GPS name of the track.
 * @property cmt A comment or description for the track.
 * @property desc A user-supplied description of the track.
 * @property src The source of the data.
 * @property link A URL link associated with the track.
 * @property number A GPS track number.
 * @property type The type of activity for the track (e.g., "cycling", "running").
 * @property trkseg A list of track segments that make up the track.
 */
@Serializable
public data class Track(
    @XmlElement val name: String? = null,
    @XmlElement val cmt: String? = null,
    @XmlElement val desc: String? = null,
    @XmlElement val src: String? = null,
    @XmlElement val link: Link? = null,
    @XmlElement val number: Int? = null,
    @XmlElement val type: String? = null,
    @XmlSerialName("trkseg") @XmlElement val trkseg: List<TrackSegment> = listOf(),
    // @XmlElement val extensions = null,
)

public fun Track.toGeoJson(): Feature<GeometryCollection<Point>, Track> {
    return Feature(
        GeometryCollection(
            trkseg.flatMap { segment -> segment.trkpt.map { point -> point.toGeoJson().geometry } }
        ),
        this,
    )
}

/**
 * A Track Segment holds a list of Track Points which are logically connected in order. To represent
 * a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new
 * Track Segment for each continuous span of track data.
 *
 * See https://www.topografix.com/GPX/1/1/#type_trksegType
 *
 * @property trkpt A list of track points.
 */
@Serializable
public data class TrackSegment(
    @XmlSerialName("trkpt") @XmlElement val trkpt: List<Waypoint>
    // @XmlElement val extensions = null,
)

public fun TrackSegment.toGeoJson(): Feature<GeometryCollection<Point>, TrackSegment> {
    return Feature(GeometryCollection(trkpt.map { it.toGeoJson().geometry }), this)
}
