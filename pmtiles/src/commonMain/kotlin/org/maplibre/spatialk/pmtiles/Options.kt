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
 * @property maxInitialReadBytes Maximum bytes read during archive open.
 * @property maxMetadataBytes Maximum metadata payload bytes.
 * @property maxDirectoryCompressedBytes Maximum compressed directory bytes.
 * @property maxDirectoryDecompressedBytes Maximum decompressed directory bytes.
 * @property maxDirectoryEntries Maximum entries in one decoded directory.
 * @property maxTileCompressedBytes Maximum compressed tile payload bytes.
 * @property maxTileDecompressedBytes Maximum decompressed tile payload bytes.
 * @property maxDirectoryDepth Maximum leaf-directory traversal depth.
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
    public val maxVarintBytes: Int = 10,
) {
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
