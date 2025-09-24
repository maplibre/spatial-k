@file:JvmName("-GeometryDslKt")

package org.maplibre.spatialk.geojson.dsl

import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

@GeoJsonDsl
public abstract class GeometryDsl<T : Geometry>
protected constructor(public var bbox: BoundingBox? = null) {
    public abstract fun create(): T
}

@GeoJsonDsl
public class PointDsl(private var coordinates: Position) : GeometryDsl<Point>() {
    override fun create(): Point = Point(coordinates, bbox)
}

@GeoJsonDsl
public inline fun point(
    longitude: Double,
    latitude: Double,
    altitude: Double? = null,
    block: PointDsl.() -> Unit = {},
): Point = PointDsl(Position(longitude, latitude, altitude)).apply(block).create()

@GeoJsonDsl
public class MultiPointDsl(private val points: MutableList<Position> = mutableListOf()) :
    GeometryDsl<MultiPoint>() {
    override fun create(): MultiPoint = MultiPoint(points, bbox)

    public operator fun Position.unaryPlus() {
        points.add(this)
    }

    public operator fun Point.unaryPlus() {
        points.add(this.coordinates)
    }

    public fun point(longitude: Double, latitude: Double, altitude: Double? = null) {
        points.add(Position(longitude, latitude, altitude))
    }
}

@GeoJsonDsl
public inline fun multiPoint(block: MultiPointDsl.() -> Unit): MultiPoint =
    MultiPointDsl().apply(block).create()

@GeoJsonDsl
public class LineStringDsl(internal val points: MutableList<Position> = mutableListOf()) :
    GeometryDsl<LineString>() {
    override fun create(): LineString = LineString(points, bbox)

    public operator fun Position.unaryPlus() {
        points.add(this)
    }

    public operator fun Point.unaryPlus() {
        points.add(this.coordinates)
    }

    public fun point(longitude: Double, latitude: Double, altitude: Double? = null) {
        points.add(Position(longitude, latitude, altitude))
    }
}

@GeoJsonDsl
public inline fun lineString(block: LineStringDsl.() -> Unit): LineString =
    LineStringDsl().apply(block).create()

@GeoJsonDsl
public class MultiLineStringDsl(
    private val coordinates: MutableList<List<Position>> = mutableListOf()
) : GeometryDsl<MultiLineString>() {
    override fun create(): MultiLineString = MultiLineString(coordinates)

    public inline fun lineString(block: LineStringDsl.() -> Unit) {
        +LineStringDsl().apply(block).create()
    }

    public operator fun LineString.unaryPlus() {
        this@MultiLineStringDsl.coordinates.add(this.coordinates)
    }
}

@GeoJsonDsl
public inline fun multiLineString(block: MultiLineStringDsl.() -> Unit): MultiLineString =
    MultiLineStringDsl().apply(block).create()

@GeoJsonDsl
public class PolygonDsl(internal val coordinates: MutableList<List<Position>> = mutableListOf()) :
    GeometryDsl<Polygon>() {
    override fun create(): Polygon = Polygon(coordinates, bbox)

    public inner class RingDsl(internal val points: MutableList<Position> = mutableListOf()) {
        public operator fun Position.unaryPlus() {
            points.add(this)
        }

        public operator fun Point.unaryPlus() {
            points.add(this.coordinates)
        }

        public inline fun lineString(block: LineStringDsl.() -> Unit) {
            +LineStringDsl().apply(block).create()
        }

        public fun point(longitude: Double, latitude: Double, altitude: Double? = null) {
            points.add(Position(longitude, latitude, altitude))
        }

        public operator fun LineString.unaryPlus() {
            this@RingDsl.points.addAll(this.coordinates)
        }

        public fun complete() {
            points.add(points.first())
        }
    }

    public fun ring(block: RingDsl.() -> Unit) {
        coordinates.add(RingDsl().apply(block).points)
    }
}

@GeoJsonDsl
public inline fun polygon(block: PolygonDsl.() -> Unit): Polygon =
    PolygonDsl().apply(block).create()

@GeoJsonDsl
public class MultiPolygonDsl(
    private val coordinates: MutableList<List<List<Position>>> = mutableListOf()
) : GeometryDsl<MultiPolygon>() {
    override fun create(): MultiPolygon = MultiPolygon(coordinates, bbox)

    public inline fun polygon(block: PolygonDsl.() -> Unit) {
        +PolygonDsl().apply(block).create()
    }

    public operator fun Polygon.unaryPlus() {
        this@MultiPolygonDsl.coordinates.add(this.coordinates)
    }
}

@GeoJsonDsl
public inline fun multiPolygon(block: MultiPolygonDsl.() -> Unit): MultiPolygon =
    MultiPolygonDsl().apply(block).create()

@GeoJsonDsl
public class GeometryCollectionDsl(
    private val geometries: MutableList<Geometry> = mutableListOf()
) : GeometryDsl<GeometryCollection>() {
    override fun create(): GeometryCollection = GeometryCollection(geometries)

    public operator fun Geometry.unaryPlus() {
        geometries.add(this)
    }
}

@GeoJsonDsl
public inline fun geometryCollection(block: GeometryCollectionDsl.() -> Unit): GeometryCollection =
    GeometryCollectionDsl().apply(block).create()
