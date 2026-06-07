package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode

internal fun encodeDirectory(entries: List<DirectoryEntry>): ByteString {
    validateDirectoryEntries(entries)
    return buildBinary {
        writeVarint(entries.size.toULong())
        writeTileIds(entries)
        writeRunLengths(entries)
        writeLengths(entries)
        writeOffsets(entries)
    }
}

private fun validateDirectoryEntries(entries: List<DirectoryEntry>) {
    if (entries.isEmpty()) {
        throw invalidDirectory("Directory entries are required.")
    }

    var previousTileId: Long? = null
    entries.forEach { entry ->
        if (entry.tileId < 0) {
            throw invalidDirectory("Directory TileID ${entry.tileId} is negative.")
        }
        if (previousTileId != null && entry.tileId <= previousTileId) {
            throw invalidDirectory("Directory TileIDs must be strictly increasing.")
        }
        if (entry.length <= 0) {
            throw invalidDirectory("Directory entry length must be positive.")
        }
        if (entry.runLength < 0) {
            throw invalidDirectory("Directory run length must not be negative.")
        }
        previousTileId = entry.tileId
    }
}

private fun BinaryWriter.writeTileIds(entries: List<DirectoryEntry>) {
    var lastTileId = 0uL
    entries.forEach { entry ->
        val tileId = entry.tileId.toULong()
        writeVarint(tileId - lastTileId)
        lastTileId = tileId
    }
}

private fun BinaryWriter.writeRunLengths(entries: List<DirectoryEntry>) {
    entries.forEach { entry -> writeVarint(entry.runLength.toULong()) }
}

private fun BinaryWriter.writeLengths(entries: List<DirectoryEntry>) {
    entries.forEach { entry -> writeVarint(entry.length.toULong()) }
}

private fun BinaryWriter.writeOffsets(entries: List<DirectoryEntry>) {
    var nextOffset = 0uL
    entries.forEachIndexed { index, entry ->
        if (index > 0 && entry.offset == nextOffset) {
            writeVarint(0uL)
        } else {
            writeVarint(checkedAdd(entry.offset, 1uL, PmTilesErrorCode.InvalidDirectory))
        }
        nextOffset =
            checkedAdd(
                entry.offset,
                entry.length.toULong(),
                PmTilesErrorCode.InvalidDirectory,
            )
    }
}

private fun invalidDirectory(message: String): Nothing =
    throw pmTilesException(PmTilesErrorCode.InvalidDirectory, message)
