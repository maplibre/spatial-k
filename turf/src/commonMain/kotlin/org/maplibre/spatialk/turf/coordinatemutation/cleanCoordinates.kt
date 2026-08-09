@file:JvmName("CoordinateMutation")
@file:JvmMultifileClass

package org.maplibre.spatialk.turf.coordinatemutation

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.MultiPoint
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.booleans.pointOnLine

/**
 * Removes redundant coordinates from any GeoJSON [Geometry].
 *
 * @return the cleaned input geometry
 */
@Suppress("MagicNumber", "UNCHECKED_CAST")
public fun <T : Geometry> T.cleanCoordinates(): T =
    when (this) {
        is LineString -> LineString(cleanLine(coordinates))
        is MultiLineString -> coordinates.map { cleanLine(it) }.let { MultiLineString(it) }
        is Polygon ->
            coordinates
                .map { cleanLine(it) }
                .also { cleanedCoordinates ->
                    require(cleanedCoordinates.first().size >= 4) {
                        "Invalid Polygon: A Polygon must have at least 4 positions."
                    }
                }
                .let { Polygon(it) }

        is MultiPolygon ->
            coordinates
                .map { polygon ->
                    polygon.map { ring -> cleanLine(ring) }
                }
                .onEach { polygon ->
                    require(polygon.first().size >= 4) {
                        "Invalid MultiPolygon: Each Polygon must have at least 4 positions."
                    }
                }
                .let { MultiPolygon(it) }

        is Point -> this
        is MultiPoint -> {
            // Position equals/hashCode include altitude, so positions that differ only in z are
            // preserved while exact duplicates are dropped. LinkedHashSet keeps input order.
            coordinates.toCollection(LinkedHashSet()).let { MultiPoint(it.toList()) }
        }

        else -> error("${this::class.simpleName} geometry not supported")
    }
        as T

@Suppress("MagicNumber")
private fun cleanLine(line: List<Position>): List<Position> {
    // handle "clean" segment
    if (line.size == 2 && line[0] != line[1]) return line

    val newPoints =
        line.fold(mutableListOf<Position>()) { acc, position ->
            acc.apply {
                add(position)
                if (
                    size > 2 &&
                        pointOnLine(
                            this[size - 2],
                            LineString(this[size - 3], this[size - 1]),
                        )
                ) {
                    removeAt(size - 2)
                }
            }
        }

    return if (
        newPoints.size > 2 &&
            pointOnLine(
                newPoints[newPoints.size - 2],
                LineString(newPoints[newPoints.size - 3], newPoints.last()),
            )
    ) {
        newPoints.dropLast(1)
    } else {
        newPoints
    }
}
