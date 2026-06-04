package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

class BinaryReaderTest {
    @Test
    fun readsLittleEndianUnsignedFields() {
        val reader =
            BinaryReader(
                byteArrayOf(
                    0x12,
                    0x34,
                    0x56,
                    0x78,
                    0x9a.toByte(),
                    0xbc.toByte(),
                    0xde.toByte(),
                    0xf0.toByte(),
                    0x11,
                    0x22,
                    0x33,
                    0x44,
                    0x55,
                    0x66,
                    0x77,
                )
            )

        assertEquals(0x12u, reader.readUInt8())
        assertEquals(0x5634u, reader.readUInt16Le())
        assertEquals(0xdebc_9a78u, reader.readUInt32Le())
        assertEquals(0x7766_5544_3322_11f0uL, reader.readULong64Le())
    }

    @Test
    fun rejectsFixedWidthReadsPastEnd() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader(byteArrayOf(1, 2, 3)).readUInt32Le()
            }

        assertEquals(PmTilesErrorCode.InvalidHeader, error.code)
    }

    @Test
    fun decodesVarints() {
        val reader =
            BinaryReader(
                byteArrayOf(
                    0x00,
                    0x01,
                    0x7f,
                    0x80.toByte(),
                    0x01,
                    0xff.toByte(),
                    0x01,
                    0xff.toByte(),
                    0xff.toByte(),
                    0xff.toByte(),
                    0xff.toByte(),
                    0xff.toByte(),
                    0xff.toByte(),
                    0xff.toByte(),
                    0xff.toByte(),
                    0xff.toByte(),
                    0x01,
                )
            )

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
                BinaryReader(byteArrayOf(0x80.toByte())).readVarint(maxBytes = 10)
            }

        assertEquals(PmTilesErrorCode.InvalidVarint, error.code)
    }

    @Test
    fun rejectsVarintPastConfiguredByteLimit() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader(byteArrayOf(0x80.toByte(), 0x01)).readVarint(maxBytes = 1)
            }

        assertEquals(PmTilesErrorCode.InvalidVarint, error.code)
    }

    @Test
    fun rejectsVarintOverflow() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader(
                        byteArrayOf(
                            0xff.toByte(),
                            0xff.toByte(),
                            0xff.toByte(),
                            0xff.toByte(),
                            0xff.toByte(),
                            0xff.toByte(),
                            0xff.toByte(),
                            0xff.toByte(),
                            0xff.toByte(),
                            0x02,
                        )
                    )
                    .readVarint(maxBytes = 10)
            }

        assertEquals(PmTilesErrorCode.InvalidVarint, error.code)
    }

    @Test
    fun rejectsVarintLongerThanUnsignedLongEvenWithHigherConfiguredLimit() {
        val error =
            assertFailsWith<PmTilesException> {
                BinaryReader(
                        byteArrayOf(
                            0x80.toByte(),
                            0x80.toByte(),
                            0x80.toByte(),
                            0x80.toByte(),
                            0x80.toByte(),
                            0x80.toByte(),
                            0x80.toByte(),
                            0x80.toByte(),
                            0x80.toByte(),
                            0x80.toByte(),
                            0x00,
                        )
                    )
                    .readVarint(maxBytes = 11)
            }

        assertEquals(PmTilesErrorCode.InvalidVarint, error.code)
    }
}
