package org.maplibre.spatialk.gpx

import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import org.intellij.lang.annotations.Language

/** A utility object for serializing and deserializing GPX (GPS Exchange Format) data. */
public data object Gpx {
    /** The [XML] format configuration for parsing and serializing GPX data. */
    @OptIn(ExperimentalXmlUtilApi::class)
    public val gpxFormat: XML = XML {
        indentString = "    "
        policy = DefaultXmlSerializationPolicy.Builder().apply { ignoreUnknownChildren() }.build()
    }

    /**
     * Decodes a [Document] from the given XML string.
     *
     * @param string The XML string to decode.
     * @return The deserialized [Document] object.
     * @throws IllegalArgumentException if the given string is not a valid GPX document.
     */
    public fun decodeFromString(@Language("xml") string: String): Document =
        gpxFormat.decodeFromString(Document.serializer(), string)

    /**
     * Decodes a [Document] from the given XML string. If the string is not a well-formed GPX
     * document, null is returned.
     *
     * @param string The XML string to decode.
     * @return A [Document] object if decoding is successful, or `null` if the string is invalid.
     */
    public fun decodeFromStringOrNull(@Language("xml") string: String): Document? =
        try {
            decodeFromString(string)
        } catch (_: IllegalArgumentException) {
            null
        }

    /**
     * Serializes a [Document] object into its GPX XML string representation.
     *
     * @param value The [Document] object to be encoded.
     * @return A [String] containing the GPX XML data.
     */
    public fun encodeToString(value: Document): String =
        gpxFormat.encodeToString(Document.serializer(), value)
}
