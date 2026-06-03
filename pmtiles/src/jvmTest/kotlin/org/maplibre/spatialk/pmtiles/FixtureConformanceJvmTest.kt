package org.maplibre.spatialk.pmtiles

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test

class FixtureConformanceJvmTest {
    private val fixtures = FixtureConformanceCases(::readFixtureBytes)

    @Test fun opensValidUpstreamFixtures() = fixtures.opensValidUpstreamFixtures()

    @Test fun parsesFixtureMetadata() = fixtures.parsesFixtureMetadata()

    @Test fun decodesGzipMvtTileFixture() = fixtures.decodesGzipMvtTileFixture()

    @Test
    fun readsRasterFixtureThroughLeafDirectories() =
        fixtures.readsRasterFixtureThroughLeafDirectories()

    @Test fun rejectsInvalidUpstreamFixtures() = fixtures.rejectsInvalidUpstreamFixtures()

    @Test
    fun opensPinnedGeneratedGoPmtilesFixture() = fixtures.opensPinnedGeneratedGoPmtilesFixture()

    private fun readFixtureBytes(path: String): ByteArray =
        Files.readAllBytes(Path.of("src/commonTest/resources/fixtures/$path"))
}
