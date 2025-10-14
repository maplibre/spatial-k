package org.maplibre.spatialk.geojson

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.maplibre.spatialk.geojson.dsl.buildLineString

@OptIn(ExperimentalSerializationApi::class)
class NonJsonFormatTest {
    // NOTE: polymorphic types are not yet supported with non-json formats
    // That's Geometry (and related interfaces), GeoJsonObject

    private val testLineString = buildLineString {
        add(Position(1.0, 2.0))
        add(Position(2.0, 3.0))
        bbox = BoundingBox(1.0, 2.0, 2.0, 3.0)
    }

    private val testGeometryCollection = GeometryCollection(testLineString)

    private val testFeatureNullProps = Feature(testLineString, null)

    @Serializable private data class ExampleProps(val x: Double = 1.0, val y: String = "two")

    private val testFeatureClassProps = Feature(testLineString, ExampleProps())

    private val testFeatureCollectionNullProps = FeatureCollection(testFeatureNullProps)
    private val testFeatureCollectionClassProps = FeatureCollection(testFeatureClassProps)

    private inline fun <reified T> assertRoundTrip(format: BinaryFormat, value: T) {
        val encoded = format.encodeToByteArray(value)
        val decoded = format.decodeFromByteArray<T>(encoded)
        assertEquals(value, decoded)
    }

    @Test fun testCborGeometry() = assertRoundTrip(Cbor, testLineString)

    @Test fun testCborGeometryCollection() = assertRoundTrip(Cbor, testGeometryCollection)

    @Test fun testCborFeature() = assertRoundTrip(Cbor, testFeatureNullProps)

    @Test fun testCborFeatureProps() = assertRoundTrip(Cbor, testFeatureClassProps)

    @Test fun testCborFeatureCollection() = assertRoundTrip(Cbor, testFeatureCollectionNullProps)

    @Test
    fun testCborFeatureCollectionProps() = assertRoundTrip(Cbor, testFeatureCollectionClassProps)

    @Test fun testProtobufGeometry() = assertRoundTrip(ProtoBuf, testLineString)

    // Below are disabled because ProtoBuf doesn't support null collection types
    // Every GeoJSON object has a nullable bbox. Our test LineString above works
    // because we set a bbox.

    @Test
    @Ignore
    fun testProtobufGeometryCollection() = assertRoundTrip(ProtoBuf, testGeometryCollection)

    @Test @Ignore fun testProtobufFeature() = assertRoundTrip(ProtoBuf, testFeatureNullProps)

    @Test @Ignore fun testProtobufFeatureProps() = assertRoundTrip(ProtoBuf, testFeatureClassProps)

    @Test
    @Ignore
    fun testProtobufFeatureCollection() = assertRoundTrip(ProtoBuf, testFeatureCollectionNullProps)

    @Test
    @Ignore
    fun testProtobufFeatureCollectionProps() =
        assertRoundTrip(ProtoBuf, testFeatureCollectionClassProps)
}
