package org.maplibre.spatialk.turf.coordinatemutation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RoundTest {

    @Test
    fun testRoundWithDefaultPrecision() {
        assertEquals(3.0, 3.14159.round())
        assertEquals(4.0, 3.6.round())
        assertEquals(-3.0, (-3.14159).round())
        assertEquals(-4.0, (-3.6).round())
        assertEquals(0.0, 0.4.round())
        assertEquals(2.0, 1.5.round())
    }

    @Test
    fun testRoundWithZeroPrecision() {
        assertEquals(3.0, 3.14159.round())
        assertEquals(4.0, 3.6.round())
        assertEquals(-3.0, (-3.14159).round())
        assertEquals(0.0, 0.0.round())
    }

    @Test
    fun testRoundWithPositivePrecision() {
        assertEquals(3.1, 3.14159.round(1))
        assertEquals(3.14, 3.14159.round(2))
        assertEquals(3.142, 3.14159.round(3))
        assertEquals(3.1416, 3.14159.round(4))
        assertEquals(3.14159, 3.14159.round(5))

        assertEquals(-3.1, (-3.14159).round(1))
        assertEquals(-3.14, (-3.14159).round(2))
        assertEquals(-3.142, (-3.14159).round(3))
    }

    @Test
    fun testRoundNegativePrecisionThrowsException() {
        assertFailsWith<IllegalArgumentException> { 3.14159.round(-1) }
        assertFailsWith<IllegalArgumentException> { 0.0.round(-5) }
    }

    @Test
    fun testRoundWithVerySmallNumbers() {
        assertEquals(0.0001, 0.000051.round(4))
        assertEquals(0.0, 0.00004.round(4))
        assertEquals(0.001, 0.00051.round(3))
    }

    @Test
    fun testRoundWithLargeNumbers() {
        assertEquals(1000000.0, 999999.6.round())
        assertEquals(1234567.89, 1234567.891.round(2))
        assertEquals(1000000000.0, 999999999.9.round())
    }
}
