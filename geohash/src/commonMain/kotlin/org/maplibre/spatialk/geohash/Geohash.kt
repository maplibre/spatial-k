package org.maplibre.spatialk.geohash

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geohash.internal.BASE32_GHS
import org.maplibre.spatialk.geohash.internal.base32GhsValue
import org.maplibre.spatialk.geohash.internal.deinterleaveX
import org.maplibre.spatialk.geohash.internal.deinterleaveY
import org.maplibre.spatialk.geohash.internal.interleave
import org.maplibre.spatialk.geohash.serialization.GeohashSerializer
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

/**
 * A rectangular cell on the WGS84 longitude and latitude plane, addressed by a
 * [Geohash](https://en.wikipedia.org/wiki/Geohash) string.
 *
 * A `Geohash` is a cell rather than a string codec. Altitude is ignored.
 */
@Serializable(with = GeohashSerializer::class)
public class Geohash private constructor(private val packed: Long) : Comparable<Geohash> {
    /** Number of base32ghs characters in this cell's address. */
    public val length: Int
        get() = (packed and LengthMask).toInt()

    /** This cell's lowercase base32ghs address. */
    public val text: String
        get() {
            val characterCount = length
            return buildString(characterCount) {
                repeat(characterCount) { index ->
                    val shift = 64 - BitsPerCharacter * (index + 1)
                    append(BASE32_GHS[((packed ushr shift) and CharacterMask).toInt()])
                }
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

    /** The enclosing cell one character shorter, or `null` when [length] is `1`. */
    public val parent: Geohash?
        get() = if (length == 1) null else truncatedTo(length - 1)

    /**
     * The 32 finer cells, in base32ghs character order.
     *
     * Empty when [length] is [MaxLength].
     */
    public val children: List<Geohash>
        get() {
            if (length == MaxLength) return emptyList()
            val childLength = length + 1
            val shift = 64 - BitsPerCharacter * childLength
            val payload = packed and LengthMask.inv()
            return List(1 shl BitsPerCharacter) { index ->
                Geohash(payload or (index.toLong() shl shift) or childLength.toLong())
            }
        }

    /**
     * The adjacent cells at this [length], with named compass directions.
     *
     * Longitude wraps at the antimeridian. North-facing or south-facing entries are `null` when
     * this cell touches the corresponding pole.
     */
    public val neighbors: GeohashNeighbors
        get() =
            GeohashNeighbors(
                north = offsetBy(east = 0, north = 1),
                northEast = offsetBy(east = 1, north = 1),
                east = offsetBy(east = 1, north = 0)!!,
                southEast = offsetBy(east = 1, north = -1),
                south = offsetBy(east = 0, north = -1),
                southWest = offsetBy(east = -1, north = -1),
                west = offsetBy(east = -1, north = 0)!!,
                northWest = offsetBy(east = -1, north = 1),
            )

    /**
     * Returns the cell reached by moving [east] cells east and [north] cells north.
     *
     * Longitude wraps at the antimeridian. Latitude does not wrap, so a move past either pole
     * returns `null`.
     */
    public fun offsetBy(east: Int, north: Int): Geohash? {
        val longitudeBits = longitudeBits(length)
        val latitudeBits = latitudeBits(length)
        val longitudeCells = 1L shl longitudeBits
        val latitudeCells = 1L shl latitudeBits
        val currentX = deinterleaveX(packed, longitudeBits, latitudeBits)
        val currentY = deinterleaveY(packed, longitudeBits, latitudeBits)
        val unwrappedX = (currentX.toLong() + east.toLong()) % longitudeCells
        val x = if (unwrappedX < 0) unwrappedX + longitudeCells else unwrappedX
        val y = currentY.toLong() + north.toLong()
        if (y !in 0..<latitudeCells) return null

        val morton = interleave(x.toInt(), longitudeBits, y.toInt(), latitudeBits)
        return Geohash(morton or length.toLong())
    }

    /**
     * Returns the enclosing cell whose address has [length] characters.
     *
     * Returns this cell when [length] equals this cell's length.
     *
     * @throws IllegalArgumentException if [length] is outside `1..this.length`.
     */
    @Throws(IllegalArgumentException::class)
    public fun truncatedTo(length: Int): Geohash {
        require(length in 1..this.length) {
            "Target length must be in 1..${this.length}, but was $length"
        }
        val payloadMask = -1L shl (64 - BitsPerCharacter * length)
        return Geohash((packed and payloadMask) or length.toLong())
    }

    /**
     * Returns true when [position] falls inside this cell.
     *
     * Altitude is ignored. Invalid coordinates throw as they do in [of].
     */
    @Throws(IllegalArgumentException::class)
    public operator fun contains(position: Position): Boolean = of(position, length) == this

    /** Returns true when [other] is this cell or one of its descendants. */
    public operator fun contains(other: Geohash): Boolean =
        other.length >= length && other.truncatedTo(length) == this

    /**
     * Orders cells in the same order as their [text] forms.
     *
     * A parent address sorts before each of its descendants.
     */
    override fun compareTo(other: Geohash): Int = packed.toULong().compareTo(other.packed.toULong())

    override fun equals(other: Any?): Boolean = other is Geohash && packed == other.packed

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

/**
 * The eight cells adjacent to a [Geohash], one property per compass direction.
 *
 * Longitude wraps, so [east] and [west] always exist. Properties that cross a pole are `null`.
 */
public class GeohashNeighbors
internal constructor(
    /** The adjacent cell to the north, or `null` at the north pole. */
    public val north: Geohash?,
    /** The adjacent cell to the northeast, or `null` at the north pole. */
    public val northEast: Geohash?,
    /** The adjacent cell to the east. */
    public val east: Geohash,
    /** The adjacent cell to the southeast, or `null` at the south pole. */
    public val southEast: Geohash?,
    /** The adjacent cell to the south, or `null` at the south pole. */
    public val south: Geohash?,
    /** The adjacent cell to the southwest, or `null` at the south pole. */
    public val southWest: Geohash?,
    /** The adjacent cell to the west. */
    public val west: Geohash,
    /** The adjacent cell to the northwest, or `null` at the north pole. */
    public val northWest: Geohash?,
) {
    /**
     * Returns existing neighbors in N, NE, E, SE, S, SW, W, NW order.
     *
     * Directions that cross a pole are omitted.
     */
    public fun toList(): List<Geohash> =
        listOfNotNull(north, northEast, east, southEast, south, southWest, west, northWest)
}
