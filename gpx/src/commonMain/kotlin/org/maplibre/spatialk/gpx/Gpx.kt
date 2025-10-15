package org.maplibre.spatialk.gpx

import nl.adaptivity.xmlutil.serialization.XML
import org.intellij.lang.annotations.Language

public data object Gpx {
    public val gpxFormat: XML = XML { indentString = "    " }

    public fun decodeFromString(@Language("xml") string: String): Document =
        gpxFormat.decodeFromString(Document.serializer(), string)

    public fun decodeFromStringOrNull(@Language("xml") string: String): Document? =
        try {
            decodeFromString(string)
        } catch (_: IllegalArgumentException) {
            null
        }

    public fun encodeToString(value: Document): String =
        gpxFormat.encodeToString(Document.serializer(), value)
}
