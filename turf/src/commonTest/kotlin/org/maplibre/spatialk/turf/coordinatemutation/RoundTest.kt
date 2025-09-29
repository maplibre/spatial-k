package org.maplibre.spatialk.turf.coordinatemutation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RoundTest {

    @Test
    fun testRoundWithDefaultPrecision() {
        assertEquals(3.0, round(3.14159))
        assertEquals(4.0, round(3.6))
        assertEquals(-3.0, round(-3.14159))
        assertEquals(-4.0, round(-3.6))
        assertEquals(0.0, round(0.4))
        assertEquals(2.0, round(1.5))
    }

    @Test
    fun testRoundWithZeroPrecision() {
        assertEquals(3.0, round(3.14159, 0))
        assertEquals(4.0, round(3.6, 0))
        assertEquals(-3.0, round(-3.14159, 0))
        assertEquals(0.0, round(0.0, 0))
    }

    @Test
    fun testRoundWithPositivePrecision() {
        assertEquals(3.1, round(3.14159, 1))
        assertEquals(3.14, round(3.14159, 2))
        assertEquals(3.142, round(3.14159, 3))
        assertEquals(3.1416, round(3.14159, 4))
        assertEquals(3.14159, round(3.14159, 5))

        assertEquals(-3.1, round(-3.14159, 1))
        assertEquals(-3.14, round(-3.14159, 2))
        assertEquals(-3.142, round(-3.14159, 3))
    }

    @Test
    fun testRoundNegativePrecisionThrowsException() {
        assertFailsWith<IllegalArgumentException> { round(3.14159, -1) }
        assertFailsWith<IllegalArgumentException> { round(0.0, -5) }
    }

    @Test
    fun testRoundWithVerySmallNumbers() {
        assertEquals(0.0001, round(0.000051, 4))
        assertEquals(0.0, round(0.00004, 4))
        assertEquals(0.001, round(0.00051, 3))
    }

    @Test
    fun testRoundWithLargeNumbers() {
        assertEquals(1000000.0, round(999999.6, 0))
        assertEquals(1234567.89, round(1234567.891, 2))
        assertEquals(1000000000.0, round(999999999.9, 0))
    }
}
