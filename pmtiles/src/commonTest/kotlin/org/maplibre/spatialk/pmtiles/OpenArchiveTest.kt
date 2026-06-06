package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.internal.FIRST_READ_BYTES
import org.maplibre.spatialk.pmtiles.internal.HEADER_BYTES
import org.maplibre.spatialk.pmtiles.internal.MINIMAL_ROOT_DIRECTORY_BYTES
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchive
import org.maplibre.spatialk.pmtiles.internal.buildHeader
import org.maplibre.spatialk.pmtiles.internal.parseHeader
import org.maplibre.spatialk.pmtiles.internal.readSourceRange
import org.maplibre.spatialk.testutil.assertDoubleEquals

class OpenArchiveTest {
    @Test
    fun opensValidMinimalHeader() = runTest {
        val source = TestByteRangeSource(buildArchive())
        val archive = PmTiles.open(source)

        assertEquals(3, archive.header.specVersion)
        assertEquals(
            ArchiveSection(
                HEADER_BYTES.toULong(),
                MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong(),
            ),
            archive.header.rootDirectory,
        )
        assertEquals(CompressionCodes.None, archive.internalCompression)
        assertEquals(TileTypeCodes.Unknown, archive.tileType)
        assertEquals(1, source.reads.size)
        assertEquals(
            ByteRange(0uL, (HEADER_BYTES + MINIMAL_ROOT_DIRECTORY_BYTES.size + 1).toULong()),
            source.reads.single(),
        )
    }

    @Test
    fun parsesEveryHeaderFieldAndRawCode() = runTest {
        val fields =
            TestHeaderFields(
                rootOffset = 127uL,
                rootLength = MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong(),
                metadataOffset = 200uL,
                metadataLength = 10uL,
                leafDirectoriesOffset = 300uL,
                leafDirectoriesLength = 20uL,
                tileDataOffset = 500uL,
                tileDataLength = 30uL,
                addressedTiles = 0uL,
                tileEntries = 2uL,
                tileContents = 3uL,
                clustered = 1u,
                internalCompression = CompressionCodes.None.code,
                tileCompression = 99u,
                tileType = 99u,
                minZoom = 4u,
                maxZoom = 8u,
                minLongitude = -10.0,
                minLatitude = -20.0,
                maxLongitude = 30.0,
                maxLatitude = 40.0,
                centerZoom = 6u,
                centerLongitude = 12.5,
                centerLatitude = -7.25,
            )
        val archive = PmTiles.open(TestByteRangeSource(buildArchive(fields)))
        val header = archive.header

        assertEquals(0, archive.warnings.size)
        assertEquals(
            ArchiveSection(127uL, MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong()),
            header.rootDirectory,
        )
        assertEquals(ArchiveSection(200uL, 10uL), header.metadata)
        assertEquals(ArchiveSection(300uL, 20uL), header.leafDirectories)
        assertEquals(ArchiveSection(500uL, 30uL), header.tileData)
        assertEquals(0uL, header.counts.addressedTiles)
        assertEquals(2uL, header.counts.tileEntries)
        assertEquals(3uL, header.counts.tileContents)
        assertEquals(true, header.isClustered)
        assertEquals(CompressionCode(99u), header.tileCompression)
        assertEquals(TileTypeCode(99u), header.tileType)
        assertEquals(4, header.minZoom)
        assertEquals(8, header.maxZoom)

        val bounds = header.bounds
        assertDoubleEquals(-10.0, bounds.west)
        assertDoubleEquals(-20.0, bounds.south)
        assertDoubleEquals(30.0, bounds.east)
        assertDoubleEquals(40.0, bounds.north)

        val center = header.center
        assertDoubleEquals(12.5, center.longitude)
        assertDoubleEquals(-7.25, center.latitude)
        assertEquals(6, center.zoom)
    }

    @Test
    fun rejectsInvalidMagicVersionAndShortHeaders() = runTest {
        val invalidMagic = buildArchive().mutableCopy().also { it[0] = 0 }.let(::ByteString)
        assertOpenFails(PmTilesErrorCode.InvalidMagic, invalidMagic)

        val invalidVersion = buildArchive().mutableCopy().also { it[7] = 2 }.let(::ByteString)
        assertOpenFails(PmTilesErrorCode.UnsupportedVersion, invalidVersion)

        val error =
            assertFailsWith<PmTilesException> {
                parseHeader(
                    ByteString(buildHeader().copyOf(HEADER_BYTES - 1)),
                    HEADER_BYTES.toULong(),
                )
            }
        assertEquals(PmTilesErrorCode.InvalidHeader, error.code)
    }

    @Test
    fun rejectsInvalidSectionLayouts() = runTest {
        val overflowFields =
            TestHeaderFields(
                metadataOffset = ULong.MAX_VALUE,
                metadataLength = 1uL,
            )
        val overflow =
            assertFailsWith<PmTilesException> {
                parseHeader(ByteString(buildHeader(overflowFields)), ULong.MAX_VALUE)
            }
        assertEquals(PmTilesErrorCode.InvalidSectionLayout, overflow.code)

        val outOfBounds =
            TestHeaderFields(
                metadataOffset = 200uL,
                metadataLength = 1uL,
            )
        assertOpenFails(
            PmTilesErrorCode.InvalidSectionLayout,
            buildArchive(outOfBounds, archiveSize = HEADER_BYTES + 1),
        )
    }

