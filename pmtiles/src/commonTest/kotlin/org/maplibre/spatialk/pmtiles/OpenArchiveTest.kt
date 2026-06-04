package org.maplibre.spatialk.pmtiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.pmtiles.internal.FIRST_READ_BYTES
import org.maplibre.spatialk.pmtiles.internal.HEADER_BYTES
import org.maplibre.spatialk.pmtiles.internal.MINIMAL_ROOT_DIRECTORY_BYTES
import org.maplibre.spatialk.pmtiles.internal.TestByteRangeSource
import org.maplibre.spatialk.pmtiles.internal.TestHeaderFields
import org.maplibre.spatialk.pmtiles.internal.buildArchive
import org.maplibre.spatialk.pmtiles.internal.buildHeader
import org.maplibre.spatialk.pmtiles.internal.parseHeader
import org.maplibre.spatialk.pmtiles.internal.readSourceRange

class OpenArchiveTest {
    @Test
    fun opensValidMinimalHeader() = runTest {
        val source = TestByteRangeSource(buildArchive())
        val archive = PmTilesArchive.open(source)

        assertEquals(3, archive.header.specVersion)
        assertEquals(
            ArchiveSection(
                HEADER_BYTES.toULong(),
                MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong(),
            ),
            archive.header.rootDirectory,
        )
        assertEquals(Compression.None, archive.internalCompression)
        assertEquals(TileType.Unknown, archive.tileType)
        assertEquals(1, source.reads.size)
        assertEquals(
            ByteRange(0uL, HEADER_BYTES + MINIMAL_ROOT_DIRECTORY_BYTES.size + 1),
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
                internalCompression = Compression.None.code,
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
        val archive = PmTilesArchive.open(TestByteRangeSource(buildArchive(fields)))
        val header = archive.header

        assertEquals(0, archive.warningCount)
        assertEquals(
            ArchiveSection(127uL, MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong()),
            header.rootDirectory,
        )
        assertEquals(ArchiveSection(200uL, 10uL), header.metadata)
        assertEquals(ArchiveSection(300uL, 20uL), header.leafDirectories)
        assertEquals(ArchiveSection(500uL, 30uL), header.tileData)
        assertNull(header.counts.addressedTiles)
        assertEquals(2uL, header.counts.tileEntries)
        assertEquals(3uL, header.counts.tileContents)
        assertEquals(Clustered.Yes, header.clustered)
        assertEquals(Compression(99u), header.tileCompression)
        assertEquals(TileType(99u), header.tileType)
        assertEquals(4, header.minZoom)
        assertEquals(8, header.maxZoom)
        assertEquals(LonLatBounds(-10.0, -20.0, 30.0, 40.0), header.bounds)
        assertEquals(TileCenter(12.5, -7.25, 6), header.center)
    }

    @Test
    fun rejectsInvalidMagicVersionAndShortHeaders() = runTest {
        val invalidMagic = buildArchive().also { it[0] = 0 }
        assertOpenFails(PmTilesErrorCode.InvalidMagic, invalidMagic)

        val invalidVersion = buildArchive().also { it[7] = 2 }
        assertOpenFails(PmTilesErrorCode.UnsupportedVersion, invalidVersion)

        val error =
            assertFailsWith<PmTilesException> {
                parseHeader(buildHeader().copyOf(HEADER_BYTES - 1), HEADER_BYTES.toULong())
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
                parseHeader(buildHeader(overflowFields), ULong.MAX_VALUE)
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

        val archive = PmTilesArchive.open(TestByteRangeSource(buildArchive(fields)))

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
        val fields = TestHeaderFields(internalCompression = Compression.Brotli.code)

        assertOpenFails(PmTilesErrorCode.UnsupportedCompression, buildArchive(fields))
    }

    @Test
    fun customDecompressorDecodesInternalSectionsAtOpen() = runTest {
        val fields = TestHeaderFields(internalCompression = Compression.Brotli.code)
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(buildArchive(fields)),
                options =
                    ArchiveOpenOptions()
                        .withDecompressor(
                            Compression.Brotli,
                            Decompressor { bytes, _ -> bytes },
                        ),
            )

        assertEquals(Compression.Brotli, archive.internalCompression)
        assertEquals(
            ArchiveSection(HEADER_BYTES.toULong(), MINIMAL_ROOT_DIRECTORY_BYTES.size.toULong()),
            archive.header.rootDirectory,
        )
    }

    @Test
    fun rejectsEmptyRootDirectoryUnlessLenient() = runTest {
        val rootBytes = byteArrayOf(0)
        val fields =
            TestHeaderFields(
                rootLength = rootBytes.size.toULong(),
                tileDataLength = 0uL,
            )
        val archiveBytes = buildArchive(fields, rootBytes = rootBytes)

        val strictError =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(TestByteRangeSource(archiveBytes))
            }
        val archive =
            PmTilesArchive.open(
                TestByteRangeSource(archiveBytes),
                options = ArchiveOpenOptions.Lenient,
            )

        assertEquals(PmTilesErrorCode.InvalidDirectory, strictError.code)
        assertTrue(
            archive.warnings().any { it.code == ArchiveWarningCode.EmptyRootDirectory },
            "Expected EmptyRootDirectory warning.",
        )
        assertNull(archive.getTileRange(0, 0, 0))
    }

    @Test
    fun wrapsAndPreservesSourceErrors() = runTest {
        val sourceError = PmTilesException(PmTilesErrorCode.SourceChanged, "changed")
        val sizePreserved =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(TestByteRangeSource(buildArchive(), sizeError = sourceError))
            }
        assertEquals(PmTilesErrorCode.SourceChanged, sizePreserved.code)

        val readPreserved =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(TestByteRangeSource(buildArchive(), readError = sourceError))
            }
        assertEquals(PmTilesErrorCode.SourceChanged, readPreserved.code)

        val sizeWrapped =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(
                    TestByteRangeSource(buildArchive(), sizeError = IllegalStateException())
                )
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, sizeWrapped.code)

        val readWrapped =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(
                    TestByteRangeSource(buildArchive(), readError = IllegalStateException())
                )
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, readWrapped.code)
    }

    @Test
    fun rejectsShortAndOutOfBoundsSourceReads() = runTest {
        val shortRead =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(TestByteRangeSource(buildArchive(), shortRead = true))
            }
        assertEquals(PmTilesErrorCode.SourceUnavailable, shortRead.code)

        val outOfBounds =
            assertFailsWith<PmTilesException> {
                TestByteRangeSource(ByteArray(4))
                    .readSourceRange(
                        ByteRange(3uL, 2),
                        archiveSize = 4uL,
                        maxBytes = 2,
                    )
            }
        assertEquals(PmTilesErrorCode.RangeOutOfBounds, outOfBounds.code)
    }

    @Test
    fun supportsZeroLengthSourceReads() = runTest {
        val bytes =
            TestByteRangeSource(ByteArray(4))
                .readSourceRange(
                    ByteRange(0uL, 0),
                    archiveSize = 4uL,
                    maxBytes = 0,
                )

        assertEquals(0, bytes.size)
    }

    private suspend fun assertOpenFails(code: PmTilesErrorCode, bytes: ByteArray) {
        val error =
            assertFailsWith<PmTilesException> {
                PmTilesArchive.open(TestByteRangeSource(bytes))
            }

        assertEquals(code, error.code)
    }
}
