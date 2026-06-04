package org.maplibre.spatialk.pmtiles

/** Tile payload read mode. */
public enum class TileReadMode {
    /** Return tile payload bytes exactly as stored in the archive. */
    CompressedBytes,

    /** Return decompressed tile payload bytes. */
    DecompressedBytes,
}

/** Archive validation mode. */
public enum class ValidationMode {
    /** Reject PMTiles spec violations. */
    Strict,

    /** Preserve recoverable anomalies as warnings. */
    Lenient,
}

/**
 * Operational limits for PMTiles archive reads.
 *
 * @property maxInitialReadBytes Maximum bytes read during archive open. Must be at least 16 KiB
 *   because PMTiles v3 root directories must be fully contained in the first 16 KiB.
 * @property maxMetadataBytes Maximum metadata payload bytes.
 * @property maxDirectoryCompressedBytes Maximum compressed directory bytes.
 * @property maxDirectoryDecompressedBytes Maximum decompressed directory bytes.
 * @property maxDirectoryEntries Maximum entries in one decoded directory.
 * @property maxTileCompressedBytes Maximum compressed tile payload bytes.
 * @property maxTileDecompressedBytes Maximum decompressed tile payload bytes.
 * @property maxDirectoryDepth Maximum leaf-directory traversal depth.
 * @property maxLeafDirectoryCacheEntries Maximum leaf directories cached per archive.
 * @property maxVarintBytes Maximum bytes in a decoded varint.
 */
public data class ArchiveLimits(
    public val maxInitialReadBytes: Int = 16 * 1024,
    public val maxMetadataBytes: Int = 16 * 1024 * 1024,
    public val maxDirectoryCompressedBytes: Int = 16 * 1024 * 1024,
    public val maxDirectoryDecompressedBytes: Int = 16 * 1024 * 1024,
    public val maxDirectoryEntries: Int = maxDirectoryDecompressedBytes / 17,
    public val maxTileCompressedBytes: Int = 64 * 1024 * 1024,
    public val maxTileDecompressedBytes: Int = 64 * 1024 * 1024,
    public val maxDirectoryDepth: Int = 3,
    public val maxLeafDirectoryCacheEntries: Int = 128,
    public val maxVarintBytes: Int = 10,
) {
    init {
        require(maxInitialReadBytes >= MIN_INITIAL_READ_BYTES) {
            "maxInitialReadBytes must be at least $MIN_INITIAL_READ_BYTES."
        }
        require(maxMetadataBytes >= 0) { "maxMetadataBytes must be non-negative." }
        require(maxDirectoryCompressedBytes >= 0) {
            "maxDirectoryCompressedBytes must be non-negative."
        }
        require(maxDirectoryDecompressedBytes >= 0) {
            "maxDirectoryDecompressedBytes must be non-negative."
        }
        require(maxDirectoryEntries >= 0) { "maxDirectoryEntries must be non-negative." }
        require(
            minEncodedDirectoryBytes(maxDirectoryEntries) <= maxDirectoryDecompressedBytes.toLong()
        ) {
            "maxDirectoryEntries must fit within maxDirectoryDecompressedBytes."
        }
        require(maxTileCompressedBytes >= 0) { "maxTileCompressedBytes must be non-negative." }
        require(maxTileDecompressedBytes >= 0) { "maxTileDecompressedBytes must be non-negative." }
        require(maxDirectoryDepth >= 0) { "maxDirectoryDepth must be non-negative." }
        require(maxLeafDirectoryCacheEntries >= 0) {
            "maxLeafDirectoryCacheEntries must be non-negative."
        }
        require(maxVarintBytes > 0) { "maxVarintBytes must be positive." }
    }

    /** Default archive read limits. */
    public companion object {
        /** Default archive read limits. */
        public val Default: ArchiveLimits = ArchiveLimits()
    }
}

/**
 * Options used when opening a PMTiles archive.
 *
 * @property validationMode Validation behavior for archive parsing and traversal.
 * @property tileReadMode Default behavior for tile payload reads.
 * @property limits Operational read limits.
 */
public data class ArchiveOpenOptions(
    public val validationMode: ValidationMode = ValidationMode.Strict,
    public val tileReadMode: TileReadMode = TileReadMode.CompressedBytes,
    public val limits: ArchiveLimits = ArchiveLimits.Default,
) {
    /** Common open option presets. */
    public companion object {
        /** Strict validation with compressed tile bytes. */
        public val Default: ArchiveOpenOptions = ArchiveOpenOptions()

        /** Lenient validation with compressed tile bytes. */
        public val Lenient: ArchiveOpenOptions =
            ArchiveOpenOptions(validationMode = ValidationMode.Lenient)
    }
}

private const val MIN_INITIAL_READ_BYTES = 16 * 1024
private const val MIN_ENCODED_DIRECTORY_ENTRY_BYTES = 4
private const val VARINT_PAYLOAD_BITS = 7

private fun minEncodedDirectoryBytes(entryCount: Int): Long {
    val countBytes = varintByteCount(entryCount).toLong()
    val entryBytes = entryCount.toLong() * MIN_ENCODED_DIRECTORY_ENTRY_BYTES
    return countBytes + entryBytes
}

private fun varintByteCount(value: Int): Int {
    var bytes = 1
    var remaining = value ushr VARINT_PAYLOAD_BITS
    while (remaining != 0) {
        bytes += 1
        remaining = remaining ushr VARINT_PAYLOAD_BITS
    }
    return bytes
}
