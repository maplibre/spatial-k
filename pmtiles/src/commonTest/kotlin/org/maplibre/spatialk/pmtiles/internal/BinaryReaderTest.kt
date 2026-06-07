package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.io.bytestring.hexToByteString
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException

class BinaryReaderTest {
    @Test
    fun readsLittleEndianUnsignedFields() {
        val reader = BinaryReader("123456789abcdef011223344556677".hexToByteString())

        assertEquals(0x12u, reader.readUInt8())
        assertEquals(0x5634u, reader.readUInt16Le())
        assertEquals(0xdebc_9a78u, reader.readUInt32Le())
        assertEquals(0x7766_5544_3322_11f0uL, reader.readULong64Le())
    }

    @Test
    fun rejectsFixedWidthReadsPastEnd() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader("010203".hexToByteString()).readUInt32Le()
            }

        assertEquals(PmTilesErrorCodes.InvalidHeader, error.code)
    }

    @Test
    fun decodesVarints() {
        val reader = BinaryReader("00017f8001ff01ffffffffffffffffff01".hexToByteString())

        assertEquals(0uL, reader.readVarint(maxBytes = 10))
        assertEquals(1uL, reader.readVarint(maxBytes = 10))
        assertEquals(127uL, reader.readVarint(maxBytes = 10))
        assertEquals(128uL, reader.readVarint(maxBytes = 10))
        assertEquals(255uL, reader.readVarint(maxBytes = 10))
        assertEquals(ULong.MAX_VALUE, reader.readVarint(maxBytes = 10))
    }

    @Test
    fun rejectsUnterminatedVarintAtEndOfInput() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader("80".hexToByteString()).readVarint(maxBytes = 10)
            }

        assertEquals(PmTilesErrorCodes.InvalidVarint, error.code)
    }

    @Test
    fun rejectsVarintPastConfiguredByteLimit() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader("8001".hexToByteString()).readVarint(maxBytes = 1)
            }

        assertEquals(PmTilesErrorCodes.InvalidVarint, error.code)
    }

    @Test
    fun rejectsVarintOverflow() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader("ffffffffffffffffff02".hexToByteString()).readVarint(maxBytes = 10)
            }

        assertEquals(PmTilesErrorCodes.InvalidVarint, error.code)
    }

    @Test
    fun rejectsVarintLongerThanUnsignedLongEvenWithHigherConfiguredLimit() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader("8080808080808080808000".hexToByteString()).readVarint(maxBytes = 11)
            }

        assertEquals(PmTilesErrorCodes.InvalidVarint, error.code)
    }
}
