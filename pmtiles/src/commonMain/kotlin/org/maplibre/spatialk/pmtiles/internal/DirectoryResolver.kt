package org.maplibre.spatialk.pmtiles.internal

import org.maplibre.spatialk.pmtiles.ArchiveHeader
import org.maplibre.spatialk.pmtiles.ArchiveOpenOptions
import org.maplibre.spatialk.pmtiles.ArchiveWarning
import org.maplibre.spatialk.pmtiles.ArchiveWarningCode
import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.ByteRangeSource
import org.maplibre.spatialk.pmtiles.Compression
import org.maplibre.spatialk.pmtiles.Decompressor
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.TileCoord
import org.maplibre.spatialk.pmtiles.TileRange
import org.maplibre.spatialk.pmtiles.ValidationMode
import org.maplibre.spatialk.pmtiles.decompress

internal class DirectoryResolver(
    private val header: ArchiveHeader,
    private val source: ByteRangeSource,
    private val options: ArchiveOpenOptions,
    private val decompressors: Map<Compression, Decompressor>,
    private val archiveSize: ULong,
    private val rootDirectory: List<DirectoryEntry>,
    private val state: ArchiveReadState,
) {
    suspend fun findTileRange(tileId: Long, coord: TileCoord): TileRange? {
        state.checkOpen()
        return findTileRange(
            directory = rootDirectory,
            tileId = tileId,
            coord = coord,
            depth = 0,
            visitedLeafRanges = mutableSetOf(),
        )
    }

    private suspend fun findTileRange(
        directory: List<DirectoryEntry>,
        tileId: Long,
        coord: TileCoord,
        depth: Int,
        visitedLeafRanges: MutableSet<ByteRange>,
    ): TileRange? {
        val entry = directory.findPredecessor(tileId) ?: return null

        if (entry.isTile) {
            return if (entry.coversTile(tileId)) entry.toTileRange(tileId, coord, depth) else null
        }

        val leafDepth = depth + 1
        val leaf = loadLeafDirectory(entry, leafDepth, visitedLeafRanges)
        return findTileRange(leaf, tileId, coord, leafDepth, visitedLeafRanges)
    }

    private suspend fun loadLeafDirectory(
        entry: DirectoryEntry,
        depth: Int,
        visitedLeafRanges: MutableSet<ByteRange>,
    ): List<DirectoryEntry> {
        if (depth > options.limits.maxDirectoryDepth) {
            throw pmTilesException(
                PmTilesErrorCode.LimitExceeded,
                "Directory depth $depth exceeds limit ${options.limits.maxDirectoryDepth}.",
            )
        }
        if (depth > 1) {
            if (options.validationMode == ValidationMode.Strict) {
                throw pmTilesException(
                    PmTilesErrorCode.InvalidDirectory,
                    "Nested leaf directories are not allowed in strict mode.",
                )
            }
            state.appendWarning(
                ArchiveWarning(
                    code = ArchiveWarningCode.NestedLeafDirectory,
                    message = "Lookup traversed a nested leaf directory.",
                    context = "depth=$depth tileId=${entry.tileId}",
                )
            )
        }

        val range = entry.toLeafRange()
        if (!visitedLeafRanges.add(range)) {
            throw pmTilesException(
                PmTilesErrorCode.LimitExceeded,
                "Lookup revisited leaf directory range $range.",
            )
        }

        state.cachedLeafDirectory(range)?.let {
            return it
        }

        val compressedBytes =
            state.readSourceRangeDeduplicated(
                source = source,
                archiveSize = archiveSize,
                range = range,
                maxBytes = options.limits.maxDirectoryCompressedBytes,
            )
        val directoryBytes =
            decompressors.decompress(
                header.internalCompression,
                compressedBytes,
                DecodeLimits(
                    maxCompressedBytes = options.limits.maxDirectoryCompressedBytes,
                    maxDecompressedBytes = options.limits.maxDirectoryDecompressedBytes,
                    purpose = DecodePurpose.LeafDirectory,
                ),
            )
        val directory = decodeDirectory(directoryBytes, header, options.limits)
        return state.cacheLeafDirectory(
            range,
            directory,
            maxEntries = options.limits.maxLeafDirectoryCacheEntries,
        )
    }

    private fun DirectoryEntry.toTileRange(
        tileId: Long,
        coord: TileCoord,
        depth: Int,
    ): TileRange =
        TileRange(
            tileId = tileId,
            coord = coord,
            archiveRange =
                ByteRange(
                    offset =
                        checkedAdd(
                            header.tileData.offset,
                            offset,
                            PmTilesErrorCode.InvalidDirectory,
                        ),
                    length = length,
                ),
            tileType = header.tileType,
            compression = header.tileCompression,
            directoryDepth = depth,
        )

    private fun DirectoryEntry.toLeafRange(): ByteRange =
        ByteRange(
            offset =
                checkedAdd(
                    header.leafDirectories.offset,
                    offset,
                    PmTilesErrorCode.InvalidDirectory,
                ),
            length = length,
        )
}
