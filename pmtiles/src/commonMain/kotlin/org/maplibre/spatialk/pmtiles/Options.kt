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
 * Controls source-range coalescing for batch tile reads.
 *
 * @property maxCoalescedBytes Maximum bytes when combining multiple tile payload reads. Individual
 *   tile payload reads may be larger. Set to zero to disable coalescing.
 * @property maxGapBytes Maximum unread bytes to include between adjacent tile payloads.
 */
public data class TileReadCoalescing(
    public val maxCoalescedBytes: Int = 512 * 1024,
    public val maxGapBytes: Int = 0,
) {
    init {
        require(maxCoalescedBytes >= 0) { "maxCoalescedBytes must be non-negative." }
        require(maxGapBytes >= 0) { "maxGapBytes must be non-negative." }
    }

    /** Common tile read coalescing presets. */
    public companion object {
        /** Coalesce contiguous tile payloads into source reads up to 512 KiB. */
        public val Default: TileReadCoalescing = TileReadCoalescing()

        /** Do not coalesce batch tile payload reads. */
        public val Disabled: TileReadCoalescing = TileReadCoalescing(maxCoalescedBytes = 0)
    }
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
    public constructor(
        validationMode: ValidationMode = ValidationMode.Strict,
        limits: ArchiveLimits = ArchiveLimits.Default,
    ) : this(
        validationMode = validationMode,
        limits = limits,
        decompressors = emptyMap(),
    )

    /** Returns a copy of these options with [validationMode] and [limits]. */
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

    override fun toString(): String =
        "ArchiveOpenOptions(validationMode=$validationMode, limits=$limits, decompressors=$decompressors)"

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
