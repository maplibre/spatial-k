package org.maplibre.spatialk.gpx

import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.UnknownChildHandler
import nl.adaptivity.xmlutil.serialization.XML
import org.intellij.lang.annotations.Language

public data object Gpx {
    @OptIn(ExperimentalXmlUtilApi::class)
    public val gpxFormat: XML = XML {
        indentString = "    "
        policy =
            DefaultXmlSerializationPolicy.Builder()
                .apply {
                    unknownChildHandler =
                        UnknownChildHandler { input, inputKind, descriptor, name, candidates ->
                            listOf()
                        }
                }
                .build()
    }

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
