package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.io.bytestring.hexToByteString
import org.maplibre.spatialk.pmtiles.ArchiveHeader
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionCodes
import org.maplibre.spatialk.pmtiles.HeaderCounts
import org.maplibre.spatialk.pmtiles.LonLatBounds
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException
import org.maplibre.spatialk.pmtiles.TileCenter
import org.maplibre.spatialk.pmtiles.TileTypeCode
import org.maplibre.spatialk.testutil.assertDoubleEquals

class HeaderWriterTest {
    @Test
    fun encodesHeaderFields() {
        val header = testHeader()
        val bytes = encodeHeader(header, archiveSize = TEST_ARCHIVE_SIZE)

        assertEquals(HEADER_BYTES, bytes.size)
        assertEquals("504d54696c657303".hexToByteString(), bytes.substring(0, 8))
        assertEquals("7f00000000000000".hexToByteString(), bytes.substring(8, 16))
        assertEquals("0a00000000000000".hexToByteString(), bytes.substring(32, 40))
        assertEquals("0100000000000000".hexToByteString(), bytes.substring(72, 80))
        assertEquals(1.toByte(), bytes[96])
        assertEquals(CompressionCodes.None.code.toByte(), bytes[97])
        assertEquals(CompressionCode(99u).code.toByte(), bytes[98])
        assertEquals(TileTypeCode(98u).code.toByte(), bytes[99])
        assertEquals(4.toByte(), bytes[100])
        assertEquals(8.toByte(), bytes[101])
        assertEquals("001f0afa 003e14f4".compactHexToByteString(), bytes.substring(102, 110))
        assertEquals("00a3e111 0084d717".compactHexToByteString(), bytes.substring(110, 118))
        assertEquals(6.toByte(), bytes[118])
        assertEquals("40597307 e0bcadfb".compactHexToByteString(), bytes.substring(119, 127))
    }

    @Test
    fun parsedHeaderMatchesEncodedInput() {
        val header = testHeader()
        val parsed = parseHeader(encodeHeader(header, TEST_ARCHIVE_SIZE), TEST_ARCHIVE_SIZE)

        assertEquals(header.rootDirectory, parsed.rootDirectory)
        assertEquals(header.metadata, parsed.metadata)
        assertEquals(header.leafDirectories, parsed.leafDirectories)
        assertEquals(header.tileData, parsed.tileData)
        assertEquals(header.counts, parsed.counts)
        assertEquals(header.isClustered, parsed.isClustered)
        assertEquals(header.internalCompression, parsed.internalCompression)
        assertEquals(header.tileCompression, parsed.tileCompression)
        assertEquals(header.tileType, parsed.tileType)
        assertEquals(header.minZoom, parsed.minZoom)
        assertEquals(header.maxZoom, parsed.maxZoom)
        assertDoubleEquals(header.bounds.west, parsed.bounds.west)
        assertDoubleEquals(header.bounds.south, parsed.bounds.south)
        assertDoubleEquals(header.bounds.east, parsed.bounds.east)
        assertDoubleEquals(header.bounds.north, parsed.bounds.north)
        assertDoubleEquals(header.center.longitude, parsed.center.longitude)
        assertDoubleEquals(header.center.latitude, parsed.center.latitude)
        assertEquals(header.center.zoom, parsed.center.zoom)
    }

    @Test
    fun rejectsInvalidHeaderBeforeEncoding() {
        val error =
            assertFailsWith<PmTilesException> {
                encodeHeader(
                    testHeader(rootDirectory = ArchiveSection(offset = 16_383uL, length = 2uL)),
                    archiveSize = 20_000uL,
                )
            }

        assertEquals(PmTilesErrorCodes.InvalidRootDirectoryLocation, error.code)
    }

    @Test
    fun rejectsCodesThatDoNotFitOneByteFields() {
        assertFailsWith<IllegalArgumentException> {
            encodeHeader(testHeader(tileCompression = CompressionCode(256u)), TEST_ARCHIVE_SIZE)
        }
    }
}

private fun testHeader(
    rootDirectory: ArchiveSection = ArchiveSection(offset = 127uL, length = 10uL),
    tileCompression: CompressionCode = CompressionCode(99u),
): ArchiveHeader =
    ArchiveHeader(
        specVersion = 3,
        rootDirectory = rootDirectory,
        metadata = ArchiveSection(offset = 200uL, length = 10uL),
        leafDirectories = ArchiveSection(offset = 300uL, length = 20uL),
        tileData = ArchiveSection(offset = 500uL, length = 30uL),
        counts = HeaderCounts(addressedTiles = 1uL, tileEntries = 2uL, tileContents = 3uL),
        isClustered = true,
        internalCompression = CompressionCodes.None,
        tileCompression = tileCompression,
        tileType = TileTypeCode(98u),
        minZoom = 4,
        maxZoom = 8,
        bounds = LonLatBounds(west = -10.0, south = -20.0, east = 30.0, north = 40.0),
        center = TileCenter(longitude = 12.5, latitude = -7.25, zoom = 6),
    )

private fun String.compactHexToByteString(): kotlinx.io.bytestring.ByteString =
    filterNot { it.isWhitespace() }.lowercase().hexToByteString()

private const val TEST_ARCHIVE_SIZE = 530uL
