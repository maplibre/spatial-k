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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.dsl.featureCollection
import org.maplibre.spatialk.geojson.dsl.lineString
import org.maplibre.spatialk.geojson.dsl.point
import org.maplibre.spatialk.geojson.dsl.polygon
import org.maplibre.spatialk.geojson.serialization.GeoJson

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class GeoJsonBenchmark {
    private lateinit var featureCollection: FeatureCollection
    private lateinit var jsonString: String
    private lateinit var jsonObject: JsonObject

    private fun generateDataset(): FeatureCollection {
        val random = Random(0)
        return featureCollection {
            repeat(5000) {
                feature(
                    geometry = point(random.nextDouble(360.0) - 180, random.nextDouble(360.0) - 180)
                )
            }

            repeat(5000) {
                feature(
                    geometry =
                        lineString {
                            repeat(10) {
                                +Position(
                                    random.nextDouble(360.0) - 180,
                                    random.nextDouble(360.0) - 180,
                                )
                            }
                        }
                )
            }

            repeat(5000) {
                feature(
                    geometry =
                        polygon {
                            ring {
                                val start =
                                    Position(
                                        random.nextDouble(360.0) - 180,
                                        random.nextDouble(360.0) - 180,
                                        random.nextDouble(100.0),
                                    )
                                +start
                                repeat(8) {
                                    +Position(
                                        random.nextDouble(360.0) - 180,
                                        random.nextDouble(360.0) - 180,
                                        random.nextDouble(100.0),
                                    )
                                }
                                +start
                            }
                        }
                ) {
                    put("example", "value")
                }
            }
        }
    }

    @Setup
    fun setup() {
        featureCollection = generateDataset()
        jsonString = featureCollection.json()
        jsonObject = GeoJson.decodeFromString(jsonString)
    }

    /** Benchmark serialization using kotlinx.serialization */
    @Benchmark
    fun kotlinxSerialization() {
        featureCollection.json()
    }

    /** Benchmark how fast kotlinx.serialization can encode a GeoJSON structure directly */
    @Benchmark
    fun baselineSerialization() {
        Json.encodeToString(jsonObject)
    }

    @Benchmark
    fun deserialization() {
        FeatureCollection.fromJson(jsonString)
    }
}
