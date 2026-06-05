package org.maplibre.spatialk.pmtiles

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

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
public data class ArchiveLimits(
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
            maxDirectoryEntries = DEFAULT_DIRECTORY_BYTES.defaultDirectoryEntryLimit(),
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
}

/**
 * Controls source-range coalescing for batch tile reads.
 *
 * @property maxCoalescedBytes Maximum bytes when combining multiple tile payload reads. Individual
 *   tile payload reads may be larger. Set to zero to disable coalescing.
 * @property maxGapBytes Maximum unread bytes to include between adjacent tile payloads.
 */
public data class TileReadCoalescing(
    public val maxCoalescedBytes: ULong = DEFAULT_COALESCED_BYTES,
    public val maxGapBytes: ULong = 0uL,
) {
    public constructor() : this(maxCoalescedBytes = DEFAULT_COALESCED_BYTES, maxGapBytes = 0uL)
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
    internal val decompressors: Map<CompressionCode, Decompressor>,
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
        compression: CompressionCode,
        decompressor: Decompressor,
    ): ArchiveOpenOptions =
        ArchiveOpenOptions(
            validationMode = validationMode,
            limits = limits,
            decompressors = decompressors + (compression to decompressor),
        )

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

internal fun ULong.defaultDirectoryEntryLimit(): Int =
    minOf(
            this / DEFAULT_MIN_DIRECTORY_ENTRY_BYTES.toULong(),
            Int.MAX_VALUE.toULong(),
        )
        .toInt()

internal fun minEncodedDirectoryBytes(entryCount: Int): Long {
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
