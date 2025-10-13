package org.maplibre.spatialk.units

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmSynthetic
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.DMS.ArcMinutes
import org.maplibre.spatialk.units.DMS.ArcSeconds
import org.maplibre.spatialk.units.DMS.Degrees
import org.maplibre.spatialk.units.extensions.degrees

/**
 * Represents an absolute bearing or heading, internally stored as a [Rotation] from [North] in the
 * range [0,360) degrees defined in the positive-clockwise direction when viewed from above (axis of
 * rotation is a vector pointing down).
 */
@JvmInline
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

    override fun toString(): String = toString(Degrees)

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

    public companion object {
        public val North: Bearing = Bearing(0.degrees)
        public val NorthEast: Bearing = Bearing(45.degrees)
        public val East: Bearing = Bearing(90.degrees)
        public val SouthEast: Bearing = Bearing(135.degrees)
        public val South: Bearing = Bearing(180.degrees)
        public val SouthWest: Bearing = Bearing(225.degrees)
        public val West: Bearing = Bearing(270.degrees)
        public val NorthWest: Bearing = Bearing(315.degrees)

        @JvmSynthetic
        internal fun of(rotationFromNorth: Rotation) = Bearing(rotationFromNorth.mod(360.degrees))
    }
}
