package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.Geometry

internal object GeometrySerializer :
    GeoJsonPolymorphicSerializer<Geometry>(
        baseClass = Geometry::class,
        allowedTypes =
            setOf(
                "Point",
                "MultiPoint",
                "LineString",
                "MultiLineString",
                "Polygon",
                "MultiPolygon",
                "GeometryCollection",
            ),
    )
