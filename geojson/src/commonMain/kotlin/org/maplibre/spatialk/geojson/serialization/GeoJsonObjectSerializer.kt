package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.GeoJsonObject

internal object GeoJsonObjectSerializer :
    GeoJsonPolymorphicSerializer<GeoJsonObject>(
        GeoJsonObject::class,
        setOf(
            "Point",
            "MultiPoint",
            "LineString",
            "MultiLineString",
            "Polygon",
            "MultiPolygon",
            "GeometryCollection",
            "Feature",
            "FeatureCollection",
        ),
    )
