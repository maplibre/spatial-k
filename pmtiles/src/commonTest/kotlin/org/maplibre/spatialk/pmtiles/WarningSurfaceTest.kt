package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.maplibre.spatialk.pmtiles.internal.MINIMAL_ROOT_DIRECTORY_BYTES
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchive
import org.maplibre.spatialk.pmtiles.internal.buildArchiveWithSections
import org.maplibre.spatialk.pmtiles.internal.runSuspending

class WarningSurfaceTest {
    @Test
    fun lenientModeRecordsHeaderWarningsInDeterministicOrder() = runSuspending {
        val fields =
            TestHeaderFields(
                rootOffset = 220uL,
                rootLength = MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong(),
                metadataOffset = 150uL,
                metadataLength = 10uL,
                leafDirectoriesOffset = 170uL,
                leafDirectoriesLength = 10uL,
                tileDataOffset = 200uL,
                tileDataLength = 1uL,
                tileCompression = Compression.Unknown.code,
                tileType = 99u,
            )

        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(buildArchive(fields)),
                options = ArchiveOpenOptions.Lenient,
            )

        assertEquals(
            listOf(
                ArchiveWarningCode.UnknownCount,
                ArchiveWarningCode.UnknownCount,
                ArchiveWarningCode.UnknownCount,
                ArchiveWarningCode.NonCanonicalSectionOrder,
                ArchiveWarningCode.UnknownCompressionCode,
                ArchiveWarningCode.UnknownTileType,
            ),
            archive.warnings().map { it.code },
        )
        assertEquals("addressedTiles", archive.warningAt(0)?.context)
        assertEquals("tileEntries", archive.warningAt(1)?.context)
        assertEquals("tileContents", archive.warningAt(2)?.context)
    }

    @Test
    fun warningAccessorsReturnSnapshotsAndSupportLazyWarnings() = runSuspending {
        val metadataBytes = "[]".encodeToByteArray()
        val fields =
            TestHeaderFields(
                rootLength = MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong(),
                metadataOffset = 200uL,
                metadataLength = metadataBytes.size.toULong(),
                tileDataOffset = 240uL,
                tileDataLength = 1uL,
                addressedTiles = 1uL,
                tileEntries = 1uL,
                tileContents = 1uL,
                tileType = 99u,
            )
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    buildArchiveWithSections(
                        fields = fields,
                        rootBytes = MINIMAL_ROOT_DIRECTORY_BYTES,
                        metadataBytes = metadataBytes,
                    )
                ),
                options = ArchiveOpenOptions.Lenient,
            )
        val snapshot = archive.warnings()

        assertEquals(1, archive.warningCount)
        assertEquals(ArchiveWarningCode.UnknownTileType, snapshot.single().code)
        assertNull(archive.warningAt(-1))
        assertNull(archive.warningAt(1))

        archive.metadata()

        assertEquals(1, snapshot.size)
        assertEquals(2, archive.warningCount)
        assertEquals(ArchiveWarningCode.InvalidMetadataRecovered, archive.warningAt(1)?.code)
    }
}
