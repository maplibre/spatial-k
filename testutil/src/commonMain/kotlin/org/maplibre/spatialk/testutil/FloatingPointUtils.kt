package org.maplibre.spatialk.testutil

import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.asserter
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position

fun assertDoubleEquals(
    expected: Double,
    actual: Double?,
    epsilon: Double = 0.0001,
    message: String? = null,
) {
    asserter.assertNotNull(null, actual)
    asserter.assertTrue(
        {
            (message ?: "") +
                "Expected <$expected>, actual <$actual>, should differ no more than <$epsilon>."
        },
        abs(expected - actual!!) <= epsilon,
    )
}

fun assertPositionEquals(
    expected: Position,
    actual: Position?,
    epsilon: Double = 0.0001,
    message: String? = null,
) {
    asserter.assertNotNull(null, actual)

    assertDoubleEquals(expected.latitude, actual?.latitude, epsilon, message)
    assertDoubleEquals(expected.longitude, actual?.longitude, epsilon, message)
}

fun assertLineStringEquals(
    expected: LineString,
    actual: LineString,
    epsilon: Double = 0.0001,
    message: String? = null,
) {
    assertEquals(expected.coordinates.size, actual.coordinates.size, "Coordinate count mismatch")
    expected.coordinates.forEachIndexed { index, expectedPos ->
        val actualPos = actual.coordinates[index]
        assertPositionEquals(expectedPos, actualPos, epsilon, (message ?: "") + " at index $index")
    }
}
