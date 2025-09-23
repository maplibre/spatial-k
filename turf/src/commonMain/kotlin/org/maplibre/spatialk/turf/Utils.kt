package org.maplibre.spatialk.turf

/**
 * Convert a distance measurement (assuming a spherical Earth) from radians to a more friendly unit.
 *
 * @param radians Radians in radians across the sphere
 * @param units Can be [Miles][Units.Miles], [NauticalMiles][Units.NauticalMiles], [Inches][Units.Inches],
 * [Yards][Units.Yards], [Meters][Units.Meters], [Kilometers][Units.Kilometers], [Centimeters][Units.Centimeters],
 * [Feet][Units.Feet], [Degrees][Units.Degrees], [Radians][Units.Radians]
 * @return Distance
 *
 * @exception IllegalArgumentException if the given units are invalid
 */
@ExperimentalTurfApi
public fun radiansToLength(radians: Double, units: Units = Units.Kilometers): Double {
    require(!units.factor.isNaN()) { "${units.name} units is invalid" }
    return radians * units.factor
}

/**
 * Convert a distance measurement (assuming a spherical Earth) from a real-world unit into radians.
 *
 * @param distance Distance in real units
 * @param units Can be [Miles][Units.Miles], [NauticalMiles][Units.NauticalMiles], [Inches][Units.Inches],
 * [Yards][Units.Yards], [Meters][Units.Meters], [Kilometers][Units.Kilometers], [Centimeters][Units.Centimeters],
 * [Feet][Units.Feet], [Degrees][Units.Degrees], [Radians][Units.Radians]
 * @return Radians
 *
 * @exception IllegalArgumentException if the given units are invalid
 */
@ExperimentalTurfApi
public fun lengthToRadians(distance: Double, units: Units = Units.Kilometers): Double {
    require(!units.factor.isNaN()) { "${units.name} units is invalid" }
    return distance / units.factor
}

/**
 * Convert a distance measurement (assuming a spherical Earth) from a real-world unit into degrees.
 *
 * @param distance Distance in real units
 * @param units Can be [Miles][Units.Miles], [NauticalMiles][Units.NauticalMiles], [Inches][Units.Inches],
 * [Yards][Units.Yards], [Meters][Units.Meters], [Kilometers][Units.Kilometers], [Centimeters][Units.Centimeters],
 * [Feet][Units.Feet], [Degrees][Units.Degrees], [Radians][Units.Radians]
 * @return Degrees
 *
 * @exception IllegalArgumentException if the given units are invalid
 */
@ExperimentalTurfApi
public fun lengthToDegrees(distance: Double, units: Units = Units.Kilometers): Double =
    degrees(
        lengthToRadians(
            distance,
            units
        )
    )

/**
 * Converts a length to the requested unit
 *
 * @param length Length to be converted
 * @param from Unit of the [length]
 * @param to Unit to convert the [length] to
 * @returns The converted length
 *
 * @exception IllegalArgumentException if the given length is negative
 */
@ExperimentalTurfApi
public fun convertLength(length: Double, from: Units = Units.Meters, to: Units = Units.Kilometers): Double {
    require(length >= 0) { "length must be a positive number" }
    return radiansToLength(
        lengthToRadians(
            length,
            from
        ), to
    )
}

/**
 * Converts an area to the requested unit.
 * Valid units: [Acres][Units.Acres], [Miles][Units.Miles], [Inches][Units.Inches], [Yards][Units.Yards],
 * [Meters][Units.Meters], [Kilometers][Units.Kilometers], [Centimeters][Units.Centimeters], [Feet][Units.Feet]
 *
 * @param area Area to be converted
 * @param from Original units of the [area]
 * @param to Units to convert the [area] to
 * @return the converted area
 *
 * @exception IllegalArgumentException if the given units are invalid, or if the area is negative
 */
@Suppress("ThrowsCount")
@ExperimentalTurfApi
public fun convertArea(area: Double, from: Units = Units.Meters, to: Units = Units.Kilometers): Double {
    require(area >= 0) { "area must be a positive number" }
    require(!from.areaFactor.isNaN()) { "invalid original units" }
    require(!to.areaFactor.isNaN()) { "invalid final units" }

    return (area / from.areaFactor) * to.areaFactor
}

/**
 * Converts any bearing angle from the north line direction (positive clockwise)
 * and returns an angle between 0-360 degrees (positive clockwise), 0 being the north line
 *
 * @param bearing angle, between -180 and +180 degrees
 * @return angle between 0 and 360 degrees
 */
@Suppress("MagicNumber")
@ExperimentalTurfApi
public fun bearingToAzimuth(bearing: Double): Double {
    var angle = bearing % 360
    if (angle < 0) {
        angle += 360
    }
    return angle
}
