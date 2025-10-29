package org.maplibre.spatialk.gpx

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.dom2.Element
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.maplibre.spatialk.gpx.serializers.UtcDefaultInstantSerializer

/**
 * Represents the root element of a GPX file.
 *
 * GPX is an XML schema designed as a common GPS data format for software applications. It can be
 * used to describe waypoints, tracks, and routes.
 *
 * See [gpx](https://www.topografix.com/GPX/1/1/#element_gpx).
 *
 * @property schemaLocation The location of the GPX schema. Should be
 *   `http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd`.
 * @property version The GPX version. Should be `1.1`.
 * @property creator The name or URL of the software that created the GPX file.
 * @property metadata Metadata about the file.
 * @property waypoints A list of waypoints.
 * @property routes A list of routes.
 * @property tracks A list of tracks.
 * @property extensions Extension schema elements.
 */
@XmlSerialName("gpx", "http://www.topografix.com/GPX/1/1")
@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class Document(
    @EncodeDefault
    @XmlSerialName("schemaLocation", "http://www.w3.org/2001/XMLSchema-instance", "xsi")
    val schemaLocation: String =
        "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd",
    @Required val version: String = "1.1",
    @EncodeDefault val creator: String = "org.maplibre.spatialk.gpx",
    @XmlSerialName("metadata") @XmlElement val metadata: Metadata? = null,
    @SerialName("tracks") @XmlSerialName("trk") @XmlElement val tracks: List<Track> = listOf(),
    @SerialName("routes") @XmlSerialName("rte") @XmlElement val routes: List<Route> = listOf(),
    @SerialName("waypoints")
    @XmlSerialName("wpt")
    @XmlElement
    val waypoints: List<Waypoint> = listOf(),
    @XmlSerialName("extensions") @XmlElement val extensions: Element? = null,
)

/**
 * Represents metadata about the GPX file. This information is about the file itself, not the data
 * within it.
 *
 * See [metadataType](https://www.topografix.com/GPX/1/1/#type_metadataType).
 *
 * @property name The name of the GPX file.
 * @property description A description of the contents of the GPX file.
 * @property author The person or organization who created the GPX file.
 * @property copyright Copyright and license information governing use of the file.
 * @property link URLs associated with the location described in the file.
 * @property timestamp The creation timestamp of the data in the file.
 * @property keywords Keywords associated with the file. Search engines or databases may use them.
 * @property bounds The minimum and maximum coordinates that describe the extent of the data in the
 *   file.
 * @property extensions Extension schema elements.
 */
@Serializable
public data class Metadata
@OptIn(ExperimentalTime::class)
constructor(
    @SerialName("name") @XmlElement val name: String? = null,
    @SerialName("desc") @XmlElement val description: String? = null,
    @SerialName("author") @XmlSerialName("author") @XmlElement val author: Author? = null,
    @XmlSerialName("copyright") @XmlElement val copyright: Copyright? = null,
    @XmlSerialName("link") @XmlElement val link: List<Link> = listOf(),
    @Serializable(with = UtcDefaultInstantSerializer::class)
    @SerialName("time")
    @XmlElement
    val timestamp: Instant? = null,
    @XmlElement val keywords: String? = null,
    @XmlSerialName("bounds") @XmlElement val bounds: Bounds? = null,
    @XmlSerialName("extensions") @XmlElement val extensions: Element? = null,
)

/**
 * Represents information about the author of the GPX file.
 *
 * See [personType](https://www.topografix.com/GPX/1/1/#type_personType).
 *
 * @property name Name of the person or organization.
 * @property email Email address of the author.
 * @property link Link to a website or other information about the author.
 */
@Serializable
public data class Author(
    @XmlElement val name: String? = null,
    @XmlElement val email: Email? = null,
    @XmlElement val link: Link? = null,
)

/**
 * Represents copyright and license information governing the use of the GPX file.
 *
 * See [copyrightType](https://www.topografix.com/GPX/1/1/#type_copyrightType).
 *
 * @property author The copyright holder.
 * @property year The copyright year.
 * @property license A URL to the license governing the use of the file.
 */
@Serializable
public data class Copyright(
    val author: String,
    @XmlElement val year: String? = null,
    @XmlElement val license: String? = null,
)

/**
 * Represents an email address, broken into two parts.
 *
 * See [emailType](https://www.topografix.com/GPX/1/1/#type_emailType).
 *
 * @property id The ID part of the email address (e.g., "me" in "me@example.com").
 * @property domain The domain part of the email address (e.g., "example.com" in "me@example.com").
 */
@Serializable public data class Email(val id: String, val domain: String)

/**
 * Represents a link to an external resource. This allows a GPX file to be associated with a web
 * page, photo, or any other information on the web.
 *
 * See [linkType](https://www.topografix.com/GPX/1/1/#type_linkType).
 *
 * @property href The URL of the link.
 * @property text The text of the link.
 * @property type The MIME type of the content.
 */
@Serializable
public data class Link(
    val href: String,
    @XmlElement val text: String? = null,
    @XmlElement val type: String? = null,
)

/**
 * Represents the minimum and maximum coordinates that describe the extent of the data in a GPX
 * file.
 *
 * See [boundsType](https://www.topografix.com/GPX/1/1/#type_boundsType).
 *
 * @property minLatitude The minimum latitude.
 * @property minLongitude The minimum longitude.
 * @property maxLatitude The maximum latitude.
 * @property maxLongitude The maximum longitude.
 */
@Serializable
@SerialName("bounds")
public data class Bounds(
    @SerialName("minlat") val minLatitude: Double,
    @SerialName("minlon") val minLongitude: Double,
    @SerialName("maxlat") val maxLatitude: Double,
    @SerialName("maxlon") val maxLongitude: Double,
)
