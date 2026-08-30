package org.maplibre.spatialk.turf.measurement

import kotlin.test.Test
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.testutil.assertPositionEquals
import org.maplibre.spatialk.testutil.readResourceFile
import org.maplibre.spatialk.units.Bearing.Companion.North
import org.maplibre.spatialk.units.Imperial.Miles
import org.maplibre.spatialk.units.International.Kilometers
import org.maplibre.spatialk.units.LengthUnit
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.toLength

class RhumbOffsetTest {

    @Test
    fun testRhumbOffset() {
        mapOf(
                "measurement/rhumbdestination/in/fiji-east-west.geojson" to
                    Position(-180.43794519555667, -16.5),
                "measurement/rhumbdestination/in/fiji-east-west-539-lng.geojson" to
                    Position(-540.4379451955566, -16.5),
                "measurement/rhumbdestination/in/fiji-west-east.geojson" to
                    Position(180.72058412338447, -17.174490272793403),
                "measurement/rhumbdestination/in/point-0.geojson" to
                    Position(-75.0, 39.00028098645979),
                "measurement/rhumbdestination/in/point-90.geojson" to
                    Position(-73.84279091917494, 39.0),
                "measurement/rhumbdestination/in/point-180.geojson" to
                    Position(-75.0, 38.10067963627546),
                "measurement/rhumbdestination/in/point-way-far-away.geojson" to
                    Position(18.117374548567227, 39.0),
            )
            .forEach { (path, expectedPosition) ->
                val feature = Feature.fromJson<Point, JsonObject?>(readResourceFile(path))
                val props = feature.properties
                val bearing =
                    North + (props?.get("bearing")?.jsonPrimitive?.doubleOrNull ?: 180.0).degrees
                val unit: LengthUnit =
                    when (props?.get("units")?.jsonPrimitive?.contentOrNull) {
                        "miles" -> Miles
                        else -> Kilometers
                    }
                val distance =
                    (props?.get("dist")?.jsonPrimitive?.doubleOrNull ?: 100.0).toLength(unit)
                val origin = feature.geometry.coordinates

                val destination = origin.rhumbOffset(distance, bearing)

                assertPositionEquals(
                    expectedPosition,
                    destination,
                    0.0000001,
                    "failed on path $path",
                )
            }
    }
}
