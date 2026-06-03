package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.maplibre.spatialk.pmtiles.internal.HEADER_BYTES
import org.maplibre.spatialk.pmtiles.internal.MINIMAL_ROOT_DIRECTORY_BYTES
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchiveWithSections
import org.maplibre.spatialk.pmtiles.internal.runSuspending

class MetadataTest {
    @Test
    fun readsRawMetadataAndTypedFieldsOnce() = runSuspending {
        val json =
            """{"name":"Tiles","description":"Desc","attribution":"Attr","type":"baselayer","version":"1","encoding":"mvt","vector_layers":[{"id":"roads"}],"custom":{"x":1}}"""
        val source =
            TestByteRangeSource(buildMetadataArchive(json.encodeToByteArray(), TileType.Mvt))
        val archive = PmTilesArchive.open(source)

        val raw = archive.rawMetadataJson()
        val rawAgain = archive.rawMetadataJson()
        val metadata = archive.metadata()
        val metadataAgain = archive.metadata()

        assertEquals(json, raw)
        assertEquals(json, rawAgain)
        assertEquals("Tiles", metadata.name)
        assertEquals("Desc", metadata.description)
        assertEquals("Attr", metadata.attribution)
        assertEquals(TilesetKind.BaseLayer, metadata.type)
        assertEquals("1", metadata.version)
        assertEquals("mvt", metadata.encoding)
        assertEquals("""[{"id":"roads"}]""", metadata.vectorLayersJson)
        assertEquals(metadata, metadataAgain)
        assertEquals(2, source.reads.size)
    }

    @Test
    fun returnsEmptyTypedMetadataForEmptyMetadataSection() = runSuspending {
        val archive = PmTilesArchive.open(TestByteRangeSource(buildMetadataArchive(ByteArray(0))))

        val metadata = archive.metadata()

        assertEquals("", archive.rawMetadataJson())
        assertNull(metadata.name)
        assertNull(metadata.vectorLayersJson)
    }

    @Test
    fun rejectsInvalidUtf8() {
        val error =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    val archive =
                        PmTilesArchive.open(
                            TestByteRangeSource(
                                buildMetadataArchive(byteArrayOf(0xc3.toByte(), 0x28))
                            )
                        )
                    archive.rawMetadataJson()
                }
            }

        assertEquals(PmTilesErrorCode.InvalidMetadata, error.code)
    }

    @Test
    fun strictModeRejectsNonObjectAndWrongTypedFields() {
        assertMetadataFails(PmTilesErrorCode.InvalidMetadata, "[]")
        assertMetadataFails(PmTilesErrorCode.InvalidMetadata, """{"name":1}""")
    }

    @Test
    fun lenientModePreservesRawJsonForNonObjectMetadata() = runSuspending {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(buildMetadataArchive("[]".encodeToByteArray())),
                options = ArchiveOpenOptions.Lenient,
            )

        val metadata = archive.metadata()

        assertEquals("[]", archive.rawMetadataJson())
        assertNull(metadata.name)
        assertEquals(ArchiveWarningCode.InvalidMetadataRecovered, archive.warningAt(0)?.code)
    }

    @Test
    fun lenientModeDropsWrongTypedFieldsAndKeepsValidFields() = runSuspending {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    buildMetadataArchive("""{"name":1,"description":"ok"}""".encodeToByteArray())
                ),
                options = ArchiveOpenOptions.Lenient,
            )

        val metadata = archive.metadata()

        assertNull(metadata.name)
        assertEquals("ok", metadata.description)
        assertEquals(ArchiveWarningCode.InvalidMetadataRecovered, archive.warningAt(0)?.code)
    }

    @Test
    fun mvtMetadataRequiresVectorLayers() {
        assertMetadataFails(
            PmTilesErrorCode.InvalidMetadata,
            """{"name":"Tiles"}""",
            tileType = TileType.Mvt,
        )
    }

    @Test
    fun lenientMvtMetadataWarnsWhenVectorLayersIsMissing() = runSuspending {
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(
                    buildMetadataArchive(
                        """{"name":"Tiles"}""".encodeToByteArray(),
                        tileType = TileType.Mvt,
                    )
                ),
                options = ArchiveOpenOptions.Lenient,
            )

        val metadata = archive.metadata()

        assertEquals("Tiles", metadata.name)
        assertEquals(ArchiveWarningCode.MissingVectorLayers, archive.warningAt(0)?.code)
    }

    private fun assertMetadataFails(
        code: PmTilesErrorCode,
        json: String,
        tileType: TileType = TileType.Png,
    ) {
        val error =
            assertFailsWith<PmTilesException> {
                runSuspending {
                    val archive =
                        PmTilesArchive.open(
                            TestByteRangeSource(
                                buildMetadataArchive(json.encodeToByteArray(), tileType)
                            )
                        )
                    archive.metadata()
                }
            }

        assertEquals(code, error.code)
    }

    private fun buildMetadataArchive(
        metadataBytes: ByteArray,
        tileType: TileType = TileType.Png,
    ): ByteArray {
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
            tileBytes = byteArrayOf(0),
        )
    }
}
