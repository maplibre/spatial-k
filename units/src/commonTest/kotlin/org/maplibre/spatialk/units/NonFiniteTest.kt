package org.maplibre.spatialk.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.maplibre.spatialk.units.extensions.*

class NonFiniteTest {
    @Test
    fun lengthClassification() {
        for (value in listOf(Length.NegativeInfinity, Length.PositiveInfinity)) {
            assertTrue(value.isInfinite)
            assertFalse(value.isFinite)
        }
        for (value in listOf((-1).meters, Length.Zero, Length.MinValue, Length.MaxValue)) {
            assertFalse(value.isInfinite)
            assertTrue(value.isFinite)
        }
        val nan = Length.Zero / 0.0
        assertFalse(nan.isInfinite)
        assertFalse(nan.isFinite)
    }

    @Test
    fun areaClassification() {
        for (value in listOf(Area.NegativeInfinity, Area.PositiveInfinity)) {
            assertTrue(value.isInfinite)
            assertFalse(value.isFinite)
        }
        for (value in listOf((-1).squareMeters, Area.Zero, Area.MinValue, Area.MaxValue)) {
            assertFalse(value.isInfinite)
            assertTrue(value.isFinite)
        }
        val nan = Area.Zero / 0.0
        assertFalse(nan.isInfinite)
        assertFalse(nan.isFinite)
    }

    @Test
    fun rotationClassification() {
        for (value in listOf(Rotation.NegativeInfinity, Rotation.PositiveInfinity)) {
            assertTrue(value.isInfinite)
            assertFalse(value.isFinite)
        }
        for (value in listOf((-1).degrees, Rotation.Zero, Rotation.MinValue, Rotation.MaxValue)) {
            assertFalse(value.isInfinite)
            assertTrue(value.isFinite)
        }
        val nan = Rotation.Zero / 0.0
        assertFalse(nan.isInfinite)
        assertFalse(nan.isFinite)
    }

    @Test
    fun rotationDmsString() {
        assertEquals("Infinity°", Rotation.PositiveInfinity.toDmsString())
        assertEquals("-Infinity°", Rotation.NegativeInfinity.toDmsString())
        assertEquals("NaN°", (Rotation.Zero / 0.0).toDmsString())
    }
}
