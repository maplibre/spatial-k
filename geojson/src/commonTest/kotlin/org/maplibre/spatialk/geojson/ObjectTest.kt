package org.maplibre.spatialk.geojson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObjectTest {
    @Test
    fun deserializePoint() {
        val feature: GeoJsonObject =
            GeoJsonObject.fromJson(
                """
                        {
                            "type": "Feature",
                            "geometry": {
                                "type": "Point",
                                "coordinates": [1.0, 2.0]
                            },
                            "properties": null
                        }
                    """
            )

        assertTrue(feature is Feature<*>)
        assertTrue(feature.geometry is Point)
        assertEquals(Position(1.0, 2.0), feature.geometry.coordinates)
    }
}
