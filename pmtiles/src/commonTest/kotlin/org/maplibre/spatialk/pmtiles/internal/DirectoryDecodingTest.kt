@file:OptIn(ExperimentalUnsignedTypes::class)

package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.io.bytestring.buildByteString
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException

class DirectoryDecodingTest {
    @Test
    fun decodesExplicitOffsets() {
        val entries =
            decodeDirectory(
                encodeDirectory(
                    DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                    DirectoryEntry(tileId = 5, offset = 10uL, length = 3, runLength = 1),
                ),
                header(tileDataLength = 20uL),
                limits = defaultLimits,
            )

        assertEquals(
            listOf(
                DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                DirectoryEntry(tileId = 5, offset = 10uL, length = 3, runLength = 1),
            ),
            entries,
        )
    }

    @Test
    fun decodesContiguousOffsetShorthand() {
        val entries =
            decodeDirectory(
                encodeDirectory(
                    DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1),
                    DirectoryEntry(tileId = 1, offset = 2uL, length = 3, runLength = 1),
                ),
                header(tileDataLength = 5uL),
                limits = defaultLimits,
            )

        assertEquals(2uL, entries[1].offset)
    }

    @Test
    fun decodesRunLengthAndLeafEntries() {
        val entries =
            decodeDirectory(
                encodeDirectory(
                    DirectoryEntry(tileId = 5, offset = 0uL, length = 4, runLength = 3),
                    DirectoryEntry(tileId = 10, offset = 0uL, length = 6, runLength = 0),
                ),
                header(tileDataLength = 4uL, leafDirectoriesLength = 6uL),
                limits = defaultLimits,
            )

        assertEquals(true, entries[0].isTile)
        assertEquals(3, entries[0].runLength)
        assertEquals(true, entries[1].isLeaf)
        assertEquals(0, entries[1].runLength)
    }

    @Test
    fun rejectsMalformedDirectories() {
        assertDirectoryFails(PmTilesErrorCodes.InvalidDirectory, bytes(0uL))
        assertDirectoryFails(PmTilesErrorCodes.InvalidDirectory, bytes(1uL, 0uL, 1uL, 0uL, 1uL))
        assertDirectoryFails(PmTilesErrorCodes.InvalidDirectory, bytes(1uL, 0uL, 1uL, 1uL, 0uL))
        assertDirectoryFails(PmTilesErrorCodes.InvalidDirectory, bytes(2uL, 5uL, 0uL))
    }

    @Test
    fun rejectsLimitAndRangeFailures() {
        val tooManyEntries =
            assertFailsWith<PmTilesException> {
                decodeDirectory(
                    encodeDirectory(
                        DirectoryEntry(tileId = 0, offset = 0uL, length = 1, runLength = 1)
                    ),
                    header(),
                    limits =
                        defaultLimits
                            .toBuilder()
                            .apply { maxDirectoryDecompressedBytes = 16uL }
                            .build(),
                )
            }
        assertEquals(PmTilesErrorCodes.LimitExceeded, tooManyEntries.code)

        val tooLargeTile =
            assertFailsWith<PmTilesException> {
                decodeDirectory(
                    encodeDirectory(
                        DirectoryEntry(tileId = 0, offset = 0uL, length = 2, runLength = 1)
                    ),
                    header(tileDataLength = 2uL),
                    limits =
                        defaultLimits.toBuilder().apply { maxTileCompressedBytes = 1uL }.build(),
                )
            }
        assertEquals(PmTilesErrorCodes.LimitExceeded, tooLargeTile.code)

        val outOfSection =
            assertFailsWith<PmTilesException> {
                decodeDirectory(
                    encodeDirectory(
                        DirectoryEntry(tileId = 0, offset = 3uL, length = 2, runLength = 1)
                    ),
                    header(tileDataLength = 4uL),
                    limits = defaultLimits,
                )
            }
        assertEquals(PmTilesErrorCodes.InvalidDirectory, outOfSection.code)
    }

    @Test
    fun rejectsMalformedVarintsAndOverflowedTileIds() {
        val malformedVarint =
            assertFailsWith<PmTilesException> {
                decodeDirectory(
                    buildByteString(11) {
                        repeat(11) { append(0x80.toByte()) }
                    },
                    header(),
                    defaultLimits,
                )
            }
        assertEquals(PmTilesErrorCodes.InvalidVarint, malformedVarint.code)

        val overflowedTileId =
            assertFailsWith<PmTilesException> {
                decodeDirectory(
                    bytes(1uL, Long.MAX_VALUE.toULong() + 1uL, 1uL, 1uL, 1uL),
                    header(),
                    defaultLimits,
                )
            }
        assertEquals(PmTilesErrorCodes.InvalidDirectory, overflowedTileId.code)
    }

    private fun assertDirectoryFails(code: PmTilesErrorCode, bytes: ByteString) {
        val error =
            assertFailsWith<PmTilesException> {
                decodeDirectory(bytes, header(), defaultLimits)
            }

        assertEquals(code, error.code)
    }

    private fun header(
        tileDataLength: ULong = 1uL,
        leafDirectoriesLength: ULong = 0uL,
    ) =
        parseHeader(
                ByteString(
                    buildHeader(
                        TestHeaderFields(
                            leafDirectoriesOffset = 200uL,
                            leafDirectoriesLength = leafDirectoriesLength,
                            tileDataOffset = 300uL,
                            tileDataLength = tileDataLength,
                        )
                    )
                ),
                archiveSize = 300uL + tileDataLength,
            )
            .copy(leafDirectories = ArchiveSection(200uL, leafDirectoriesLength))

    private fun bytes(vararg values: ULong): ByteString = buildByteString {
        values.forEach { value -> writeVarint(value) }
    }

    private fun ByteStringBuilder.writeVarint(value: ULong) {
        var remaining = value
        while (remaining >= 0x80uL) {
            append(((remaining and 0x7fuL) or 0x80uL).toByte())
            remaining = remaining shr 7
        }
        append(remaining.toByte())
    }

    private val defaultLimits = org.maplibre.spatialk.pmtiles.ArchiveLimits()
}
