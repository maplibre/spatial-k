package org.maplibre.spatialk.testutil

import kotlin.math.abs
import kotlin.test.asserter
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.Rotation
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.inDegrees

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

fun assertRotationEquals(
    expected: Rotation,
    actual: Rotation?,
    epsilon: Rotation = 0.0001.degrees,
    message: String? = null,
) = assertDoubleEquals(expected.inDegrees, actual?.inDegrees, epsilon.inDegrees, message)

fun assertBearingEquals(
    expected: Bearing,
    actual: Bearing?,
    epsilon: Rotation = 0.0001.degrees,
    message: String? = null,
) = assertRotationEquals(expected - North, actual?.minus(North), epsilon, message)
