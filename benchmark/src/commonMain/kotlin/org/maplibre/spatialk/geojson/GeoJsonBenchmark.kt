package org.maplibre.spatialk.geojson

//
// @State(Scope.Benchmark)
// @BenchmarkMode(Mode.AverageTime)
// @OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
// open class GeoJsonBenchmark {
//    private lateinit var featureCollection: FeatureCollection<Geometry?, JsonObject?>
//    private lateinit var jsonString: String
//    private lateinit var jsonObject: JsonObject
//
//    private val random = Random(0)
//
//    private fun generateDataset() = buildFeatureCollection {
//        repeat(5000) {
//            addFeature(
//                geometry =
//                    Point(
//                        longitude = random.nextDouble(360.0) - 180,
//                        latitude = random.nextDouble(360.0) - 180,
//                    )
//            )
//        }
//
//        repeat(5000) {
//            addFeature(
//                geometry =
//                    buildLineString {
//                        repeat(10) {
//                            add(
//                                longitude = random.nextDouble(360.0) - 180,
//                                latitude = random.nextDouble(360.0) - 180,
//                            )
//                        }
//                    }
//            )
//        }
//
//        repeat(5000) {
//            addFeature(
//                geometry =
//                    buildPolygon {
//                        addRing {
//                            add(
//                                longitude = random.nextDouble(360.0) - 180,
//                                latitude = random.nextDouble(360.0) - 180,
//                                altitude = random.nextDouble(100.0),
//                            )
//                            repeat(8) {
//                                add(
//                                    longitude = random.nextDouble(360.0) - 180,
//                                    latitude = random.nextDouble(360.0) - 180,
//                                    altitude = random.nextDouble(100.0),
//                                )
//                            }
//                        }
//                    }
//            ) {
//                properties = buildJsonObject { put("example", "value") }
//            }
//        }
//    }
//
//    @Setup
//    fun setup() {
//        featureCollection = generateDataset()
//        jsonString = featureCollection.toJson()
//        jsonObject = Json.decodeFromString(jsonString)
//    }
//
//    /** Benchmark serialization using kotlinx.serialization */
//    @Benchmark
//    fun serialization() {
//        featureCollection.toJson()
//    }
//
//    /** Benchmark how fast kotlinx.serialization can encode a GeoJSON structure directly */
//    @Benchmark
//    fun baselineSerialization() {
//        Json.encodeToString(jsonObject)
//    }
//
//    @Benchmark
//    fun deserialization() {
//        FeatureCollection.fromJson<Geometry?, JsonObject?>(jsonString)
//    }
// }
