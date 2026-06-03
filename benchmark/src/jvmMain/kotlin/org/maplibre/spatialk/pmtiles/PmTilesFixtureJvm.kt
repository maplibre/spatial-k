package org.maplibre.spatialk.pmtiles

import java.nio.file.Files
import java.nio.file.Path

internal actual fun readPmTilesFixture(path: String): ByteArray {
    val candidates =
        listOf(
            Path.of("pmtiles/src/commonTest/resources/fixtures/$path"),
            Path.of("../pmtiles/src/commonTest/resources/fixtures/$path"),
        )
    val fixturePath = candidates.firstOrNull(Files::exists)
    require(fixturePath != null) { "Missing PMTiles benchmark fixture $path" }
    return Files.readAllBytes(fixturePath)
}
