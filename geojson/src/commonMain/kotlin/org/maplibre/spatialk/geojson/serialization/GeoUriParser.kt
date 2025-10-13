package org.maplibre.spatialk.geojson.serialization

import kotlin.text.get
import org.maplibre.spatialk.geojson.Position

internal object GeoUriParser {
    // geo uri syntax as defined in RFC 5870 Section 3.3:
    // https://datatracker.ietf.org/doc/html/rfc5870#section-3.3
    // I'd make it cleaner with free spacing mode, but JS doesn't support it.
    private val syntax =
        "^([gG])([eE])([oO]):(?<lat>-?(\\d+(\\.\\d+)?)),(?<lon>-?(\\d+(\\.\\d+)?))(,(?<alt>-?(\\d+(\\.\\d+)?)))?(?<p>(;([-0-9a-zA-Z]+)(=((${$"[-_.!~*'()\\[\\]:&+$0-9a-zA-Z]"})|(%[0-9A-Fa-f]{2}))+)?)*)$"
            .toRegex()

    fun parsePosition(uri: String): Position {
        val result =
            syntax.matchEntire(uri) ?: throw IllegalArgumentException("Invalid Geo URI: $uri")

        result.groups["p"]?.value?.split(";")?.forEach { paramStr ->
            val value = paramStr.substringAfter('=', "")
            when (paramStr.substringBefore('=')) {
                // geojson only supports WGS 84
                "crs" ->
                    require(value.isEmpty() || value.lowercase() == "wgs84") { "crs must be wgs84" }
                // geojson doesn't support uncertainty
                "u" -> require(value.isEmpty() || value.toDouble() == 0.0) { "u must be zero" }
            }
            // all other properties are ignored
        }

        return Position(
            latitude = result.groups["lat"]!!.value.toDouble(),
            longitude = result.groups["lon"]!!.value.toDouble(),
            altitude = result.groups["alt"]?.value?.toDouble(),
        )
    }
}
