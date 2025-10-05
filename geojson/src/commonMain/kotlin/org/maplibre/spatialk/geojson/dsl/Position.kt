@file:JvmSynthetic

package org.maplibre.spatialk.geojson.dsl

import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.Rotation
import org.maplibre.spatialk.units.extensions.degrees

private val LONGITUDE_RANGE = (-180).degrees..180.degrees

private val LATITUDE_RANGE = (-90).degrees..90.degrees

@GeoJsonDsl
public fun lngLat(longitude: Rotation, latitude: Rotation): Position {
    require(longitude in LONGITUDE_RANGE && latitude in LATITUDE_RANGE)
    return Position(longitude, latitude)
}

@GeoJsonDsl
public fun lngLat(longitude: Rotation, latitude: Rotation, altitude: Length): Position {
    require(longitude in LONGITUDE_RANGE && latitude in LATITUDE_RANGE)
    return Position(longitude, latitude, altitude)
}

@GeoJsonDsl
public fun lngLat(longitude: Rotation, latitude: Rotation, altitude: Length?): Position {
    require(longitude in LONGITUDE_RANGE && latitude in LATITUDE_RANGE)
    return Position(longitude, latitude, altitude)
}
