package org.maplibre.spatialk.pmtiles.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.io.bytestring.hexToByteString

class BinaryWriterTest {
    @Test
    fun writesLittleEndianFields() {
        val bytes = buildBinary {
            writeUInt8(0x12u)
            writeInt32Le(-2)
            writeULong64Le(0x7766_5544_3322_11f0uL)
        }

        assertEquals("12feffffff f011223344556677".compactHexToByteString(), bytes)
    }

    @Test
    fun writesVarints() {
        val bytes = buildBinary {
            writeVarint(0uL)
            writeVarint(1uL)
            writeVarint(127uL)
            writeVarint(128uL)
            writeVarint(255uL)
            writeVarint(ULong.MAX_VALUE)
        }

        assertEquals("00017f8001ff01ffffffffffffffffff01".hexToByteString(), bytes)
    }

    @Test
    fun writesScaledPositions() {
        val bytes = buildBinary { writePosition(longitude = 12.5, latitude = -7.25) }

        assertEquals("40597307e0bcadfb".hexToByteString(), bytes)
    }

    @Test
    fun roundsScaledPositionsToNearestInteger() {
        val bytes = buildBinary { writePosition(longitude = 0.00000016, latitude = -0.00000016) }

        assertEquals("02000000feffffff".hexToByteString(), bytes)
    }

    @Test
    fun tracksSize() {
        val writer = BinaryWriter()

        assertEquals(0, writer.size)
        writer.writeUInt8(1u)
        writer.writeULong64Le(2uL)

        assertEquals(9, writer.size)
    }

    @Test
    fun rejectsUInt8Overflow() {
        assertFailsWith<IllegalArgumentException> { BinaryWriter().writeUInt8(256u) }
    }
}

private fun String.compactHexToByteString(): kotlinx.io.bytestring.ByteString =
    filterNot { it.isWhitespace() }.lowercase().hexToByteString()
