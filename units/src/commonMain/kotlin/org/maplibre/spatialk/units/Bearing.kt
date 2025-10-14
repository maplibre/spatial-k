package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.DMS.ArcMinutes
import org.maplibre.spatialk.units.DMS.ArcSeconds
import org.maplibre.spatialk.units.DMS.Degrees
import org.maplibre.spatialk.units.extensions.degrees

/**
 * Represents an absolute bearing or heading, internally stored as a [Rotation] from [North] in the
 * range [0,360) degrees defined in the positive-clockwise direction when viewed from above (axis of
 * rotation is a vector pointing down).
 *
 * @see Rotation
 */
@JvmInline
@Serializable
public value class Bearing private constructor(private val rotationFromNorth: Rotation) {

    /**
     * Rotate this bearing by the given [Rotation] clockwise, wrapping in [0,360) degrees if
     * necessary.
     */
    public operator fun plus(other: Rotation): Bearing = of(rotationFromNorth + other)

    /**
     * Rotate this bearing by the given [Rotation] anticlockwise, wrapping in [0,360) degrees if
     * necessary.
     */
    public operator fun minus(other: Rotation): Bearing = of(rotationFromNorth - other)

    /**
     * Inverse of [clockwiseRotationTo]. Calculate the nonnegative [Rotation] such that rotating
     * [other] by the [Rotation] brings you to this.
     *
     * @return a [Rotation] in the range [0,360) degrees.
     */
    public operator fun minus(other: Bearing): Rotation = other.clockwiseRotationTo(this)

    /**
     * Calculate the nonnegative [Rotation] such that rotating this by the [Rotation] brings you to
     * [other].
     *
     * @return a [Rotation] in the range [0,360) degrees.
     */
    public fun clockwiseRotationTo(other: Bearing): Rotation =
        (other.rotationFromNorth - rotationFromNorth).mod(360.degrees)

    /**
     * Calculate the smallest [Rotation] such that rotating this by the [Rotation] brings you to
     * [other].
     *
     * @return a [Rotation] in the range (-180,180] degrees.
     */
    public fun smallestRotationTo(other: Bearing): Rotation {
        val offset = this.clockwiseRotationTo(other)
        if (offset > 180.degrees) return (offset - 360.degrees)
        return offset
    }

    /** Returns a string representation of this bearing as a quadrant bearing. */
    override fun toString(): String = toString(Degrees)

    /**
     * Returns a string representation of this bearing as a quadrant bearing with the specified
     * [unit] and [decimalPlaces].
     */
    public fun toString(unit: RotationUnit = Degrees, decimalPlaces: Int = 2): String =
        when (this - North) {
            in 0.degrees..<90.degrees -> "N ${(this - North).toString(unit, decimalPlaces)} E"
            in 90.degrees..<180.degrees -> "S ${(South - this).toString(unit, decimalPlaces)} E"
            in 180.degrees..<270.degrees -> "S ${(this - South).toString(unit, decimalPlaces)} W"
            else -> "N ${(North - this).toString(unit, decimalPlaces)} W"
        }

    /**
     * Format this [Bearing] as a quadrant bearing with [Degrees], [ArcMinutes], and [ArcSeconds]
     * components.
     *
     * @param decimalPlaces the number of decimal places to use for the arc seconds component.
     */
    public fun toDmsString(decimalPlaces: Int = 2): String =
        when (this - North) {
            in 0.degrees..<90.degrees -> "N ${(this - North).toDmsString( decimalPlaces)} E"
            in 90.degrees..<180.degrees -> "S ${(South - this).toDmsString( decimalPlaces)} E"
            in 180.degrees..<270.degrees -> "S ${(this - South).toDmsString( decimalPlaces)} W"
            else -> "N ${(North - this).toDmsString( decimalPlaces)} W"
        }

    /** Companion object containing cardinal and intercardinal bearing directions. */
    public companion object {
        /** North bearing. */
        public val North: Bearing = Bearing(0.degrees)

        /** North-northeast bearing. */
        public val NorthNortheast: Bearing = Bearing(22.5.degrees)

        /** Northeast bearing. */
        public val Northeast: Bearing = Bearing(45.degrees)

        /** East-northeast bearing. */
        public val EastNortheast: Bearing = Bearing(67.5.degrees)

        /** East bearing. */
        public val East: Bearing = Bearing(90.degrees)

        /** East-southeast bearing. */
        public val EastSoutheast: Bearing = Bearing(112.5.degrees)

        /** Southeast bearing. */
        public val Southeast: Bearing = Bearing(135.degrees)

        /** South-southeast bearing. */
        public val SouthSoutheast: Bearing = Bearing(157.5.degrees)

        /** South bearing. */
        public val South: Bearing = Bearing(180.degrees)

        /** South-southwest bearing. */
        public val SouthSouthwest: Bearing = Bearing(202.5.degrees)

        /** Southwest bearing. */
        public val Southwest: Bearing = Bearing(225.degrees)

        /** West-southwest bearing. */
        public val WestSouthwest: Bearing = Bearing(247.5.degrees)

        /** West bearing. */
        public val West: Bearing = Bearing(270.degrees)

        /** West-northwest bearing. */
        public val WestNorthwest: Bearing = Bearing(292.5.degrees)

        /** Northwest bearing. */
        public val Northwest: Bearing = Bearing(315.degrees)

        /** North-northwest bearing. */
        public val NorthNorthwest: Bearing = Bearing(337.5.degrees)

        @JvmSynthetic
        internal fun of(rotationFromNorth: Rotation) = Bearing(rotationFromNorth.mod(360.degrees))
    }
}
