package org.maplibre.spatialk.gpx

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
 * See https://www.topografix.com/GPX/1/1/#element_gpx
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
    @XmlSerialName("metadata") @XmlElement(true) val metadata: Metadata? = null,
    @XmlElement(true) val trk: List<Track> = listOf(),
    @XmlElement(true) val rte: List<Route> = listOf(),
    @XmlElement(true) val wpt: List<Waypoint> = listOf(),
)

/**
 * Represents metadata about the GPX file. This information is about the file itself, not the data
 * within it.
 *
 * See https://www.topografix.com/GPX/1/1/#type_metadataType
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
public data class Metadata(
    @XmlSerialName("name") @XmlElement(true) val name: String? = null,
    @XmlElement(true) val desc: String? = null,
    @XmlSerialName("author") @XmlElement(true) val author: Author? = null,
    @XmlSerialName("copyright") @XmlElement(true) val copyright: Copyright? = null,
    @XmlElement(true) val link: List<Link> = listOf(),
    @XmlElement(true) val time: String? = null,
    @XmlElement(true) val keywords: String? = null,
    @XmlElement(true) val bounds: Bounds? = null,
    // val extensions: Extensions?,
)

/**
 * Represents information about the author of the GPX file.
 *
 * See https://www.topografix.com/GPX/1/1/#type_personType
 *
 * @property name Name of the person or organization.
 * @property email Email address of the author.
 * @property link Link to a website or other information about the author.
 */
@Serializable
public data class Author(
    @XmlElement(true) val name: String? = null,
    @XmlElement(true) val email: Email? = null,
    @XmlElement(true) val link: Link? = null,
)

/**
 * Represents copyright and license information governing the use of the GPX file.
 *
 * See https://www.topografix.com/GPX/1/1/#type_copyrightType
 *
 * @property year The copyright year.
 * @property license A URL to the license governing the use of the file.
 */
@Serializable
public data class Copyright(
    @XmlElement(true) val year: String? = null,
    @XmlElement(true) val license: String? = null,
)

@Serializable public data class Email(val id: String, val domain: String)

@Serializable
public data class Link(
    val href: String,
    @XmlElement(true) val text: String? = null,
    @XmlElement(true) val type: String? = null,
)

@Serializable
public data class Bounds(
    val minlat: Double,
    val minlon: Double,
    val maxlat: Double,
    val maxlon: Double,
)
