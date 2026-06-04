package org.maplibre.spatialk.pmtiles

import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

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
 * @property maxDirectoryEntries Maximum entries in one decoded directory. Defaults to a
 *   conservative entry count derived from [maxDirectoryDecompressedBytes]. When using [copy] to
 *   change [maxDirectoryDecompressedBytes], set this field explicitly if the existing entry limit
 *   no longer fits.
 * @property maxTileCompressedBytes Maximum compressed tile payload bytes.
 * @property maxTileDecompressedBytes Maximum decompressed tile payload bytes.
 * @property maxDirectoryDepth Maximum leaf-directory traversal depth.
 * @property maxLeafDirectoryCacheEntries Maximum leaf directories cached per archive.
 * @property maxVarintBytes Maximum bytes in a decoded varint.
 */
public class ArchiveLimits(
    public val maxInitialReadBytes: ULong = DEFAULT_INITIAL_READ_BYTES,
    public val maxMetadataBytes: ULong = DEFAULT_METADATA_BYTES,
    public val maxDirectoryCompressedBytes: ULong = DEFAULT_DIRECTORY_BYTES,
    public val maxDirectoryDecompressedBytes: ULong = DEFAULT_DIRECTORY_BYTES,
    public val maxDirectoryEntries: Int =
        maxDirectoryDecompressedBytes.defaultDirectoryEntryLimit(),
    public val maxTileCompressedBytes: ULong = DEFAULT_TILE_BYTES,
    public val maxTileDecompressedBytes: ULong = DEFAULT_TILE_BYTES,
    public val maxDirectoryDepth: Int = 3,
    public val maxLeafDirectoryCacheEntries: Int = 128,
    public val maxVarintBytes: Int = 10,
) {
    public constructor() :
        this(
            maxInitialReadBytes = DEFAULT_INITIAL_READ_BYTES,
            maxMetadataBytes = DEFAULT_METADATA_BYTES,
            maxDirectoryCompressedBytes = DEFAULT_DIRECTORY_BYTES,
            maxDirectoryDecompressedBytes = DEFAULT_DIRECTORY_BYTES,
            maxDirectoryEntries = (16 * 1024 * 1024) / 17,
            maxTileCompressedBytes = DEFAULT_TILE_BYTES,
            maxTileDecompressedBytes = DEFAULT_TILE_BYTES,
            maxDirectoryDepth = 3,
            maxLeafDirectoryCacheEntries = 128,
            maxVarintBytes = 10,
        )

    init {
        require(maxInitialReadBytes >= MIN_INITIAL_READ_BYTES.toULong()) {
            "maxInitialReadBytes must be at least $MIN_INITIAL_READ_BYTES."
        }
        require(maxDirectoryEntries >= 0) { "maxDirectoryEntries must be non-negative." }
        require(
            minEncodedDirectoryBytes(maxDirectoryEntries).toULong() <= maxDirectoryDecompressedBytes
        ) {
            "maxDirectoryEntries must fit within maxDirectoryDecompressedBytes."
        }
        require(maxDirectoryDepth >= 0) { "maxDirectoryDepth must be non-negative." }
        require(maxLeafDirectoryCacheEntries >= 0) {
            "maxLeafDirectoryCacheEntries must be non-negative."
        }
        require(maxVarintBytes > 0) { "maxVarintBytes must be positive." }
    }

    /** Returns a copy of these limits with selected fields replaced. */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    public fun copy(
        maxInitialReadBytes: ULong = this.maxInitialReadBytes,
        maxMetadataBytes: ULong = this.maxMetadataBytes,
        maxDirectoryCompressedBytes: ULong = this.maxDirectoryCompressedBytes,
        maxDirectoryDecompressedBytes: ULong = this.maxDirectoryDecompressedBytes,
        maxDirectoryEntries: Int = this.maxDirectoryEntries,
        maxTileCompressedBytes: ULong = this.maxTileCompressedBytes,
        maxTileDecompressedBytes: ULong = this.maxTileDecompressedBytes,
        maxDirectoryDepth: Int = this.maxDirectoryDepth,
        maxLeafDirectoryCacheEntries: Int = this.maxLeafDirectoryCacheEntries,
        maxVarintBytes: Int = this.maxVarintBytes,
    ): ArchiveLimits =
        ArchiveLimits(
            maxInitialReadBytes = maxInitialReadBytes,
            maxMetadataBytes = maxMetadataBytes,
            maxDirectoryCompressedBytes = maxDirectoryCompressedBytes,
            maxDirectoryDecompressedBytes = maxDirectoryDecompressedBytes,
            maxDirectoryEntries = maxDirectoryEntries,
            maxTileCompressedBytes = maxTileCompressedBytes,
            maxTileDecompressedBytes = maxTileDecompressedBytes,
            maxDirectoryDepth = maxDirectoryDepth,
            maxLeafDirectoryCacheEntries = maxLeafDirectoryCacheEntries,
            maxVarintBytes = maxVarintBytes,
        )

    /** Returns a copy of these limits with [maxInitialReadBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxInitialReadBytes(
        @ObjCName(swiftName = "_") maxInitialReadBytes: ULong
    ): ArchiveLimits = copy(maxInitialReadBytes = maxInitialReadBytes)

    /** Returns a copy of these limits with [maxMetadataBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxMetadataBytes(
        @ObjCName(swiftName = "_") maxMetadataBytes: ULong
    ): ArchiveLimits = copy(maxMetadataBytes = maxMetadataBytes)

    /** Returns a copy of these limits with [maxDirectoryCompressedBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxDirectoryCompressedBytes(
        @ObjCName(swiftName = "_") maxDirectoryCompressedBytes: ULong
    ): ArchiveLimits = copy(maxDirectoryCompressedBytes = maxDirectoryCompressedBytes)

    /** Returns a copy of these limits with [maxDirectoryDecompressedBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxDirectoryDecompressedBytes(
        @ObjCName(swiftName = "_") maxDirectoryDecompressedBytes: ULong
    ): ArchiveLimits =
        copy(
            maxDirectoryDecompressedBytes = maxDirectoryDecompressedBytes,
            maxDirectoryEntries =
                if (
                    minEncodedDirectoryBytes(maxDirectoryEntries).toULong() <=
                        maxDirectoryDecompressedBytes
                ) {
                    maxDirectoryEntries
                } else {
                    maxDirectoryDecompressedBytes.defaultDirectoryEntryLimit()
                },
        )

    /** Returns a copy of these limits with [maxDirectoryEntries]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxDirectoryEntries(
        @ObjCName(swiftName = "_") maxDirectoryEntries: Int
    ): ArchiveLimits = copy(maxDirectoryEntries = maxDirectoryEntries)

    /** Returns a copy of these limits with [maxTileCompressedBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxTileCompressedBytes(
        @ObjCName(swiftName = "_") maxTileCompressedBytes: ULong
    ): ArchiveLimits = copy(maxTileCompressedBytes = maxTileCompressedBytes)

    /** Returns a copy of these limits with [maxTileDecompressedBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxTileDecompressedBytes(
        @ObjCName(swiftName = "_") maxTileDecompressedBytes: ULong
    ): ArchiveLimits = copy(maxTileDecompressedBytes = maxTileDecompressedBytes)

    /** Returns a copy of these limits with [maxDirectoryDepth]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxDirectoryDepth(
        @ObjCName(swiftName = "_") maxDirectoryDepth: Int
    ): ArchiveLimits = copy(maxDirectoryDepth = maxDirectoryDepth)

    /** Returns a copy of these limits with [maxLeafDirectoryCacheEntries]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxLeafDirectoryCacheEntries(
        @ObjCName(swiftName = "_") maxLeafDirectoryCacheEntries: Int
    ): ArchiveLimits = copy(maxLeafDirectoryCacheEntries = maxLeafDirectoryCacheEntries)

    /** Returns a copy of these limits with [maxVarintBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxVarintBytes(@ObjCName(swiftName = "_") maxVarintBytes: Int): ArchiveLimits =
        copy(maxVarintBytes = maxVarintBytes)

    override fun equals(other: Any?): Boolean =
        other is ArchiveLimits &&
            maxInitialReadBytes == other.maxInitialReadBytes &&
            maxMetadataBytes == other.maxMetadataBytes &&
            maxDirectoryCompressedBytes == other.maxDirectoryCompressedBytes &&
            maxDirectoryDecompressedBytes == other.maxDirectoryDecompressedBytes &&
            maxDirectoryEntries == other.maxDirectoryEntries &&
            maxTileCompressedBytes == other.maxTileCompressedBytes &&
            maxTileDecompressedBytes == other.maxTileDecompressedBytes &&
            maxDirectoryDepth == other.maxDirectoryDepth &&
            maxLeafDirectoryCacheEntries == other.maxLeafDirectoryCacheEntries &&
            maxVarintBytes == other.maxVarintBytes

    override fun hashCode(): Int {
        var result = maxInitialReadBytes.hashCode()
        result = 31 * result + maxMetadataBytes.hashCode()
        result = 31 * result + maxDirectoryCompressedBytes.hashCode()
        result = 31 * result + maxDirectoryDecompressedBytes.hashCode()
        result = 31 * result + maxDirectoryEntries
        result = 31 * result + maxTileCompressedBytes.hashCode()
        result = 31 * result + maxTileDecompressedBytes.hashCode()
        result = 31 * result + maxDirectoryDepth
        result = 31 * result + maxLeafDirectoryCacheEntries
        result = 31 * result + maxVarintBytes
        return result
    }

    override fun toString(): String =
        "ArchiveLimits(" +
            "maxInitialReadBytes=$maxInitialReadBytes, " +
            "maxMetadataBytes=$maxMetadataBytes, " +
            "maxDirectoryCompressedBytes=$maxDirectoryCompressedBytes, " +
            "maxDirectoryDecompressedBytes=$maxDirectoryDecompressedBytes, " +
            "maxDirectoryEntries=$maxDirectoryEntries, " +
            "maxTileCompressedBytes=$maxTileCompressedBytes, " +
            "maxTileDecompressedBytes=$maxTileDecompressedBytes, " +
            "maxDirectoryDepth=$maxDirectoryDepth, " +
            "maxLeafDirectoryCacheEntries=$maxLeafDirectoryCacheEntries, " +
            "maxVarintBytes=$maxVarintBytes" +
            ")"
}

/**
 * Controls source-range coalescing for batch tile reads.
 *
 * @property maxCoalescedBytes Maximum bytes when combining multiple tile payload reads. Individual
 *   tile payload reads may be larger. Set to zero to disable coalescing.
 * @property maxGapBytes Maximum unread bytes to include between adjacent tile payloads.
 */
public class TileReadCoalescing(
    public val maxCoalescedBytes: ULong = DEFAULT_COALESCED_BYTES,
    public val maxGapBytes: ULong = 0uL,
) {
    public constructor() : this(maxCoalescedBytes = DEFAULT_COALESCED_BYTES, maxGapBytes = 0uL)

    /** Returns a copy of this coalescing configuration with selected fields replaced. */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    public fun copy(
        maxCoalescedBytes: ULong = this.maxCoalescedBytes,
        maxGapBytes: ULong = this.maxGapBytes,
    ): TileReadCoalescing =
        TileReadCoalescing(maxCoalescedBytes = maxCoalescedBytes, maxGapBytes = maxGapBytes)

    /** Returns a copy of this coalescing configuration with [maxCoalescedBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxCoalescedBytes(
        @ObjCName(swiftName = "_") maxCoalescedBytes: ULong
    ): TileReadCoalescing = copy(maxCoalescedBytes = maxCoalescedBytes)

    /** Returns a copy of this coalescing configuration with [maxGapBytes]. */
    @OptIn(ExperimentalObjCName::class)
    public fun withMaxGapBytes(@ObjCName(swiftName = "_") maxGapBytes: ULong): TileReadCoalescing =
        copy(maxGapBytes = maxGapBytes)

    override fun equals(other: Any?): Boolean =
        other is TileReadCoalescing &&
            maxCoalescedBytes == other.maxCoalescedBytes &&
            maxGapBytes == other.maxGapBytes

    override fun hashCode(): Int {
        var result = maxCoalescedBytes.hashCode()
        result = 31 * result + maxGapBytes.hashCode()
        return result
    }

    override fun toString(): String =
        "TileReadCoalescing(maxCoalescedBytes=$maxCoalescedBytes, maxGapBytes=$maxGapBytes)"
}

/**
 * Options used when opening a PMTiles archive.
 *
 * @property validationMode Validation behavior for archive parsing and traversal.
 * @property limits Operational read limits.
 */
public class ArchiveOpenOptions
private constructor(
    public val validationMode: ValidationMode,
    public val limits: ArchiveLimits,
    internal val decompressors: Map<Compression, Decompressor>,
) {
    public constructor() :
        this(
            validationMode = ValidationMode.Strict,
            limits = ArchiveLimits(),
        )

    public constructor(
        validationMode: ValidationMode
    ) : this(
        validationMode = validationMode,
        limits = ArchiveLimits(),
    )

    public constructor(
        limits: ArchiveLimits
    ) : this(
        validationMode = ValidationMode.Strict,
        limits = limits,
    )

    public constructor(
        validationMode: ValidationMode = ValidationMode.Strict,
        limits: ArchiveLimits = ArchiveLimits(),
    ) : this(
        validationMode = validationMode,
        limits = limits,
        decompressors = emptyMap(),
    )

    /** Returns a copy of these options with [validationMode]. */
    public fun with(validationMode: ValidationMode): ArchiveOpenOptions =
        copy(validationMode = validationMode)

    /** Returns a copy of these options with [limits]. */
    public fun with(limits: ArchiveLimits): ArchiveOpenOptions = copy(limits = limits)

    /** Returns a copy of these options with [validationMode] and [limits]. */
    public fun with(
        validationMode: ValidationMode,
        limits: ArchiveLimits,
    ): ArchiveOpenOptions = copy(validationMode = validationMode, limits = limits)

    /** Returns a copy of these options with [validationMode] and [limits]. */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    public fun copy(
        validationMode: ValidationMode = this.validationMode,
        limits: ArchiveLimits = this.limits,
    ): ArchiveOpenOptions =
        ArchiveOpenOptions(
            validationMode = validationMode,
            limits = limits,
            decompressors = decompressors,
        )

    /** Returns a copy of these options with [decompressor] registered for [compression]. */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    public fun withDecompressor(
        compression: Compression,
        decompressor: Decompressor,
    ): ArchiveOpenOptions =
        ArchiveOpenOptions(
            validationMode = validationMode,
            limits = limits,
            decompressors = decompressors + (compression to decompressor),
        )

    /** Returns a copy of these options with [decompressor] registered for [compression]. */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    public fun withDecompressor(
        compression: KnownCompression,
        decompressor: Decompressor,
    ): ArchiveOpenOptions = withDecompressor(Compression(compression), decompressor)

    override fun toString(): String =
        "ArchiveOpenOptions(validationMode=$validationMode, limits=$limits, decompressors=$decompressors)"

    override fun equals(other: Any?): Boolean =
        other is ArchiveOpenOptions &&
            validationMode == other.validationMode &&
            limits == other.limits &&
            decompressors == other.decompressors

    override fun hashCode(): Int {
        var result = validationMode.hashCode()
        result = 31 * result + limits.hashCode()
        result = 31 * result + decompressors.hashCode()
        return result
    }
}

private const val MIN_INITIAL_READ_BYTES = 16 * 1024
private val DEFAULT_INITIAL_READ_BYTES = (16 * 1024).toULong()
private val DEFAULT_METADATA_BYTES = (16 * 1024 * 1024).toULong()
private val DEFAULT_DIRECTORY_BYTES = (16 * 1024 * 1024).toULong()
private val DEFAULT_TILE_BYTES = (64 * 1024 * 1024).toULong()
private val DEFAULT_COALESCED_BYTES = (512 * 1024).toULong()
private const val DEFAULT_MIN_DIRECTORY_ENTRY_BYTES = 17
private const val MIN_ENCODED_DIRECTORY_ENTRY_BYTES = 4
private const val VARINT_PAYLOAD_BITS = 7

private fun ULong.defaultDirectoryEntryLimit(): Int =
    minOf(
            this / DEFAULT_MIN_DIRECTORY_ENTRY_BYTES.toULong(),
            Int.MAX_VALUE.toULong(),
        )
        .toInt()

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
