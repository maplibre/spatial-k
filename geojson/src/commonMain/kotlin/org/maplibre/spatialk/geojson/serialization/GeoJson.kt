package org.maplibre.spatialk.geojson.serialization

import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json

public val GeoJson: Json = Json {
    classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
    ignoreUnknownKeys = true
}
