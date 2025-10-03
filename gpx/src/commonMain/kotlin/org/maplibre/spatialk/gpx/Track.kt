package org.maplibre.spatialk.gpx

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

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
    @XmlElement(true) val name: String?,
    @XmlElement(true) val cmt: String?,
    @XmlElement(true) val desc: String?,
    @XmlElement(true) val src: String?,
    @XmlElement(true) val link: String?,
    @XmlElement(true) val number: Int?,
    @XmlElement(true) val type: String?,
    @SerialName("trkseg") @XmlElement(true) val trkseg: List<TrackSegment>,
)

/**
 * A Track Segment holds a list of Track Points which are logically connected in order. To represent
 * a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new
 * Track Segment for each continuous span of track data.
 *
 * See https://www.topografix.com/GPX/1/1/#type_trksegType
 *
 * @property trkpt A list of track points.
 */
@Serializable public data class TrackSegment(@XmlElement(true) val trkpt: List<Waypoint>)
