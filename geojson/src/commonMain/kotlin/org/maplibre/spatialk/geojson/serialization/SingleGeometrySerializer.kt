package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.SingleGeometry

internal object SingleGeometrySerializer :
    GeoJsonPolymorphicSerializer<SingleGeometry>(
        baseClass = SingleGeometry::class,
        allowedTypes = setOf("Point", "LineString", "Polygon"),
    )
