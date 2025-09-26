@file:JvmSynthetic

package org.maplibre.spatialk.geojson.dsl

import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.geojson.Position

private val LONGITUDE_RANGE = -180.0..180.0

private val LATITUDE_RANGE = -90.0..90.0

@GeoJsonDsl
public fun lngLat(longitude: Double, latitude: Double): Position {
    require(longitude in LONGITUDE_RANGE && latitude in LATITUDE_RANGE)
    return Position(longitude, latitude)
}

@GeoJsonDsl
public fun lngLat(longitude: Double, latitude: Double, altitude: Double): Position {
    require(longitude in LONGITUDE_RANGE && latitude in LATITUDE_RANGE)
    return Position(longitude, latitude, altitude)
}

@GeoJsonDsl
public fun lngLat(longitude: Double, latitude: Double, altitude: Double?): Position {
    require(longitude in LONGITUDE_RANGE && latitude in LATITUDE_RANGE)
    return Position(longitude, latitude, altitude)
}
