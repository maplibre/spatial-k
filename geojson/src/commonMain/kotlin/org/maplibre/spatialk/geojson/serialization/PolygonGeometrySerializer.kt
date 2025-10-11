package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.PolygonGeometry

internal object PolygonGeometrySerializer :
    GeoJsonPolymorphicSerializer<PolygonGeometry>(
        baseClass = PolygonGeometry::class,
        allowedTypes = setOf("Polygon", "MultiPolygon"),
    )
