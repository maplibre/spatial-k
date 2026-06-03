package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import org.maplibre.spatialk.testutil.readResourceBytes

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

    private fun readFixtureBytes(path: String): ByteArray = readResourceBytes("fixtures/$path")
}
