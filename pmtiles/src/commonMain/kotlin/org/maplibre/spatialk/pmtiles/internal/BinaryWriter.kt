package org.maplibre.spatialk.pmtiles.internal

import kotlin.math.roundToInt
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.ByteStringBuilder

internal class BinaryWriter {
    private val builder = ByteStringBuilder()

    val size: Int
        get() = builder.size

    fun writeUInt8(value: UInt) {
        require(value <= UINT8_MAX) { "UInt8 value $value exceeds 255." }
        builder.append(value.toByte())
    }

    fun writeULong64Le(value: ULong) {
        var remaining = value
        repeat(ULong.SIZE_BYTES) {
            builder.append((remaining and BYTE_MASK).toByte())
            remaining = remaining shr Byte.SIZE_BITS
        }
    }

    fun writeInt32Le(value: Int) {
        repeat(Int.SIZE_BYTES) { index ->
            builder.append((value ushr (index * Byte.SIZE_BITS)).toByte())
        }
    }

    fun writeVarint(value: ULong) {
        var remaining = value
        while (remaining >= VARINT_CONTINUATION_THRESHOLD) {
            builder.append(
                ((remaining and VARINT_PAYLOAD_MASK) or VARINT_CONTINUATION_BIT).toByte()
            )
            remaining = remaining shr VARINT_PAYLOAD_BITS
        }
        builder.append(remaining.toByte())
    }

    fun writePosition(longitude: Double, latitude: Double) {
        writeInt32Le(longitude.toScaledPosition())
        writeInt32Le(latitude.toScaledPosition())
    }

    fun toByteString(): ByteString = builder.toByteString()
}

internal fun buildBinary(block: BinaryWriter.() -> Unit): ByteString =
    BinaryWriter().apply(block).toByteString()

private fun Double.toScaledPosition(): Int = (this * POSITION_SCALE).roundToInt()

private const val POSITION_SCALE = 10_000_000.0
private const val VARINT_PAYLOAD_BITS = 7
private const val VARINT_PAYLOAD_MASK = 0x7fuL
private const val VARINT_CONTINUATION_BIT = 0x80uL
private const val VARINT_CONTINUATION_THRESHOLD = 0x80uL
private const val BYTE_MASK = 0xffuL
private const val UINT8_MAX = 0xffu
