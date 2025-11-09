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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.dsl.addFeature
import org.maplibre.spatialk.geojson.dsl.addRing
import org.maplibre.spatialk.geojson.dsl.buildFeatureCollection
import org.maplibre.spatialk.geojson.dsl.buildLineString
import org.maplibre.spatialk.geojson.dsl.buildPolygon
import org.maplibre.spatialk.turf.other.RTree

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class RTreeBenchmark {
    private lateinit var featureCollection: FeatureCollection<Geometry?, JsonObject?>
    private lateinit var rtree5: RTree<Feature<Geometry?, JsonObject?>>
    private lateinit var rtree50: RTree<Feature<Geometry?, JsonObject?>>

    private val random = Random(0)

    private fun generateDataset() = buildFeatureCollection {
        repeat(50000) {
            addFeature(
                geometry =
                    Point(
                        longitude = random.nextDouble(360.0) - 180,
                        latitude = random.nextDouble(360.0) - 180,
                    )
            )
        }

        repeat(50000) {
            addFeature(
                geometry =
                    buildLineString {
                        repeat(10) {
                            add(
                                longitude = random.nextDouble(360.0) - 180,
                                latitude = random.nextDouble(360.0) - 180,
                            )
                        }
                    }
            )
        }

        repeat(50000) {
            addFeature(
                geometry =
                    buildPolygon {
                        addRing {
                            add(
                                longitude = random.nextDouble(360.0) - 180,
                                latitude = random.nextDouble(360.0) - 180,
                                altitude = random.nextDouble(100.0),
                            )
                            repeat(8) {
                                add(
                                    longitude = random.nextDouble(360.0) - 180,
                                    latitude = random.nextDouble(360.0) - 180,
                                    altitude = random.nextDouble(100.0),
                                )
                            }
                        }
                    }
            ) {
                properties = buildJsonObject { put("example", "value") }
            }
        }
    }

    @Setup
    fun setup() {
        featureCollection = generateDataset()
        rtree5 = RTree(featureCollection.features, 5)
        rtree50 = RTree(featureCollection.features, 50)
    }

    /** Benchmark insertion with entries=50 */
    @Benchmark
    fun insertionFast() {
        val rtree = RTree(featureCollection.features, 50)
        require(rtree.all().size == featureCollection.features.size)
    }

    /** Benchmark insertion with entries=5 */
    @Benchmark
    fun insertionSlow() {
        val rtree = RTree(featureCollection.features, 5)
        require(rtree.all().size == featureCollection.features.size)
    }

    /** Benchmark search with entries=5 */
    @Benchmark
    fun searchFast() {
        val result = rtree5.search(BoundingBox(Position(10.0, 10.0), Position(20.0, 20.0)))
        require(result.size == 99471)
    }

    /** Benchmark search with entries=50 */
    @Benchmark
    fun searchSlow() {
        val result = rtree50.search(BoundingBox(Position(10.0, 10.0), Position(20.0, 20.0)))
        require(result.size == 99471)
    }
}
