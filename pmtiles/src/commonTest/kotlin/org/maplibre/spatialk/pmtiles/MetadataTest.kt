package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import org.maplibre.spatialk.pmtiles.internal.HEADER_BYTES
import org.maplibre.spatialk.pmtiles.internal.MINIMAL_ROOT_DIRECTORY_BYTES
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchiveWithSections

class MetadataTest {
    @Test
    fun readsRawMetadataAndTypedFieldsOnce() = runTest {
        val json =
            """{"name":"Tiles","description":"Desc","attribution":"Attr","type":"baselayer","version":"1","encoding":"mvt","vector_layers":[{"id":"roads"}],"custom":{"x":1}}"""
        val source =
            TestByteRangeSource(buildMetadataArchive(json.encodeToByteString(), TileTypeCodes.Mvt))
        val archive = PmTiles.open(source)

        val raw = archive.rawMetadataJson()
        val rawAgain = archive.rawMetadataJson()
        val metadata = archive.metadata()
        val metadataAgain = archive.metadata()

        assertEquals(json, raw)
        assertEquals(json, rawAgain)
        assertEquals("Tiles", metadata.name)
        assertEquals("Desc", metadata.description)
        assertEquals("Attr", metadata.attribution)
        assertEquals(TilesetKind(KnownTilesetKind.BaseLayer), metadata.type)
        assertEquals("1", metadata.version)
        assertEquals("mvt", metadata.encoding)
        assertEquals("""[{"id":"roads"}]""", metadata.vectorLayersJson)
        assertEquals(metadata, metadataAgain)
        assertEquals(2, source.reads.size)
    }

    @Test
    fun returnsEmptyTypedMetadataForEmptyMetadataSection() = runTest {
        val archive = PmTiles.open(TestByteRangeSource(buildMetadataArchive(ByteString())))

        val metadata = archive.metadata()

        assertEquals("", archive.rawMetadataJson())
        assertNull(metadata.name)
        assertNull(metadata.vectorLayersJson)
    }

    @Test
    fun rejectsInvalidUtf8() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTiles.open(
                        TestByteRangeSource(buildMetadataArchive(ByteString(0xc3.toByte(), 0x28)))
                    )
                archive.rawMetadataJson()
            }

        assertEquals(PmTilesErrorCode.InvalidMetadata, error.code)
    }

    @Test
    fun strictModeRejectsNonObjectAndWrongTypedFields() = runTest {
        assertMetadataFails(PmTilesErrorCode.InvalidMetadata, "[]")
        assertMetadataFails(PmTilesErrorCode.InvalidMetadata, """{"name":1}""")
    }

    @Test
    fun lenientModePreservesRawJsonForNonObjectMetadata() = runTest {
        val archive =
            PmTiles.open(
                TestByteRangeSource(buildMetadataArchive("[]".encodeToByteString())),
                options = ArchiveOpenOptions.build { validationMode = ValidationMode.Lenient },
            )

        val metadata = archive.metadata()

        assertEquals("[]", archive.rawMetadataJson())
        assertNull(metadata.name)
        assertEquals(ArchiveWarningCode.InvalidMetadataRecovered, archive.warnings.single().code)
    }

    @Test
    fun lenientModeDropsWrongTypedFieldsAndKeepsValidFields() = runTest {
        val archive =
            PmTiles.open(
                TestByteRangeSource(
                    buildMetadataArchive("""{"name":1,"description":"ok"}""".encodeToByteString())
                ),
                options = ArchiveOpenOptions.build { validationMode = ValidationMode.Lenient },
            )

        val metadata = archive.metadata()

        assertNull(metadata.name)
        assertEquals("ok", metadata.description)
        assertEquals(ArchiveWarningCode.InvalidMetadataRecovered, archive.warnings.single().code)
    }

    @Test
    fun mvtMetadataRequiresVectorLayers() = runTest {
        assertMetadataFails(
            PmTilesErrorCode.InvalidMetadata,
            """{"name":"Tiles"}""",
            tileType = TileTypeCodes.Mvt,
        )
    }

    @Test
    fun lenientMvtMetadataWarnsWhenVectorLayersIsMissing() = runTest {
        val archive =
            PmTiles.open(
                TestByteRangeSource(
                    buildMetadataArchive(
                        """{"name":"Tiles"}""".encodeToByteString(),
                        tileType = TileTypeCodes.Mvt,
                    )
                ),
                options = ArchiveOpenOptions.build { validationMode = ValidationMode.Lenient },
            )

        val metadata = archive.metadata()

        assertEquals("Tiles", metadata.name)
        assertEquals(ArchiveWarningCode.MissingVectorLayers, archive.warnings.single().code)
    }

    private suspend fun assertMetadataFails(
        code: PmTilesErrorCode,
        json: String,
        tileType: TileTypeCode = TileTypeCodes.Png,
    ) {
        val error =
            assertFailsWith<PmTilesException> {
                val archive =
                    PmTiles.open(
                        TestByteRangeSource(
                            buildMetadataArchive(json.encodeToByteString(), tileType)
                        )
                    )
                archive.metadata()
            }

        assertEquals(code, error.code)
    }

    private fun buildMetadataArchive(
        metadataBytes: ByteString,
        tileType: TileTypeCode = TileTypeCodes.Png,
    ): ByteString {
        val metadataOffset = HEADER_BYTES.toULong() + MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong()
        val fields =
            TestHeaderFields(
                metadataOffset = metadataOffset,
                metadataLength = metadataBytes.size.toULong(),
                tileDataOffset = metadataOffset + metadataBytes.size.toULong(),
                tileDataLength = 1uL,
                addressedTiles = 1uL,
                tileEntries = 1uL,
                tileContents = 1uL,
                tileType = tileType.code,
            )
        return buildArchiveWithSections(
            fields = fields,
            rootBytes = MINIMAL_ROOT_DIRECTORY_BYTES,
            metadataBytes = metadataBytes,
            tileBytes = ByteString(0),
        )
    }
}