    @Test
    fun acceptsLegalNonCanonicalSectionOrder() = runTest {
        val fields =
            TestHeaderFields(
                leafDirectoriesOffset = 150uL,
                leafDirectoriesLength = 10uL,
                metadataOffset = 200uL,
                metadataLength = 10uL,
                tileDataOffset = 250uL,
                tileDataLength = 10uL,
                rootOffset = 300uL,
                rootLength = MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong(),
            )

        val archive = PmTiles.open(TestByteRangeSource(buildArchive(fields)))

        assertEquals(
            ArchiveSection(300uL, MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong()),
            archive.header.rootDirectory,
        )
    }

    @Test
    fun rejectsRootOutsideFirstRead() = runTest {
        val fields = TestHeaderFields(rootOffset = FIRST_READ_BYTES.toULong())

        assertOpenFails(
            PmTilesErrorCode.InvalidRootDirectoryLocation,
            buildArchive(
                fields,
                archiveSize = FIRST_READ_BYTES + MINIMAL_ROOT_DIRECTORY_BYTES.size,
            ),
        )
    }

    @Test
    fun rejectsUnsupportedInternalCompressionAtOpen() = runTest {
        val fields = TestHeaderFields(internalCompression = CompressionCodes.Brotli.code)

        assertOpenFails(PmTilesErrorCode.UnsupportedCompression, buildArchive(fields))
    }

    @Test
    fun customDecompressorDecodesInternalSectionsAtOpen() = runTest {
        val fields = TestHeaderFields(internalCompression = CompressionCodes.Brotli.code)
        val archive =
            PmTiles.open(
                TestByteRangeSource(buildArchive(fields)),
                options =
                    ArchiveOpenOptions.build {
                        decompressor(CompressionCodes.Brotli) { bytes, _ -> bytes }
                    },
            )

        assertEquals(CompressionCodes.Brotli, archive.internalCompression)
        assertEquals(
            ArchiveSection(HEADER_BYTES.toULong(), MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong()),
            archive.header.rootDirectory,
        )
    }

    @Test
    fun rejectsEmptyRootDirectoryUnlessLenient() = runTest {
        val rootBytes = ByteString(0)
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                tileDataLength = 0uL,
            )
        val archiveBytes = buildArchive(fields, rootBytes = rootBytes)

        val strictError =
            assertFailsWith<PmTilesException> {
                PmTiles.open(TestByteRangeSource(archiveBytes))
            }
        val archive =
            PmTiles.open(
                TestByteRangeSource(archiveBytes),
                options = ArchiveOpenOptions.build { validationMode = ValidationMode.Lenient },
            )

        assertEquals(PmTilesErrorCode.InvalidDirectory, strictError.code)
        assertTrue(
            archive.warnings.any { it.code == ArchiveWarningCode.EmptyRootDirectory },
            "Expected EmptyRootDirectory warning.",
        )
        assertNull(archive.findTileRange(0, 0, 0))
    }

    @Test
    fun wrapsAndPreservesSourceErrors() = runTest {
        val sourceError = PmTilesException(PmTilesErrorCode.SourceUnavailable, "unavailable")
        val sizePreserved =
            assertFailsWith<PmTilesException> {
                PmTiles.open(TestByteRangeSource(buildArchive(), sizeError = sourceError))
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, sizePreserved.code)

        val readPreserved =
            assertFailsWith<PmTilesException> {
                PmTiles.open(TestByteRangeSource(buildArchive(), readError = sourceError))
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, readPreserved.code)

        val sizeWrapped =
            assertFailsWith<PmTilesException> {
                PmTiles.open(
                    TestByteRangeSource(buildArchive(), sizeError = IllegalStateException())
                )
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, sizeWrapped.code)

        val readWrapped =
            assertFailsWith<PmTilesException> {
                PmTiles.open(
                    TestByteRangeSource(buildArchive(), readError = IllegalStateException())
                )
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, readWrapped.code)
    }

    @Test
    fun rejectsShortAndOutOfBoundsSourceReads() = runTest {
        val shortRead =
            assertFailsWith<PmTilesException> {
                PmTiles.open(TestByteRangeSource(buildArchive(), shortRead = true))
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, shortRead.code)

        val outOfBounds =
            assertFailsWith<PmTilesException> {
                TestByteRangeSource(ByteString(0, 0, 0, 0))
                    .readSourceRange(
                        ByteRange(3uL, 2uL),
                        archiveSize = 4uL,
                        maxBytes = 2uL,
                    )
            }
        assertEquals(PmTilesErrorCode.RangeOutOfBounds, outOfBounds.code)
    }

    @Test
    fun supportsZeroLengthSourceReads() = runTest {
        val bytes =
            TestByteRangeSource(ByteString(0, 0, 0, 0))
                .readSourceRange(
                    ByteRange(0uL, 0uL),
                    archiveSize = 4uL,
                    maxBytes = 0uL,
                )

        assertEquals(0, bytes.size)
    }

    private suspend fun assertOpenFails(code: PmTilesErrorCode, bytes: ByteString) {
        val error =
            assertFailsWith<PmTilesException> {
                PmTiles.open(TestByteRangeSource(bytes))
            }

        assertEquals(code, error.code)
    }

    private fun ByteString.mutableCopy(): ByteArray = toByteArray()
}
