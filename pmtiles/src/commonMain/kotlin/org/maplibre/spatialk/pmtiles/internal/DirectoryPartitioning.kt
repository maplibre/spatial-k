package org.maplibre.spatialk.pmtiles.internal

import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveWriteOptions
import org.maplibre.spatialk.pmtiles.CompressionLimits
import org.maplibre.spatialk.pmtiles.PmTilesErrorCodes
import org.maplibre.spatialk.pmtiles.PmTilesException

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
    val directRoot = tryBuildDirectRoot(tileEntries, options)
    if (directRoot != null) {
        return BuiltDirectories(
            rootEntries = tileEntries,
            compressedRoot = directRoot,
            compressedLeaves = emptyList(),
            leafDirectoriesLength = 0uL,
        )
    }

    return buildRootToLeafDirectories(tileEntries, options)
}

private suspend fun tryBuildDirectRoot(
    tileEntries: List<DirectoryEntry>,
    options: ArchiveWriteOptions,
): ByteString? {
    val rawBytes = encodeDirectory(tileEntries)
    if (rawBytes.size.toULong() > options.limits.maxDirectoryBytes) return null

    val compressedRoot =
        try {
            compressDirectoryBytes(
                rawBytes = rawBytes,
                options = options,
                purpose = EncodePurpose.RootDirectory,
                maxBytes =
                    maxOf(options.limits.maxDirectoryBytes, options.limits.maxRootDirectoryBytes),
            )
        } catch (error: PmTilesException) {
            if (error.code == PmTilesErrorCodes.LimitExceeded) return null
            throw error
        }

    if (compressedRoot.size.toULong() > options.limits.maxDirectoryBytes) return null
    if (compressedRoot.size.toULong() > options.limits.maxRootDirectoryBytes) return null
    return compressedRoot
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
    return compressDirectoryBytes(
        rawBytes = rawBytes,
        options = options,
        purpose = purpose,
        maxBytes = options.limits.maxDirectoryBytes,
    )
}

private suspend fun compressDirectoryBytes(
    rawBytes: ByteString,
    options: ArchiveWriteOptions,
    purpose: EncodePurpose,
    maxBytes: ULong,
): ByteString =
    options
        .effectiveCompressors()
        .compress(
            compression = options.internalCompression,
            bytes = rawBytes,
            limits =
                CompressionLimits(
                    maxUncompressedBytes = maxBytes,
                    maxCompressedBytes = maxBytes,
                ),
            purpose = purpose,
        )

private const val DEFAULT_LEAF_ENTRY_COUNT = 4096
