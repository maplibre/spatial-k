package org.maplibre.spatialk.pmtiles

/**
 * Error thrown by the PMTiles reader.
 *
 * @property code Stable machine-readable error code.
 */
public class PmTilesException(
    public val code: PmTilesErrorCode,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/** Stable error codes emitted by the PMTiles reader. */
public enum class PmTilesErrorCode {
    /** The archive does not start with the PMTiles magic bytes. */
    InvalidMagic,

    /** The archive uses a PMTiles version unsupported by this reader. */
    UnsupportedVersion,

    /** The header is malformed or internally inconsistent. */
    InvalidHeader,

    /** Header section offsets or lengths are invalid. */
    InvalidSectionLayout,

    /** The root directory is not fully contained in the first 16 KiB. */
    InvalidRootDirectoryLocation,

    /** A root or leaf directory is malformed. */
    InvalidDirectory,

    /** A varint is malformed, unterminated, or overflows. */
    InvalidVarint,

    /** A tile coordinate or TileID is outside the supported range. */
    InvalidTileCoordinate,

    /** The archive requires a compression codec this reader does not implement. */
    UnsupportedCompression,

    /** Compressed data could not be decompressed. */
    DecompressionFailed,

    /** Metadata JSON is malformed or violates strict metadata rules. */
    InvalidMetadata,

    /** A requested byte range is outside the archive or configured limits. */
    RangeOutOfBounds,

    /** The caller-provided source changed while the archive was open. */
    SourceChanged,

    /** The caller-provided source could not satisfy a read. */
    SourceUnavailable,

    /** A configured operational limit was exceeded. */
    LimitExceeded,

    /** The archive was used after it was closed. */
    Closed,

    /** The operation was cancelled. */
    Cancelled,

    /** An internal invariant failed. */
    InternalError,
}

/** Warning codes recorded by lenient archive operations. */
public enum class ArchiveWarningCode {
    /** The archive uses an unknown tile type code. */
    UnknownTileType,

    /** The archive uses an unknown compression code that does not block the current operation. */
    UnknownCompressionCode,

    /** A PMTiles count field uses the unknown-count sentinel. */
    UnknownCount,

    /** Header sections are valid but not in canonical order. */
    NonCanonicalSectionOrder,

    /** MVT metadata does not contain `vector_layers`. */
    MissingVectorLayers,

    /** Metadata was preserved after a recoverable lenient-mode metadata issue. */
    InvalidMetadataRecovered,

    /** A lookup traversed a nested leaf directory. */
    NestedLeafDirectory,

    /** The archive root directory has zero entries and was accepted in lenient mode. */
    EmptyRootDirectory,
}
