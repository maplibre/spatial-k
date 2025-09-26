package org.maplibre.spatialk.turf

/**
 * Radius of the Earth used with the Harvesine formula. Approximated using a spherical
 * (non-ellipsoid) Earth.
 */
public const val EARTH_RADIUS: Double = 6371008.8

internal const val ANTIMERIDIAN_POS = 180.0
internal const val ANTIMERIDIAN_NEG = -180.0

/**
 * Supported units of measurement in Turf.
 *
 * @property unitFactor Units of measurement factors in relation to 1 meter.
 * @property factor Units of measurement factors using a spherical earth radius.
 * @property areaFactor Area of measurement factors based on 1 square meter.
 */
@ExperimentalTurfApi
public enum class Units(
    internal val unitFactor: Double,
    internal val factor: Double,
    internal val areaFactor: Double,
) {
    Meters(1.0, EARTH_RADIUS, 1.0),
    Millimeters(1000.0, EARTH_RADIUS * 1000, 1_000_000.0),
    Centimeters(100.0, EARTH_RADIUS * 100, 10_000.0),
    Kilometers(1 / 1000.0, EARTH_RADIUS / 1000, 0.000_001),
    Acres(Double.NaN, Double.NaN, 0.000_247_105),
    Miles(1 / 1609.344, EARTH_RADIUS / 1609.344, 3.86e-7),
    NauticalMiles(1 / 1852.0, EARTH_RADIUS / 1852.0, Double.NaN),
    Inches(39.370, EARTH_RADIUS * 39.370, 1550.003_100_006),
    Yards(1 / 1.0936, EARTH_RADIUS / 1.0936, 1.195_990_046),
    Feet(3.28084, EARTH_RADIUS * 3.28084, 10.763_910_417),
    Radians(1 / EARTH_RADIUS, 1.0, Double.NaN),
    Degrees(1 / 111325.0, EARTH_RADIUS / 111325, Double.NaN),
}
