package org.maplibre.spatialk.geohash.internal

internal const val BASE32_GHS: String = "0123456789bcdefghjkmnpqrstuvwxyz"

internal const val OSM_BASE64: String =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_~"

private val base32GhsValues =
    IntArray(128) { -1 }
        .apply {
            BASE32_GHS.forEachIndexed { index, char -> this[char.code] = index }
        }

private val osmBase64Values =
    IntArray(128) { -1 }
        .apply {
            OSM_BASE64.forEachIndexed { index, char -> this[char.code] = index }
            this['@'.code] = OSM_BASE64.lastIndex
        }

internal fun base32GhsValue(char: Char): Int =
    if (char.code < base32GhsValues.size) base32GhsValues[char.code] else -1

internal fun osmBase64Value(char: Char): Int =
    if (char.code < osmBase64Values.size) osmBase64Values[char.code] else -1
