package org.maplibre.spatialk.geohash

import kotlin.jvm.JvmStatic
import org.maplibre.spatialk.geohash.internal.BASE32_GHS
import org.maplibre.spatialk.geohash.internal.base32GhsValue
import org.maplibre.spatialk.geohash.internal.deinterleaveX
import org.maplibre.spatialk.geohash.internal.deinterleaveY
import org.maplibre.spatialk.geohash.internal.interleave
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

/**
 * A rectangular cell on the WGS84 longitude and latitude plane, addressed by a
 * [Geohash](https://en.wikipedia.org/wiki/Geohash) string.
 *
 * A `Geohash` is a cell rather than a string codec. Its [text], [center], and [boundingBox] are
 * projections of one packed value. Altitude is ignored.
 *
 * Every instance is valid. Construction validates untrusted coordinates or text once, then cell
 * operations trust the instance.
 */
public class Geohash private constructor(private val packed: Long) : Comparable<Geohash> {
    /** Number of base32ghs characters in this cell's address. */
    public val length: Int
        get() = (packed and LengthMask).toInt()

    /** This cell's lowercase base32ghs address. */
    public val text: String
        get() =
            buildString(length) {
                repeat(length) { index ->
                    val shift = 64 - BitsPerCharacter * (index + 1)
                    append(BASE32_GHS[((packed ushr shift) and CharacterMask).toInt()])
                }
            }

    /**
     * The geometric center of this cell.
     *
     * Encoding the result at this cell's [length] returns an equal cell.
     */
    public val center: Position
        get() {
            val box = boundingBox
            return Position(
                longitude = (box.west + box.east) / 2.0,
                latitude = (box.south + box.north) / 2.0,
            )
        }

    /**
     * The cell's two-dimensional extent in west, south, east, north order.
     *
     * The east and north edges are exclusive except at longitude `180` and latitude `90`.
     */
    public val boundingBox: BoundingBox
        get() {
            val longitudeBits = longitudeBits(length)
            val latitudeBits = latitudeBits(length)
            val x = deinterleaveX(packed, longitudeBits, latitudeBits)
            val y = deinterleaveY(packed, longitudeBits, latitudeBits)
            val longitudeStep = LongitudeSpan / (1L shl longitudeBits)
            val latitudeStep = LatitudeSpan / (1L shl latitudeBits)
            val west = MinimumLongitude + x * longitudeStep
            val south = MinimumLatitude + y * latitudeStep
            return BoundingBox(
                west = west,
                south = south,
                east = west + longitudeStep,
                north = south + latitudeStep,
            )
        }

    /**
     * Orders cells in the same order as their [text] forms.
     *
     * A parent address sorts before each of its descendants.
     */
    override fun compareTo(other: Geohash): Int = packed.toULong().compareTo(other.packed.toULong())

    /** Returns true when [other] identifies the same cell. */
    override fun equals(other: Any?): Boolean = other is Geohash && packed == other.packed

    /** Returns a hash code consistent with [equals]. */
    override fun hashCode(): Int = packed.hashCode()

    /** Returns [text]. */
    override fun toString(): String = text

    /** Factories for valid [Geohash] cells. */
    public companion object {
        /** The longest supported address, `12` characters. */
        public const val MaxLength: Int = 12

        private const val BitsPerCharacter: Int = 5
        private const val LengthMask: Long = 0xF
        private const val CharacterMask: Long = 0x1F
        private const val MinimumLongitude: Double = -180.0
        private const val MaximumLongitude: Double = 180.0
        private const val MinimumLatitude: Double = -90.0
        private const val MaximumLatitude: Double = 90.0
        private const val LongitudeSpan: Double = 360.0
        private const val LatitudeSpan: Double = 180.0

        /**
         * Returns the cell of the given [length] that contains [position].
         *
         * Longitude must be in `-180.0..180.0`, latitude must be in `-90.0..90.0`, and both values
         * must be finite. Altitude is ignored.
         *
         * @throws IllegalArgumentException if the length or coordinate is invalid.
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun of(position: Position, length: Int): Geohash {
            require(length in 1..MaxLength) {
                "Geohash length must be in 1..$MaxLength, but was $length"
            }
            require(
                position.longitude.isFinite() &&
                    position.longitude in MinimumLongitude..MaximumLongitude
            ) {
                "Longitude must be finite and in $MinimumLongitude..$MaximumLongitude, but was ${position.longitude}"
            }
            require(
                position.latitude.isFinite() &&
                    position.latitude in MinimumLatitude..MaximumLatitude
            ) {
                "Latitude must be finite and in $MinimumLatitude..$MaximumLatitude, but was ${position.latitude}"
            }

            val longitudeBits = longitudeBits(length)
            val latitudeBits = latitudeBits(length)
            val longitudeCells = 1 shl longitudeBits
            val latitudeCells = 1 shl latitudeBits
            val x =
                (((position.longitude - MinimumLongitude) / LongitudeSpan) * longitudeCells)
                    .toInt()
                    .coerceAtMost(longitudeCells - 1)
            val y =
                (((position.latitude - MinimumLatitude) / LatitudeSpan) * latitudeCells)
                    .toInt()
                    .coerceAtMost(latitudeCells - 1)
            val morton = interleave(x, longitudeBits, y, latitudeBits)
            return Geohash(morton or length.toLong())
        }

        /**
         * Parses a base32ghs address after folding ASCII letters to lowercase.
         *
         * The address must contain `1..`[MaxLength] characters from
         * `0123456789bcdefghjkmnpqrstuvwxyz`. For every accepted input, `parse(input).text ==
         * input.lowercase()`.
         *
         * @throws IllegalArgumentException if [text] has an invalid length or character.
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun parse(text: String): Geohash {
            require(text.length in 1..MaxLength) {
                "Geohash text length must be in 1..$MaxLength, but was ${text.length}"
            }

            var bits = 0L
            text.forEachIndexed { index, char ->
                val folded = if (char in 'A'..'Z') char + ('a' - 'A') else char
                val value = base32GhsValue(folded)
                require(value >= 0) {
                    "Invalid geohash character '$char' at index $index"
                }
                bits = (bits shl BitsPerCharacter) or value.toLong()
            }

            return Geohash((bits shl (64 - BitsPerCharacter * text.length)) or text.length.toLong())
        }

        /** Parses a base32ghs address, or returns `null` when [text] is invalid. */
        @JvmStatic
        public fun parseOrNull(text: String): Geohash? =
            try {
                parse(text)
            } catch (_: IllegalArgumentException) {
                null
            }

        private fun longitudeBits(length: Int): Int = (BitsPerCharacter * length + 1) / 2

        private fun latitudeBits(length: Int): Int = BitsPerCharacter * length / 2
    }
}
