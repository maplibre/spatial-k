package org.maplibre.spatialk.turf

import kotlin.random.Random
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.dsl.addFeature
import org.maplibre.spatialk.geojson.dsl.buildFeatureCollection
import org.maplibre.spatialk.turf.other.RTree

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class RTreeBenchmark {
    private lateinit var featureCollection: FeatureCollection<Geometry?, JsonObject?>
    private lateinit var rtree16: RTree<Feature<Geometry?, JsonObject?>>
    private lateinit var rtree128: RTree<Feature<Geometry?, JsonObject?>>

    private val random = Random(0)

    private fun generateDataset() = buildFeatureCollection {
        repeat(100000) {
            addFeature(
                geometry =
                    Point(
                        longitude = random.nextDouble(360.0) - 180,
                        latitude = random.nextDouble(180.0) - 90,
                    )
            ) {
                properties = null
            }
        }
    }

    @Setup
    fun setup() {
        featureCollection = generateDataset()
        rtree16 = RTree(featureCollection.features, 16)
        rtree128 = RTree(featureCollection.features, 128)
    }

    @Benchmark
    fun insertion128() {
        val rtree = RTree(featureCollection.features, 128)
        require(rtree.size == featureCollection.features.size)
    }

    @Benchmark
    fun insertion16() {
        val rtree = RTree(featureCollection.features, 16)
        require(rtree.size == featureCollection.features.size)
    }

    @Benchmark
    fun search128() {
        val result = rtree128.search(BoundingBox(Position(10.0, 10.0), Position(20.0, 20.0)))
        require(result.size == 149) { "Wrong number of results (${result.size})" }
    }

    @Benchmark
    fun search16() {
        val result = rtree16.search(BoundingBox(Position(10.0, 10.0), Position(20.0, 20.0)))
        require(result.size == 149) { "Wrong number of results (${result.size})" }
    }
}
