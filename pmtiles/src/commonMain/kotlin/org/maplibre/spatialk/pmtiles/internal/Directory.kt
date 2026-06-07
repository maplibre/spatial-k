package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveHeader
import org.maplibre.spatialk.pmtiles.ArchiveLimits
import org.maplibre.spatialk.pmtiles.ArchiveSection
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes

internal data class DirectoryEntry(
    val tileId: Long,
    val offset: ULong,
    val length: Int,
    val runLength: Int,
) {
    val isLeaf: Boolean
        get() = runLength == 0

    val isTile: Boolean
        get() = runLength > 0
}

internal fun List<DirectoryEntry>.findPredecessor(targetTileId: Long): DirectoryEntry? {
    var low = 0
    var high = lastIndex
    var predecessor: DirectoryEntry? = null

    while (low <= high) {
        val mid = low + (high - low) / 2
        val entry = this[mid]
        if (entry.tileId <= targetTileId) {
            predecessor = entry
            low = mid + 1
        } else {
            high = mid - 1
        }
    }

    return predecessor
}

internal fun DirectoryEntry.coversTile(targetTileId: Long): Boolean {
    if (!isTile) return false
    if (tileId > Long.MAX_VALUE - runLength.toLong()) {
        throw pmTilesException(
            PmTilesErrorCodes.InvalidDirectory,
            "Directory tile run overflows the supported TileID range.",
        )
    }
    return targetTileId < tileId + runLength
}

internal fun decodeDirectory(
    bytes: ByteString,
    header: ArchiveHeader,
    limits: ArchiveLimits,
    allowEmpty: Boolean = false,
): List<DirectoryEntry> {
    val reader = BinaryReader(bytes, PmTilesErrorCodes.InvalidDirectory)
    val entryCount = reader.readEntryCount(limits, allowEmpty)
    val tileIds = reader.readTileIds(entryCount, limits)
    val runLengths = reader.readRunLengths(entryCount, limits)
    val lengths = reader.readLengths(entryCount, runLengths, header, limits)
    val offsets = reader.readOffsets(entryCount, lengths, limits)

    if (reader.remaining != 0) {
        throw pmTilesException(
            PmTilesErrorCodes.InvalidDirectory,
            "Directory has ${reader.remaining} trailing bytes.",
        )
    }

    return List(entryCount) { index ->
        DirectoryEntry(
                tileId = tileIds[index],
                offset = offsets[index],
                length = lengths[index],
                runLength = runLengths[index],
            )
            .also { entry -> validateEntryRange(entry, header) }
    }
}

private fun BinaryReader.readEntryCount(limits: ArchiveLimits, allowEmpty: Boolean): Int {
    val count = readVarint(limits.maxVarintBytes)
    if (count == 0uL && !allowEmpty) {
        throw pmTilesException(
            PmTilesErrorCodes.InvalidDirectory,
            "Directory entry count is zero.",
        )
    }
    if (count > limits.maxDirectoryEntries.toULong()) {
        throw pmTilesException(
            PmTilesErrorCodes.LimitExceeded,
            "Directory entry count $count exceeds limit ${limits.maxDirectoryEntries}.",
        )
    }
    return count.toInt()
}

private fun BinaryReader.readTileIds(entryCount: Int, limits: ArchiveLimits): LongArray {
    val tileIds = LongArray(entryCount)
    var lastTileId = 0uL
    repeat(entryCount) { index ->
        val delta = readVarint(limits.maxVarintBytes)
        val tileId = checkedAdd(lastTileId, delta, PmTilesErrorCodes.InvalidDirectory)
        if (index > 0 && tileId <= lastTileId) {
            throw pmTilesException(
                PmTilesErrorCodes.InvalidDirectory,
                "Directory TileIDs must be strictly increasing.",
            )
        }
        tileIds[index] = tileId.toLongChecked("Directory TileID")
        lastTileId = tileId
    }
    return tileIds
}

