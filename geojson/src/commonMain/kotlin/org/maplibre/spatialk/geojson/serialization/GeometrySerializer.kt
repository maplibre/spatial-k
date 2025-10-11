package org.maplibre.spatialk.geojson.serialization

import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineStringGeometry
import org.maplibre.spatialk.geojson.MultiGeometry
import org.maplibre.spatialk.geojson.PointGeometry
import org.maplibre.spatialk.geojson.PolygonGeometry
import org.maplibre.spatialk.geojson.SingleGeometry

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

internal object SingleGeometrySerializer :
    GeoJsonPolymorphicSerializer<SingleGeometry>(
        baseClass = SingleGeometry::class,
        allowedTypes = setOf("Point", "LineString", "Polygon"),
    )

internal object MultiGeometrySerializer :
    GeoJsonPolymorphicSerializer<MultiGeometry>(
        baseClass = MultiGeometry::class,
        allowedTypes = setOf("MultiPoint", "MultiLineString", "MultiPolygon"),
    )

internal object PointGeometrySerializer :
    GeoJsonPolymorphicSerializer<PointGeometry>(
        baseClass = PointGeometry::class,
        allowedTypes = setOf("Point", "MultiPoint"),
    )

internal object LineStringGeometrySerializer :
    GeoJsonPolymorphicSerializer<LineStringGeometry>(
        baseClass = LineStringGeometry::class,
        allowedTypes = setOf("LineString", "MultiLineString"),
    )

internal object PolygonGeometrySerializer :
    GeoJsonPolymorphicSerializer<PolygonGeometry>(
        baseClass = PolygonGeometry::class,
        allowedTypes = setOf("Polygon", "MultiPolygon"),
    )
