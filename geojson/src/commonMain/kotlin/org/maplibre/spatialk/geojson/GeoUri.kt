package org.maplibre.spatialk.geojson

internal object GeoUriParser {
    // geo uri syntax as defined at https://datatracker.ietf.org/doc/html/rfc5870#section-3.3

    private val coord = "-?(\\d+(\\.\\d+)?)".toRegex()
    private val unreservedChar = $"[-_.!~*'()\\[\\]:&+$0-9a-zA-Z]".toRegex()
    private val pctEncodedChar = "%[0-9A-Fa-f]{2}".toRegex()
    private val parameter = ";([-0-9a-zA-Z]+)(=(($unreservedChar)|($pctEncodedChar))+)?".toRegex()
    private val geoUri =
        "^([gG])([eE])([oO]):(?<lat>$coord),(?<lon>$coord)(,(?<alt>$coord))?(?<p>($parameter)*)$"
            .toRegex()

    fun parsePosition(uri: String): Position {
        val result =
            geoUri.matchEntire(uri) ?: throw IllegalArgumentException("Invalid Geo URI: $uri")

        result.groups["p"]?.value?.split(";")?.forEach { paramStr ->
            println(paramStr)
            val value = paramStr.substringAfter('=', "")
            when (paramStr.substringBefore('=')) {
                // geojson only supports WGS 84
                "crs" -> require(value.isEmpty() || value.lowercase() == "wgs84")
                // geojson doesn't support uncertainty
                "u" -> require(value.isEmpty() || value.toDouble() == 0.0)
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
