package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json

public val GeoJson: Json = Json {
    @OptIn(ExperimentalSerializationApi::class)
    classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
    ignoreUnknownKeys = true
}
