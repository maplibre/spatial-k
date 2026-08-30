package org.maplibre.spatialk.geohash.internal

internal fun interleave(x: Int, xBits: Int, y: Int, yBits: Int): Long {
    val totalBits = xBits + yBits
    var morton = 0L

    for (index in 0..<totalBits) {
        val bit =
            if (index % 2 == 0) {
                (x ushr (xBits - 1 - index / 2)) and 1
            } else {
                (y ushr (yBits - 1 - index / 2)) and 1
            }
        morton = morton or (bit.toLong() shl (63 - index))
    }

    return morton
}

internal fun deinterleaveX(morton: Long, xBits: Int, yBits: Int): Int {
    var x = 0
    for (index in 0..<(xBits + yBits) step 2) {
        x = (x shl 1) or ((morton ushr (63 - index)) and 1L).toInt()
    }
    return x
}

internal fun deinterleaveY(morton: Long, xBits: Int, yBits: Int): Int {
    var y = 0
    for (index in 1..<(xBits + yBits) step 2) {
        y = (y shl 1) or ((morton ushr (63 - index)) and 1L).toInt()
    }
    return y
}
