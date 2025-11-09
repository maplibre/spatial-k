package org.maplibre.spatialk.geojson

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
        repeat(5000000) {
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
        rtree16 = RTree(featureCollection.features, 32)
        rtree128 = RTree(featureCollection.features, 128)
    }

    /** Benchmark insertion with entries=50 */
    @Benchmark
    fun insertionFast() {
        val rtree = RTree(featureCollection.features, 128)
        require(rtree.all().size == featureCollection.features.size)
    }

    /** Benchmark insertion with entries=5 */
    @Benchmark
    fun insertionSlow() {
        val rtree = RTree(featureCollection.features, 32)
        require(rtree.all().size == featureCollection.features.size)
    }

    /** Benchmark search with entries=5 */
    @Benchmark
    fun searchFast() {
        val result = rtree16.search(BoundingBox(Position(10.0, 10.0), Position(20.0, 20.0)))
        require(result.size == 7665) { "Wrong number of results (${result.size})" }
    }

    /** Benchmark search with entries=50 */
    @Benchmark
    fun searchSlow() {
        val result = rtree128.search(BoundingBox(Position(10.0, 10.0), Position(20.0, 20.0)))
        require(result.size == 7665) { "Wrong number of results (${result.size})" }
    }
}
