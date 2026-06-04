package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.internal.MINIMAL_ROOT_DIRECTORY_BYTES
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchiveWithSections

class WarningSurfaceTest {
    @Test
    fun warningAccessorsReturnSnapshotsAndSupportLazyWarnings() = runTest {
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
