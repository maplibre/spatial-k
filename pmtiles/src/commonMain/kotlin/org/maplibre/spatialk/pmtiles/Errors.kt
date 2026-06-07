@file:OptIn(ExperimentalObjCName::class, ExperimentalObjCRefinement::class)

package org.maplibre.spatialk.pmtiles

import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.jvm.JvmInline
import kotlin.native.ObjCName
import kotlin.native.ShouldRefineInSwift

/**
 * Error thrown by PMTiles archive operations.
 *
 * @property code Stable machine-readable error code.
 */
public class PmTilesException(
    /** Stable machine-readable error code. */
    @ShouldRefineInSwift public val code: PmTilesErrorCode,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    public constructor(code: PmTilesErrorCode, message: String) : this(code, message, null)
}

/**
 * Stable error code emitted by PMTiles archive operations.
 *
 * @property code Raw error code.
 */
@JvmInline public value class PmTilesErrorCode(public val code: UInt)

/** PMTiles error-code constants. */
public object PmTilesErrorCodes {
    /** The archive does not start with the PMTiles magic bytes. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidMagic")
    public val InvalidMagic: PmTilesErrorCode = PmTilesErrorCode(1u)

    /** The archive uses a PMTiles version unsupported by this reader. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "unsupportedVersion")
    public val UnsupportedVersion: PmTilesErrorCode = PmTilesErrorCode(2u)

    /** The header is malformed or internally inconsistent. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidHeader")
    public val InvalidHeader: PmTilesErrorCode = PmTilesErrorCode(3u)

    /** Header section offsets or lengths are invalid. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidSectionLayout")
    public val InvalidSectionLayout: PmTilesErrorCode = PmTilesErrorCode(4u)

    /** The root directory is not fully contained in the first 16 KiB. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidRootDirectoryLocation")
    public val InvalidRootDirectoryLocation: PmTilesErrorCode = PmTilesErrorCode(5u)

    /** A root or leaf directory is malformed. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidDirectory")
    public val InvalidDirectory: PmTilesErrorCode = PmTilesErrorCode(6u)

    /** A varint is malformed, unterminated, or overflows. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidVarint")
    public val InvalidVarint: PmTilesErrorCode = PmTilesErrorCode(7u)

    /** A tile coordinate or TileID is outside the supported range. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidTileCoordinate")
    public val InvalidTileCoordinate: PmTilesErrorCode = PmTilesErrorCode(8u)

    /** The archive requires a compression codec this reader does not implement. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "unsupportedCompression")
    public val UnsupportedCompression: PmTilesErrorCode = PmTilesErrorCode(9u)

    /** Compressed data could not be decompressed. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "decompressionFailed")
    public val DecompressionFailed: PmTilesErrorCode = PmTilesErrorCode(10u)

    /** Data could not be compressed for archive writing. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "compressionFailed")
    public val CompressionFailed: PmTilesErrorCode = PmTilesErrorCode(11u)

    /** Metadata JSON is malformed or violates strict metadata rules. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidMetadata")
    public val InvalidMetadata: PmTilesErrorCode = PmTilesErrorCode(12u)

    /** A requested byte range is outside the archive or configured limits. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "rangeOutOfBounds")
    public val RangeOutOfBounds: PmTilesErrorCode = PmTilesErrorCode(13u)

    /** The caller-provided source could not satisfy a read. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "sourceUnavailable")
    public val SourceUnavailable: PmTilesErrorCode = PmTilesErrorCode(14u)

    /** The caller-provided sink could not satisfy a write. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "sinkUnavailable")
    public val SinkUnavailable: PmTilesErrorCode = PmTilesErrorCode(15u)

    /** Tile input provided to the writer is malformed or inconsistent. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidTileInput")
    public val InvalidTileInput: PmTilesErrorCode = PmTilesErrorCode(16u)

    /** A configured operational limit was exceeded. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "limitExceeded")
    public val LimitExceeded: PmTilesErrorCode = PmTilesErrorCode(17u)

    /** The archive was used after it was closed. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "closed")
    public val Closed: PmTilesErrorCode = PmTilesErrorCode(18u)
}

/**
 * Warning code recorded by lenient archive operations.
 *
 * @property code Raw warning code.
 */
@JvmInline public value class ArchiveWarningCode(public val code: UInt)

/** Archive warning-code constants. */
public object ArchiveWarningCodes {
    /** The archive uses an unknown tile type code. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "unknownTileType")
    public val UnknownTileType: ArchiveWarningCode = ArchiveWarningCode(1u)

    /** The archive uses an unknown compression code that does not block the current operation. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "unknownCompressionCode")
    public val UnknownCompressionCode: ArchiveWarningCode = ArchiveWarningCode(2u)

    /** A PMTiles count field uses the unknown-count sentinel. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "unknownCount")
    public val UnknownCount: ArchiveWarningCode = ArchiveWarningCode(3u)

    /** Header sections are valid but not in canonical order. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "nonCanonicalSectionOrder")
    public val NonCanonicalSectionOrder: ArchiveWarningCode = ArchiveWarningCode(4u)

    /** MVT metadata does not contain `vector_layers`. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "missingVectorLayers")
    public val MissingVectorLayers: ArchiveWarningCode = ArchiveWarningCode(5u)

    /** Metadata was preserved after a recoverable lenient-mode metadata issue. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "invalidMetadataRecovered")
    public val InvalidMetadataRecovered: ArchiveWarningCode = ArchiveWarningCode(6u)

    /** A lookup traversed a nested leaf directory. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "nestedLeafDirectory")
    public val NestedLeafDirectory: ArchiveWarningCode = ArchiveWarningCode(7u)

    /** The archive root directory has zero entries and was accepted in lenient mode. */
    @ShouldRefineInSwift
    @ObjCName(swiftName = "emptyRootDirectory")
    public val EmptyRootDirectory: ArchiveWarningCode = ArchiveWarningCode(8u)
}
