package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes

internal data class BuiltDirectories(
    val rootEntries: List<DirectoryEntry>,
    val compressedRoot: ByteString,
    val compressedLeaves: List<ByteString>,
    val leafDirectoriesLength: ULong,
)

internal suspend fun buildDirectories(
    tileEntries: List<DirectoryEntry>,
    options: ArchiveWriteOptions,
): BuiltDirectories {
    val directRoot = encodeAndCompressDirectory(tileEntries, options, EncodePurpose.RootDirectory)
    if (directRoot.size.toULong() <= options.limits.maxRootDirectoryBytes) {
        return BuiltDirectories(
            rootEntries = tileEntries,
            compressedRoot = directRoot,
            compressedLeaves = emptyList(),
            leafDirectoriesLength = 0uL,
        )
    }

    return buildRootToLeafDirectories(tileEntries, options)
}

private suspend fun buildRootToLeafDirectories(
    tileEntries: List<DirectoryEntry>,
    options: ArchiveWriteOptions,
): BuiltDirectories {
    var leafSize = minOf(tileEntries.size, DEFAULT_LEAF_ENTRY_COUNT)
    while (leafSize <= tileEntries.size) {
        val built = buildLeafAttempt(tileEntries, options, leafSize)
        if (built.compressedRoot.size.toULong() <= options.limits.maxRootDirectoryBytes) {
            return built
        }
        if (leafSize == tileEntries.size) break
        leafSize = minOf(tileEntries.size, leafSize * 2)
    }

    throw pmTilesException(
        PmTilesErrorCodes.LimitExceeded,
        "Compressed root directory exceeds limit ${options.limits.maxRootDirectoryBytes} after leaf partitioning.",
    )
}

private suspend fun buildLeafAttempt(
    tileEntries: List<DirectoryEntry>,
    options: ArchiveWriteOptions,
    leafSize: Int,
): BuiltDirectories {
    val compressedLeaves = mutableListOf<ByteString>()
    val rootEntries = mutableListOf<DirectoryEntry>()
    var leafOffset = 0uL

    tileEntries.chunked(leafSize).forEach { leafEntries ->
        val compressedLeaf =
            encodeAndCompressDirectory(leafEntries, options, EncodePurpose.LeafDirectory)
        compressedLeaves += compressedLeaf
        rootEntries +=
            DirectoryEntry(
                tileId = leafEntries.first().tileId,
                offset = leafOffset,
                length = compressedLeaf.size,
                runLength = 0,
            )
        leafOffset =
            checkedAdd(
                leafOffset,
                compressedLeaf.size.toULong(),
                PmTilesErrorCodes.InvalidDirectory,
            )
    }

    val compressedRoot =
        encodeAndCompressDirectory(rootEntries, options, EncodePurpose.RootDirectory)
    return BuiltDirectories(
        rootEntries = rootEntries,
        compressedRoot = compressedRoot,
        compressedLeaves = compressedLeaves,
        leafDirectoriesLength = leafOffset,
    )
}

private suspend fun encodeAndCompressDirectory(
    entries: List<DirectoryEntry>,
    options: ArchiveWriteOptions,
    purpose: EncodePurpose,
): ByteString {
    val rawBytes = encodeDirectory(entries)
    return options
        .effectiveCompressors()
        .compress(
            compression = options.internalCompression,
            bytes = rawBytes,
            limits =
                CompressionLimits(
                    maxUncompressedBytes = options.limits.maxDirectoryBytes,
                    maxCompressedBytes = options.limits.maxDirectoryBytes,
                ),
            purpose = purpose,
        )
}

private const val DEFAULT_LEAF_ENTRY_COUNT = 4096
