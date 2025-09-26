package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.maplibre.spatialk.geojson.serialization.GeoJson

class ObjectTest {
    @Test
    fun deserializePoint() {
        val feature: GeoJsonObject =
            GeoJson.decodeFromString(
                """
                        {
                            "type": "Feature",
                            "geometry": {
                                "type": "Point",
                                "coordinates": [1.0, 2.0]
                            }
                        }
                    """
            )

        assertTrue(feature is Feature)
        assertTrue(feature.geometry is Point)
        assertEquals(Position(1.0, 2.0), feature.geometry.coordinates)
    }
}
