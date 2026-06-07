@file:OptIn(ExperimentalObjCName::class)

package org.maplibre.spatialk.pmtiles

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Error thrown by PMTiles archive operations.
 *
 * @property code Stable machine-readable error code.
 */
public class PmTilesException(
    /** Stable machine-readable error code. */
    public val code: PmTilesErrorCode,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    public constructor(code: PmTilesErrorCode, message: String) : this(code, message, null)
}

/** Stable error codes emitted by PMTiles archive operations. */
public enum class PmTilesErrorCode {
    /** The archive does not start with the PMTiles magic bytes. */
    @ObjCName(swiftName = "invalidMagic") InvalidMagic,

    /** The archive uses a PMTiles version unsupported by this reader. */
    @ObjCName(swiftName = "unsupportedVersion") UnsupportedVersion,

    /** The header is malformed or internally inconsistent. */
    @ObjCName(swiftName = "invalidHeader") InvalidHeader,

    /** Header section offsets or lengths are invalid. */
    @ObjCName(swiftName = "invalidSectionLayout") InvalidSectionLayout,

    /** The root directory is not fully contained in the first 16 KiB. */
    @ObjCName(swiftName = "invalidRootDirectoryLocation") InvalidRootDirectoryLocation,

    /** A root or leaf directory is malformed. */
    @ObjCName(swiftName = "invalidDirectory") InvalidDirectory,

    /** A varint is malformed, unterminated, or overflows. */
    @ObjCName(swiftName = "invalidVarint") InvalidVarint,

    /** A tile coordinate or TileID is outside the supported range. */
    @ObjCName(swiftName = "invalidTileCoordinate") InvalidTileCoordinate,

    /** The archive requires a compression codec this reader does not implement. */
    @ObjCName(swiftName = "unsupportedCompression") UnsupportedCompression,

    /** Compressed data could not be decompressed. */
    @ObjCName(swiftName = "decompressionFailed") DecompressionFailed,

    /** Metadata JSON is malformed or violates strict metadata rules. */
    @ObjCName(swiftName = "invalidMetadata") InvalidMetadata,

    /** A requested byte range is outside the archive or configured limits. */
    @ObjCName(swiftName = "rangeOutOfBounds") RangeOutOfBounds,

    /** The caller-provided source could not satisfy a read. */
    @ObjCName(swiftName = "sourceUnavailable") SourceUnavailable,

    /** The caller-provided sink could not satisfy a write. */
    @ObjCName(swiftName = "sinkUnavailable") SinkUnavailable,

    /** Tile input provided to the writer is malformed or inconsistent. */
    @ObjCName(swiftName = "invalidTileInput") InvalidTileInput,

    /** A configured operational limit was exceeded. */
    @ObjCName(swiftName = "limitExceeded") LimitExceeded,

    /** The archive was used after it was closed. */
    @ObjCName(swiftName = "closed") Closed,
}

/** Warning codes recorded by lenient archive operations. */
public enum class ArchiveWarningCode {
    /** The archive uses an unknown tile type code. */
    @ObjCName(swiftName = "unknownTileType") UnknownTileType,

    /** The archive uses an unknown compression code that does not block the current operation. */
    @ObjCName(swiftName = "unknownCompressionCode") UnknownCompressionCode,

    /** A PMTiles count field uses the unknown-count sentinel. */
    @ObjCName(swiftName = "unknownCount") UnknownCount,

    /** Header sections are valid but not in canonical order. */
    @ObjCName(swiftName = "nonCanonicalSectionOrder") NonCanonicalSectionOrder,

    /** MVT metadata does not contain `vector_layers`. */
    @ObjCName(swiftName = "missingVectorLayers") MissingVectorLayers,

    /** Metadata was preserved after a recoverable lenient-mode metadata issue. */
    @ObjCName(swiftName = "invalidMetadataRecovered") InvalidMetadataRecovered,

    /** A lookup traversed a nested leaf directory. */
    @ObjCName(swiftName = "nestedLeafDirectory") NestedLeafDirectory,

    /** The archive root directory has zero entries and was accepted in lenient mode. */
    @ObjCName(swiftName = "emptyRootDirectory") EmptyRootDirectory,
}
