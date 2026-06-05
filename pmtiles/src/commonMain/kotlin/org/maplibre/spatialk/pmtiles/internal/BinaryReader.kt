package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode

internal class BinaryReader(
    private val bytes: ByteString,
    private val errorCode: PmTilesErrorCode = PmTilesErrorCode.InvalidHeader,
) {
    var position: Int = 0
        private set

    val remaining: Int
        get() = bytes.size - position

    fun seek(position: Int) {
        if (position < 0 || position > bytes.size) {
            throw pmTilesException(errorCode, "Binary reader position $position is out of bounds.")
        }
        this.position = position
    }

    fun readUInt8(): UInt {
        requireAvailable(1)
        return bytes[position++].toUByte().toUInt()
    }

    fun readUInt16Le(): UInt {
        requireAvailable(2)
        val start = position
        position += 2
        return bytes[start].unsigned() or (bytes[start + 1].unsigned() shl 8)
    }

    fun readUInt32Le(): UInt {
        requireAvailable(4)
        val start = position
        position += 4
        return bytes[start].unsigned() or
            (bytes[start + 1].unsigned() shl 8) or
            (bytes[start + 2].unsigned() shl 16) or
            (bytes[start + 3].unsigned() shl 24)
    }

    fun readInt32Le(): Int = readUInt32Le().toInt()

    fun readULong64Le(): ULong {
        requireAvailable(8)
        var value = 0uL
        for (shift in 0 until ULong.SIZE_BITS step Byte.SIZE_BITS) {
            value = value or (bytes[position++].toUByte().toULong() shl shift)
        }
        return value
    }

    fun readVarint(maxBytes: Int): ULong {
        if (maxBytes <= 0) {
            throw pmTilesException(
                PmTilesErrorCode.LimitExceeded,
                "Varint byte limit must be positive.",
            )
        }

        val byteLimit = minOf(maxBytes, MAX_UINT64_VARINT_BYTES)
        var value = 0uL
        var shift = 0
        repeat(byteLimit) { index ->
            if (remaining == 0) {
                throw pmTilesException(
                    PmTilesErrorCode.InvalidVarint,
                    "Varint ended before a terminating byte.",
                )
            }

            val byte = bytes[position++].toUByte().toUInt()
            val payload = byte and VARINT_PAYLOAD_MASK
            if (index == MAX_UINT64_VARINT_BYTES - 1 && payload > MAX_UINT64_TOP_PAYLOAD) {
                throw pmTilesException(
                    PmTilesErrorCode.InvalidVarint,
                    "Varint overflows an unsigned 64-bit value.",
                )
            }

            value = value or (payload.toULong() shl shift)
            if ((byte and VARINT_CONTINUATION_BIT) == 0u) {
                return value
            }
            shift += VARINT_PAYLOAD_BITS
        }

        throw pmTilesException(
            PmTilesErrorCode.InvalidVarint,
            if (maxBytes > MAX_UINT64_VARINT_BYTES) {
                "Varint exceeded the unsigned 64-bit byte limit."
            } else {
                "Varint exceeded the configured byte limit."
            },
        )
    }

    private fun requireAvailable(count: Int) {
        if (count < 0 || count > remaining) {
            throw pmTilesException(
                errorCode,
                "Binary reader requires $count bytes with $remaining bytes remaining.",
            )
        }
    }
}

private const val VARINT_PAYLOAD_BITS = 7
private const val MAX_UINT64_VARINT_BYTES = 10
private const val VARINT_PAYLOAD_MASK = 0x7fu
private const val VARINT_CONTINUATION_BIT = 0x80u
private const val MAX_UINT64_TOP_PAYLOAD = 0x01u

private fun Byte.unsigned(): UInt = toUByte().toUInt()
