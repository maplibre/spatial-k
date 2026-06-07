package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import org.maplibre.spatialk.pmtiles.ArchiveWriteConfig
import org.maplibre.spatialk.pmtiles.ArchiveWriteLimits
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.Compressor
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException
import org.maplibre.spatialk.pmtiles.TileTypeCodes

class MetadataWriterTest {
    @Test
    fun encodesJsonObjectAsUtf8MetadataBytes() = runTest {
        val json = """{"name":"demo","custom":{"x":1}}"""
        val metadata =
            encodeMetadata(
                config = ArchiveWriteConfig.build { metadataJson = json },
                options = ArchiveWriteOptions(),
            )

        assertEquals(json.encodeToByteString(), metadata.rawBytes)
        assertEquals(json.encodeToByteString(), metadata.compressedBytes)
    }

    @Test
    fun rejectsMalformedMetadataJson() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                encodeMetadata(
                    config = ArchiveWriteConfig.build { metadataJson = "{" },
                    options = ArchiveWriteOptions(),
                )
            }

        assertEquals(PmTilesErrorCode.InvalidMetadata, error.code)
    }

    @Test
    fun rejectsMetadataThatIsNotJsonObject() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                encodeMetadata(
                    config = ArchiveWriteConfig.build { metadataJson = "[]" },
                    options = ArchiveWriteOptions(),
                )
            }

        assertEquals(PmTilesErrorCode.InvalidMetadata, error.code)
    }

    @Test
    fun mvtMetadataRequiresVectorLayersArray() = runTest {
        listOf("""{"name":"demo"}""", """{"vector_layers":{}}""").forEach { json ->
            val error =
                assertFailsWith<PmTilesException> {
                    encodeMetadata(
                        config =
                            ArchiveWriteConfig.build {
                                tileType = TileTypeCodes.Mvt
                                metadataJson = json
                            },
                        options = ArchiveWriteOptions(),
                    )
                }

            assertEquals(PmTilesErrorCode.InvalidMetadata, error.code)
        }
    }

    @Test
    fun acceptsMvtMetadataWithVectorLayersArray() = runTest {
        val json = """{"vector_layers":[{"id":"roads"}]}"""
        val metadata =
            encodeMetadata(
                config =
                    ArchiveWriteConfig.build {
                        tileType = TileTypeCodes.Mvt
                        metadataJson = json
                    },
                options = ArchiveWriteOptions(),
            )

        assertEquals(json.encodeToByteString(), metadata.rawBytes)
    }

    @Test
    fun enforcesMetadataLimitsBeforeCompression() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                encodeMetadata(
                    config = ArchiveWriteConfig.build { metadataJson = """{"name":"demo"}""" },
                    options =
                        ArchiveWriteOptions.build {
                            limits = ArchiveWriteLimits.build { maxMetadataBytes = 4uL }
                        },
                )
            }

        assertEquals(PmTilesErrorCode.LimitExceeded, error.code)
    }

    @Test
    fun compressesMetadataWithRegisteredCompressor() = runTest {
        val compressed = ByteString(9, 8, 7)
        val metadata =
            encodeMetadata(
                config = ArchiveWriteConfig(),
                options =
                    ArchiveWriteOptions.build {
                        internalCompression = CompressionCodes.Brotli
                        compressor(CompressionCodes.Brotli, Compressor { _, _ -> compressed })
                    },
            )

        assertEquals("{}".encodeToByteString(), metadata.rawBytes)
        assertEquals(compressed, metadata.compressedBytes)
    }

    @Test
    fun rejectsUnsupportedInternalCompression() = runTest {
        val error =
            assertFailsWith<PmTilesException> {
                encodeMetadata(
                    config = ArchiveWriteConfig(),
                    options =
                        ArchiveWriteOptions.build {
                            internalCompression = CompressionCodes.Brotli
                        },
                )
            }

        assertEquals(PmTilesErrorCode.UnsupportedCompression, error.code)
    }
}
