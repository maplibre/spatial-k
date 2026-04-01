package org.maplibre.spatialk.polyline

import kotlin.math.pow
import kotlin.math.roundToLong
import org.maplibre.spatialk.geojson.Position

/**
 * Encodes and decodes coordinate sequences using the Encoded Polyline Algorithm.
 *
 * The encoding logic is run following steps. Decoding the same in reversed order.
 * 1. Take the initial signed value
 * 2. Take the decimal value and multiply it by 10^precision, rounding the result
 * 3. Convert the decimal value to binary. Note that a negative value must be calculated using its two's complement by inverting the binary value and adding one to the result:
 * 4. Left-shift the binary value one bit:
 * 5. If the original decimal value is negative, invert this encoding
 * 6. Break the binary value out into 5-bit chunks (starting from the right hand side)
 * 7. Place the 5-bit chunks into reverse order
 * 8. OR each value with 0x20 if another bit chunk follows
 * 9. Convert each value to decimal
 * 10. Add 63 to each value
 * 11. Convert each value to its ASCII equivalent
 *
 * @see <a href="https://developers.google.com/maps/documentation/utilities/polylinealgorithm">
 *     Google Encoded Polyline Algorithm Format</a>
 */
public object PolylineEncoding {

    /**
     * Encode a list of coordinates to an encoded polyline string.
     *
     * @param coordinates the list of [Position] objects to encode
     * @param precision the number of decimal digits to encode (e.g. 5 for 1e5, the standard Google
     *   precision)
     * @return the encoded polyline string
     */
    public fun encode(coordinates: List<Position>, precision: Int): String {
        val factor = 10.0.pow(precision)
        val result = StringBuilder()
        var prevLat = 0L
        var prevLon = 0L

        for (position in coordinates) {
            val lat = (position.latitude * factor).roundToLong()
            val lon = (position.longitude * factor).roundToLong()

            encodeValue(lat - prevLat, result)
            encodeValue(lon - prevLon, result)

            prevLat = lat
            prevLon = lon
        }

        return result.toString()
    }

    /**
     * Decode an encoded polyline string to a list of coordinates.
     *
     * @param encoded the encoded polyline string
     * @param precision the number of decimal digits used during encoding (e.g. 5 for the standard
     *   Google precision)
     * @return the decoded list of [Position] objects
     */
    public fun decode(encoded: String, precision: Int): List<Position> {
        val factor = 10.0.pow(precision)
        val result = mutableListOf<Position>()
        var index = 0
        var lat = 0L
        var lon = 0L

        while (index < encoded.length) {
            lat += decodeValue(encoded, index).also { index += it.chunkCount }.value
            lon += decodeValue(encoded, index).also { index += it.chunkCount }.value

            result.add(Position(longitude = lon / factor, latitude = lat / factor))
        }

        return result
    }

    private fun encodeValue(value: Long, result: StringBuilder) {
        var encoded = if (value < 0) (value shl 1).inv() else value shl 1

        while (encoded >= 0x20L) {
            result.append(((0x20L or (encoded and 0x1FL)) + 63L).toInt().toChar())
            encoded = encoded ushr 5
        }
        result.append((encoded + 63L).toInt().toChar())
    }

    private data class DecodedValue(val value: Long, val chunkCount: Int)

    private fun decodeValue(encoded: String, startIndex: Int): DecodedValue {
        var result = 0L
        var shift = 0
        var index = startIndex

        var b: Int
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1F).toLong() shl shift)
            shift += 5
        } while (b >= 0x20)

        val value = if (result and 1L != 0L) (result shr 1).inv() else result shr 1
        return DecodedValue(value, index - startIndex)
    }
}
