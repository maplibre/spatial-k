package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.ArchiveWriteTile
import org.maplibre.spatialk.pmtiles.CompressionCode
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.Compressor
import org.maplibre.spatialk.pmtiles.HeaderCounts
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.TilePayloadMode

internal data class AssembledTileData(
    val entries: List<DirectoryEntry>,
    val payloads: List<ByteString>,
    val counts: HeaderCounts,
    val length: ULong,
)

internal data class Fnv128Hash(
    val high: ULong,
    val low: ULong,
)

internal suspend fun assembleTileData(
    tiles: List<ArchiveWriteTile>,
    options: ArchiveWriteOptions,
): AssembledTileData {
    if (tiles.isEmpty()) {
        throw invalidTileInput("At least one tile is required.")
    }

    val compressors = options.effectiveCompressors()
    val sortedTiles = tiles.sortedBy { tile -> tile.coord.toTileId() }
    val deduplicatedPayloads = mutableMapOf<Fnv128Hash, StoredPayload>()
    val storedPayloads = mutableListOf<ByteString>()
    val entries = mutableListOf<DirectoryEntry>()
    var tileDataLength = 0uL
    var previousTileId: Long? = null

    sortedTiles.forEach { tile ->
        val tileId = tile.coord.toTileId()
        if (tileId == previousTileId) {
            throw invalidTileInput(
                "Duplicate tile coordinate z=${tile.coord.z} x=${tile.coord.x} y=${tile.coord.y}."
            )
        }

        val storedPayload =
            if (options.deduplicateTilePayloads) {
                val hash = fnv1a128(tile.payload)
                deduplicatedPayloads[hash]
                    ?: tile
                        .storeNewPayload(compressors, options, storedPayloads, tileDataLength)
                        .also { payload -> deduplicatedPayloads[hash] = payload }
            } else {
                tile.storeNewPayload(compressors, options, storedPayloads, tileDataLength)
            }

        if (!storedPayload.isDeduplicatedFrom(tileDataLength)) {
            tileDataLength =
                checkedAdd(
                    tileDataLength,
                    storedPayload.length.toULong(),
                    PmTilesErrorCode.InvalidTileInput,
                )
        }
        entries.addOrExtend(tileId, storedPayload)
        previousTileId = tileId
    }

    return AssembledTileData(
        entries = entries,
        payloads = storedPayloads,
        counts =
            HeaderCounts(
                addressedTiles = entries.sumOf { it.runLength.toULong() },
                tileEntries = entries.size.toULong(),
                tileContents = storedPayloads.size.toULong(),
            ),
        length = tileDataLength,
    )
}

internal fun fnv1a128(bytes: ByteString): Fnv128Hash {
    var hash = FNV_128_OFFSET
    for (index in 0 until bytes.size) {
        val byte = bytes[index]
        hash = hash.copy(low = hash.low xor byte.toUByte().toULong())
        hash = hash.timesPrime()
    }
    return hash
}

private data class StoredPayload(
    val offset: ULong,
    val length: Int,
)

private suspend fun ArchiveWriteTile.storeNewPayload(
    compressors: Map<CompressionCode, Compressor>,
    options: ArchiveWriteOptions,
    storedPayloads: MutableList<ByteString>,
    offset: ULong,
): StoredPayload {
    val storedBytes =
        when (payloadMode) {
            TilePayloadMode.Stored -> {
                validateStoredTilePayload(payload, options)
                payload
            }
            TilePayloadMode.Uncompressed ->
                compressors.compress(
                    compression = options.tileCompression,
                    bytes = payload,
                    limits =
                        CompressionLimits(
                            maxUncompressedBytes = options.limits.maxTileBytes,
                            maxCompressedBytes = options.limits.maxTileBytes,
                        ),
                    purpose = EncodePurpose.Tile,
                )
        }
    validateStoredTilePayload(storedBytes, options)
    storedPayloads += storedBytes
    return StoredPayload(offset = offset, length = storedBytes.size)
}

private fun StoredPayload.isDeduplicatedFrom(nextOffset: ULong): Boolean = offset != nextOffset

private fun MutableList<DirectoryEntry>.addOrExtend(tileId: Long, payload: StoredPayload) {
    val previous = lastOrNull()
    if (
        previous != null &&
            previous.offset == payload.offset &&
            previous.length == payload.length &&
            previous.tileId + previous.runLength == tileId
    ) {
        this[lastIndex] = previous.copy(runLength = previous.runLength + 1)
        return
    }

    this +=
        DirectoryEntry(
            tileId = tileId,
            offset = payload.offset,
            length = payload.length,
            runLength = 1,
        )
}

private fun validateStoredTilePayload(bytes: ByteString, options: ArchiveWriteOptions) {
    if (bytes.size == 0) {
        throw invalidTileInput("Stored tile payload length must be positive.")
    }
    allocationLength(bytes.size.toULong(), options.limits.maxTileBytes, "Tile payload")
}

private fun Fnv128Hash.timesPrime(): Fnv128Hash =
    shiftLeft(88) + shiftLeft(8) + timesSmall(FNV_128_PRIME_LOW)

private fun Fnv128Hash.timesSmall(multiplier: UInt): Fnv128Hash {
    var result = Fnv128Hash(high = 0uL, low = 0uL)
    var addend = this
    var remaining = multiplier
    while (remaining > 0u) {
        if ((remaining and 1u) == 1u) result += addend
        addend = addend.shiftLeft(1)
        remaining = remaining shr 1
    }
    return result
}

private fun Fnv128Hash.shiftLeft(bits: Int): Fnv128Hash {
    require(bits in 0..127) { "128-bit shift must be in 0..127." }
    return when {
        bits == 0 -> this
        bits < ULong.SIZE_BITS ->
            Fnv128Hash(
                high = (high shl bits) or (low shr (ULong.SIZE_BITS - bits)),
                low = low shl bits,
            )
        else -> Fnv128Hash(high = low shl (bits - ULong.SIZE_BITS), low = 0uL)
    }
}

private operator fun Fnv128Hash.plus(other: Fnv128Hash): Fnv128Hash {
    val lowSum = low + other.low
    val carry = if (lowSum < low) 1uL else 0uL
    return Fnv128Hash(high = high + other.high + carry, low = lowSum)
}

private fun invalidTileInput(message: String): Nothing =
    throw pmTilesException(PmTilesErrorCode.InvalidTileInput, message)

private val FNV_128_OFFSET = Fnv128Hash(high = 0x6c62272e07bb0142uL, low = 0x62b821756295c58duL)
private const val FNV_128_PRIME_LOW: UInt = 0x3bu
