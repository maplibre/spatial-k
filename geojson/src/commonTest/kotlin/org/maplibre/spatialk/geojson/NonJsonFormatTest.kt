package org.maplibre.spatialk.geojson

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.maplibre.spatialk.geojson.dsl.buildLineString

class NonJsonFormatTest {
    // NOTE: polymorphic types are not yet supported with non-json formats
    // That's Geometry, GeometryCollection, GeoJsonObject, FeatureCollection
    // Also Feature (because of properties: JsonObject) is not yet supported

    fun assertRoundTrip(roundTrip: (LineString) -> LineString) {
        val original = buildLineString {
            add(Position(1.0, 2.0))
            add(Position(2.0, 3.0))
            bbox = BoundingBox(1.0, 2.0, 2.0, 3.0)
        }

        assertEquals(original, roundTrip(original))
    }

    @Test
    fun testProtoBufSerialization() {
        assertRoundTrip { obj ->
            @OptIn(ExperimentalSerializationApi::class)
            ProtoBuf.decodeFromByteArray(ProtoBuf.encodeToByteArray(obj))
        }
    }

    @Test
    @Ignore // TODO
    fun testCborSerialization() {
        assertRoundTrip { obj ->
            @OptIn(ExperimentalSerializationApi::class)
            Cbor.decodeFromByteArray(ProtoBuf.encodeToByteArray(obj))
        }
    }
}
