package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.LineStringGeometry

internal object LineStringGeometrySerializer :
    GeoJsonPolymorphicSerializer<LineStringGeometry>(
        baseClass = LineStringGeometry::class,
        allowedTypes = setOf("LineString", "MultiLineString"),
    )
