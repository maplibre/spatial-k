package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.MultiGeometry

internal object MultiGeometrySerializer :
    GeoJsonPolymorphicSerializer<MultiGeometry>(
        baseClass = MultiGeometry::class,
        allowedTypes = setOf("MultiPoint", "MultiLineString", "MultiPolygon"),
    )
