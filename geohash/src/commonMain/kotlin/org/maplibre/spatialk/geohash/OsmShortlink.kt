package org.maplibre.spatialk.geohash

import kotlin.jvm.JvmStatic
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geohash.internal.OSM_BASE64
import org.maplibre.spatialk.geohash.internal.deinterleaveX
import org.maplibre.spatialk.geohash.internal.deinterleaveY
import org.maplibre.spatialk.geohash.internal.interleave
import org.maplibre.spatialk.geohash.internal.osmBase64Value
import org.maplibre.spatialk.geohash.serialization.OsmShortlinkSerializer
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

/**
 * An [OpenStreetMap shortlink](https://wiki.openstreetmap.org/wiki/Shortlink).
 *
 * This sibling encoding is not a [Geohash]. It uses a modified base64 alphabet, carries a [zoom],
 * and has no cell hierarchy or neighbor operations.
 */
@Serializable(with = OsmShortlinkSerializer::class)
public class OsmShortlink
private constructor(
    private val morton: Long,
    /** The OpenStreetMap zoom level encoded by this shortlink. */
    public val zoom: Int,
) {
    /** The canonical shortlink code without an `osm.org/go/` URL. */
    public val text: String
        get() {
            val characterCount = dataCharacterCount(zoom)
            return buildString(characterCount + paddingCount(zoom)) {
                repeat(characterCount) { index ->
                    val shift = 64 - BitsPerCharacter * (index + 1)
                    append(OSM_BASE64[((morton ushr shift) and CharacterMask).toInt()])
                }
                repeat(paddingCount(zoom)) { append('-') }
            }
        }

    /** The geometric center of the area addressed by this shortlink. */
    public val center: Position
        get() {
            val box = boundingBox
            return Position(
                longitude = (box.west + box.east) / 2.0,
                latitude = (box.south + box.north) / 2.0,
            )
        }

    /** The two-dimensional area addressed by this shortlink. */
    public val boundingBox: BoundingBox
        get() {
            val axisBits = dataCharacterCount(zoom) * BitsPerAxisPerCharacter
            val x = deinterleaveX(morton, axisBits, axisBits)
            val y = deinterleaveY(morton, axisBits, axisBits)
            val longitudeStep = LongitudeSpan / (1L shl axisBits)
            val latitudeStep = LatitudeSpan / (1L shl axisBits)
            val west = MinimumLongitude + x * longitudeStep
            val south = MinimumLatitude + y * latitudeStep
            return BoundingBox(
                west = west,
                south = south,
                east = west + longitudeStep,
                north = south + latitudeStep,
            )
        }

    /** Returns true when [other] has the same canonical code. */
    override fun equals(other: Any?): Boolean =
        other is OsmShortlink && morton == other.morton && zoom == other.zoom

    override fun hashCode(): Int = 31 * morton.hashCode() + zoom

    /** Returns [text]. */
    override fun toString(): String = text

    /** Factories for valid [OsmShortlink] values. */
    public companion object {
        /** The highest zoom supported by the OpenStreetMap 64-bit Morton layout. */
        public const val MaxZoom: Int = 22

        private const val BitsPerCharacter: Int = 6
        private const val BitsPerAxisPerCharacter: Int = 3
        private const val FullAxisBits: Int = 32
        private const val FullAxisCells: Long = 1L shl FullAxisBits
        private const val CharacterMask: Long = 0x3F
        private const val MinimumLongitude: Double = -180.0
        private const val MaximumLongitude: Double = 180.0
        private const val MinimumLatitude: Double = -90.0
        private const val MaximumLatitude: Double = 90.0
        private const val LongitudeSpan: Double = 360.0
        private const val LatitudeSpan: Double = 180.0

        /**
         * Returns the shortlink for [position] at [zoom].
         *
         * Longitude must be in `-180.0..180.0`, latitude must be in `-90.0..90.0`, and both values
         * must be finite. Altitude is ignored.
         *
         * @throws IllegalArgumentException if the zoom or coordinate is invalid.
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun of(position: Position, zoom: Int): OsmShortlink {
            require(zoom in 0..MaxZoom) {
                "OpenStreetMap shortlink zoom must be in 0..$MaxZoom, but was $zoom"
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

            val axisBits = dataCharacterCount(zoom) * BitsPerAxisPerCharacter
            val x = axisIndex(position.longitude, MinimumLongitude, LongitudeSpan, axisBits)
            val y = axisIndex(position.latitude, MinimumLatitude, LatitudeSpan, axisBits)
            return OsmShortlink(interleave(x, axisBits, y, axisBits), zoom)
        }

        /**
         * Parses a shortlink code, `/go/` path, or OpenStreetMap URL.
         *
         * URLs may use `osm.org`, `www.osm.org`, `openstreetmap.org`, or `www.openstreetmap.org`.
         * Historical `@` and `=` characters are accepted and [text] emits `~` and `-`.
         *
         * @throws IllegalArgumentException if [text] is not a valid OpenStreetMap shortlink.
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun parse(text: String): OsmShortlink {
            val code = extractCode(text).replace('@', '~').replace('=', '-')
            require(code.isNotEmpty()) { "OpenStreetMap shortlink code must not be empty" }

            val firstPadding = code.indexOf('-').let { if (it < 0) code.length else it }
            val data = code.substring(0, firstPadding)
            val padding = code.length - firstPadding
            require(padding in 0..2 && code.drop(firstPadding).all { it == '-' }) {
                "OpenStreetMap shortlink padding must be zero, one, or two trailing '-' characters"
            }
            require(data.isNotEmpty()) {
                "OpenStreetMap shortlink code must contain data characters"
            }

            var morton = 0L
            data.forEachIndexed { index, char ->
                val value = osmBase64Value(char)
                require(value >= 0) {
                    "Invalid OpenStreetMap shortlink character '$char' at index $index"
                }
                val shift = 64 - BitsPerCharacter * (index + 1)
                require(shift >= 0) { "OpenStreetMap shortlink code is too long" }
                morton = morton or (value.toLong() shl shift)
            }

            val zoom =
                data.length * BitsPerAxisPerCharacter -
                    8 -
                    when (padding) {
                        0 -> 0
                        1 -> 2
                        else -> 1
                    }
            require(zoom in 0..MaxZoom) {
                "OpenStreetMap shortlink zoom must be in 0..$MaxZoom, but was $zoom"
            }
            return OsmShortlink(morton, zoom)
        }

        /** Parses an OpenStreetMap shortlink, or returns `null` when [text] is invalid. */
        @JvmStatic
        public fun parseOrNull(text: String): OsmShortlink? =
            try {
                parse(text)
            } catch (_: IllegalArgumentException) {
                null
            }

        private fun dataCharacterCount(zoom: Int): Int =
            (zoom + 8 + BitsPerAxisPerCharacter - 1) / BitsPerAxisPerCharacter

        private fun paddingCount(zoom: Int): Int = (zoom + 8) % BitsPerAxisPerCharacter

        private fun axisIndex(value: Double, minimum: Double, span: Double, axisBits: Int): Int {
            val raw = (((value - minimum) / span) * FullAxisCells).toLong()
            val wrapped = if (raw !in 0..<FullAxisCells) 0L else raw
            return (wrapped ushr (FullAxisBits - axisBits)).toInt()
        }

        private fun hostFromAuthority(authority: String): String {
            val colon = authority.indexOf(':')
            if (colon < 0) return authority.lowercase()

            val port = authority.substring(colon + 1)
            require(port.isNotEmpty() && port.all { it in '0'..'9' }) {
                "OpenStreetMap shortlink URL has an invalid authority"
            }
            return authority.substring(0, colon).lowercase()
        }

        private fun extractCode(text: String): String {
            if (text.startsWith("/go/")) return codeFromPath(text)

            val schemeEnd = text.indexOf("://")
            if (schemeEnd < 0) return text

            val scheme = text.substring(0, schemeEnd).lowercase()
            require(scheme == "http" || scheme == "https") {
                "OpenStreetMap shortlink URL must use HTTP or HTTPS"
            }
            val remainder = text.substring(schemeEnd + 3)
            val authorityEnd =
                remainder.indexOfAny(charArrayOf('/', '?', '#')).let {
                    if (it < 0) remainder.length else it
                }
            val authority = remainder.substring(0, authorityEnd)
            require(authority.isNotEmpty() && '@' !in authority) {
                "OpenStreetMap shortlink URL has an invalid authority"
            }
            val host = hostFromAuthority(authority)
            require(
                host == "osm.org" ||
                    host == "www.osm.org" ||
                    host == "openstreetmap.org" ||
                    host == "www.openstreetmap.org"
            ) {
                "OpenStreetMap shortlink URL has an unsupported host"
            }
            return codeFromPath(remainder.substring(authorityEnd))
        }

        private fun codeFromPath(path: String): String {
            require(path.startsWith("/go/")) {
                "OpenStreetMap shortlink path must start with /go/"
            }
            return path.removePrefix("/go/").substringBefore('?').substringBefore('#')
        }
    }
}
