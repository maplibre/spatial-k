package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.PointGeometry

internal object PointGeometrySerializer :
    GeoJsonPolymorphicSerializer<PointGeometry>(
        baseClass = PointGeometry::class,
        allowedTypes = setOf("Point", "MultiPoint"),
    )