private fun BinaryReader.readRunLengths(entryCount: Int, limits: ArchiveLimits): IntArray {
    val runLengths = IntArray(entryCount)
    repeat(entryCount) { index ->
        runLengths[index] = readOperationalInt("Directory run length", limits)
    }
    return runLengths
}

private fun BinaryReader.readLengths(
    entryCount: Int,
    runLengths: IntArray,
    header: ArchiveHeader,
    limits: ArchiveLimits,
): IntArray {
    val lengths = IntArray(entryCount)
    repeat(entryCount) { index ->
        val length = readOperationalInt("Directory entry length", limits)
        if (length == 0) {
            throw pmTilesException(
                PmTilesErrorCodes.InvalidDirectory,
                "Directory entry length is zero.",
            )
        }
        val maxBytes =
            if (runLengths[index] == 0) {
                limits.maxDirectoryCompressedBytes
            } else {
                limits.maxTileCompressedBytes
            }
        allocationLength(length.toULong(), maxBytes, directoryLengthPurpose(runLengths[index]))
        lengths[index] = length
    }
    validateSectionAvailable(header.leafDirectories, runLengths, lengths, isLeaf = true)
    validateSectionAvailable(header.tileData, runLengths, lengths, isLeaf = false)
    return lengths
}

private fun BinaryReader.readOffsets(
    entryCount: Int,
    lengths: IntArray,
    limits: ArchiveLimits,
): Array<ULong> {
    val offsets = Array(entryCount) { 0uL }
    repeat(entryCount) { index ->
        val encodedOffset = readVarint(limits.maxVarintBytes)
        offsets[index] =
            if (encodedOffset == 0uL) {
                if (index == 0) {
                    throw pmTilesException(
                        PmTilesErrorCodes.InvalidDirectory,
                        "First directory entry cannot use contiguous offset shorthand.",
                    )
                }
                checkedAdd(
                    offsets[index - 1],
                    lengths[index - 1].toULong(),
                    PmTilesErrorCodes.InvalidDirectory,
                )
            } else {
                encodedOffset - 1uL
            }
    }
    return offsets
}

private fun validateEntryRange(entry: DirectoryEntry, header: ArchiveHeader) {
    val section =
        if (entry.isLeaf) {
            header.leafDirectories
        } else {
            header.tileData
        }
    val end = checkedAdd(entry.offset, entry.length.toULong(), PmTilesErrorCodes.InvalidDirectory)
    if (end > section.length) {
        throw pmTilesException(
            PmTilesErrorCodes.InvalidDirectory,
            "Directory entry range exceeds its relative section length.",
        )
    }
}

private fun validateSectionAvailable(
    section: ArchiveSection,
    runLengths: IntArray,
    lengths: IntArray,
    isLeaf: Boolean,
) {
    if (section.length > 0uL) return
    val hasEntry =
        runLengths.indices.any { index ->
            val entryIsLeaf = runLengths[index] == 0
            entryIsLeaf == isLeaf && lengths[index] > 0
        }
    if (hasEntry) {
        throw pmTilesException(
            PmTilesErrorCodes.InvalidDirectory,
            "Directory entry points into an empty section.",
        )
    }
}

private fun BinaryReader.readOperationalInt(purpose: String, limits: ArchiveLimits): Int {
    val value = readVarint(limits.maxVarintBytes)
    return value.toIntChecked(purpose)
}

private fun ULong.toIntChecked(purpose: String): Int {
    if (this > Int.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCodes.LimitExceeded,
            "$purpose $this exceeds the supported Int range.",
        )
    }
    return toInt()
}

private fun ULong.toLongChecked(purpose: String): Long {
    if (this > Long.MAX_VALUE.toULong()) {
        throw pmTilesException(
            PmTilesErrorCodes.InvalidDirectory,
            "$purpose $this exceeds the supported Long range.",
        )
    }
    return toLong()
}

private fun directoryLengthPurpose(runLength: Int): String =
    if (runLength == 0) "Leaf directory entry" else "Tile entry"
