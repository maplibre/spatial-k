package org.maplibre.spatialk.gpx

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * Represents the root element of a GPX file.
 *
 * GPX is an XML schema designed as a common GPS data format for software applications. It can be
 * used to describe waypoints, tracks, and routes.
 *
 * See <a href="https://www.topografix.com/GPX/1/1/#element_gpx">gpx</a>.
 *
 * @property metadata Metadata about the file.
 * @property trk A list of tracks.
 * @property rte A list of routes.
 * @property wpt A list of waypoints.
 */
@XmlSerialName("gpx", "http://www.topografix.com/GPX/1/1")
@Serializable
public data class Document(
    @Required
    @XmlSerialName("schemaLocation", "http://www.w3.org/2001/XMLSchema-instance", "xsi")
    val schemaLocation: String =
        "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd",
    @Required val version: String = "1.1",
    @Required val creator: String = "org.maplibre.spatialk.gpx",
    @XmlSerialName("metadata") @XmlElement val metadata: Metadata? = null,
    @XmlSerialName("trk") @XmlElement val trk: List<Track> = listOf(),
    @XmlSerialName("rte") @XmlElement val rte: List<Route> = listOf(),
    @XmlSerialName("wpt") @XmlElement val wpt: List<Waypoint> = listOf(),
)

/**
 * Represents metadata about the GPX file. This information is about the file itself, not the data
 * within it.
 *
 * See <a href="https://www.topografix.com/GPX/1/1/#type_metadataType">metadataType</a>.
 *
 * @property name The name of the GPX file.
 * @property desc A description of the contents of the GPX file.
 * @property author The person or organization who created the GPX file.
 * @property copyright Copyright and license information governing use of the file.
 * @property link URLs associated with the location described in the file.
 * @property time The creation timestamp of the data in the file.
 * @property keywords Keywords associated with the file. Search engines or databases may use them.
 * @property bounds The minimum and maximum coordinates that describe the extent of the data in the
 *   file.
 */
@Serializable
public data class Metadata
@OptIn(ExperimentalTime::class)
constructor(
    @XmlSerialName("name") @XmlElement val name: String? = null,
    @XmlElement val desc: String? = null,
    @XmlSerialName("author") @XmlElement val author: Author? = null,
    @XmlSerialName("copyright") @XmlElement val copyright: Copyright? = null,
    @XmlSerialName("link") @XmlElement val link: List<Link> = listOf(),
    @XmlElement val time: Instant? = null,
    @XmlElement val keywords: String? = null,
    @XmlElement val bounds: Bounds? = null,
    // val extensions: Extensions?,
)

/**
 * Represents information about the author of the GPX file.
 *
 * See <a href="https://www.topografix.com/GPX/1/1/#type_personType">personType</a>.
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
 * See <a href="https://www.topografix.com/GPX/1/1/#type_copyrightType">copyrightType</a>.
 *
 * @property year The copyright year.
 * @property license A URL to the license governing the use of the file.
 */
@Serializable
public data class Copyright(
    @XmlElement val year: String? = null,
    @XmlElement val license: String? = null,
)

/**
 * Represents an email address, broken into two parts.
 *
 * See <a href="https://www.topografix.com/GPX/1/1/#type_emailType">emailType</a>.
 *
 * @property id The ID part of the email address (e.g., "me" in "me@example.com").
 * @property domain The domain part of the email address (e.g., "example.com" in "me@example.com").
 */
@Serializable public data class Email(val id: String, val domain: String)

/**
 * Represents a link to an external resource. This allows a GPX file to be associated with a web
 * page, photo, or any other information on the web.
 *
 * See <a href="https://www.topografix.com/GPX/1/1/#type_linkType">linkType</a>.
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
 * See <a href="https://www.topografix.com/GPX/1/1/#type_boundsType">boundsType</a>.
 *
 * @property minlat The minimum latitude.
 * @property minlon The minimum longitude.
 * @property maxlat The maximum latitude.
 * @property maxlon The maximum longitude.
 */
@Serializable
public data class Bounds(
    val minlat: Double,
    val minlon: Double,
    val maxlat: Double,
    val maxlon: Double,
)
