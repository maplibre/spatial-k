package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo

class FixtureConformanceAppleTest {
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

    private fun readFixtureBytes(path: String): ByteArray {
        val candidates =
            listOfNotNull(
                "src/commonTest/resources/fixtures/$path",
                projectDir()?.let { "$it/src/commonTest/resources/fixtures/$path" },
            )
        val data =
            candidates.firstNotNullOfOrNull { fixturePath ->
                NSFileManager.defaultManager.contentsAtPath(fixturePath)
            } ?: error("Missing fixture $path; tried ${candidates.joinToString()}")
        return data.toByteArray()
    }

    private fun projectDir(): String? =
        NSProcessInfo.processInfo.environment["PMTILES_PROJECT_DIR"] as? String
}
