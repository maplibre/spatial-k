package org.maplibre.spatialk.geohash

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.maplibre.spatialk.testutil.readResourceFile

/** Vectors from `commonTest/resources`. Provenance is in `resources/README.md`. */
internal object GeohashFixtures {
    val geohashEncode: List<GeohashEncodeCase> = load("geohash/encode.json")
    val geohashDecode: List<GeohashDecodeCase> = load("geohash/decode.json")
    val geohashNeighbors: List<GeohashNeighborCase> = load("geohash/neighbors.json")
    val geohashNeighborSteps: List<GeohashNeighborStepCase> = load("geohash/neighbor_steps.json")
    val osmEncode: List<OsmEncodeCase> = load("osm/encode.json")
    val osmParse: List<OsmParseCase> = load("osm/parse.json")

    private inline fun <reified T> load(path: String): List<T> {
        val value = Json.decodeFromString<List<T>>(readResourceFile(path))
        check(value.isNotEmpty()) { "Fixture $path must not be empty" }
        return value
    }
}

@Serializable
internal data class GeohashEncodeCase(
    val source: String,
    val longitude: Double,
    val latitude: Double,
    val length: Int,
    val text: String,
)

@Serializable
internal data class GeohashDecodeCase(
    val source: String,
    val text: String,
    val longitude: Double,
    val latitude: Double,
    val west: Double? = null,
    val south: Double? = null,
    val east: Double? = null,
    val north: Double? = null,
)

@Serializable
internal data class GeohashNeighborCase(
    val source: String,
    val text: String,
    val north: String,
    val northEast: String,
    val east: String,
    val southEast: String,
    val south: String,
    val southWest: String,
    val west: String,
    val northWest: String,
)

@Serializable
internal data class GeohashNeighborStepCase(
    val source: String,
    val text: String,
    val east: Int,
    val north: Int,
    val expected: String,
)

@Serializable
internal data class OsmEncodeCase(
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val zoom: Int,
    val text: String,
)

@Serializable
internal data class OsmParseCase(
    val source: String,
    val input: String,
    val text: String,
    val zoom: Int? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val west: Double? = null,
    val south: Double? = null,
    val east: Double? = null,
    val north: Double? = null,
)
