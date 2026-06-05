@file:OptIn(ExperimentalObjCRefinement::class)

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
 *   conservative entry count derived from [maxDirectoryDecompressedBytes].
 * @property maxTileCompressedBytes Maximum compressed tile payload bytes.
 * @property maxTileDecompressedBytes Maximum decompressed tile payload bytes.
 * @property maxDirectoryDepth Maximum leaf-directory traversal depth.
 * @property maxLeafDirectoryCacheEntries Maximum leaf directories cached per archive.
 * @property maxVarintBytes Maximum bytes in a decoded varint.
 */
public class ArchiveLimits
private constructor(
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

    /** Returns a mutable builder initialized from these limits. */
    @HiddenFromObjC public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [ArchiveLimits]. */
    @HiddenFromObjC
    public class Builder public constructor() {
        /** Maximum bytes read during archive open. */
        public var maxInitialReadBytes: ULong = DEFAULT_INITIAL_READ_BYTES

        /** Maximum metadata payload bytes. */
        public var maxMetadataBytes: ULong = DEFAULT_METADATA_BYTES

        /** Maximum compressed directory bytes. */
        public var maxDirectoryCompressedBytes: ULong = DEFAULT_DIRECTORY_BYTES

        /** Maximum decompressed directory bytes. */
        public var maxDirectoryDecompressedBytes: ULong = DEFAULT_DIRECTORY_BYTES
            set(value) {
                field = value
                if (
                    !maxDirectoryEntriesWasExplicit ||
                        minEncodedDirectoryBytes(maxDirectoryEntries).toULong() > value
                ) {
                    maxDirectoryEntriesBacking = value.defaultDirectoryEntryLimit()
                    maxDirectoryEntriesWasExplicit = false
                }
            }

        private var maxDirectoryEntriesBacking: Int =
            DEFAULT_DIRECTORY_BYTES.defaultDirectoryEntryLimit()
        private var maxDirectoryEntriesWasExplicit: Boolean = false

        /** Maximum entries in one decoded directory. */
        public var maxDirectoryEntries: Int
            get() = maxDirectoryEntriesBacking
            set(value) {
                maxDirectoryEntriesBacking = value
                maxDirectoryEntriesWasExplicit = true
            }

        /** Maximum compressed tile payload bytes. */
        public var maxTileCompressedBytes: ULong = DEFAULT_TILE_BYTES

        /** Maximum decompressed tile payload bytes. */
        public var maxTileDecompressedBytes: ULong = DEFAULT_TILE_BYTES

        /** Maximum leaf-directory traversal depth. */
        public var maxDirectoryDepth: Int = 3

        /** Maximum leaf directories cached per archive. */
        public var maxLeafDirectoryCacheEntries: Int = 128

        /** Maximum bytes in a decoded varint. */
        public var maxVarintBytes: Int = 10

        internal constructor(limits: ArchiveLimits) : this() {
            maxInitialReadBytes = limits.maxInitialReadBytes
            maxMetadataBytes = limits.maxMetadataBytes
            maxDirectoryCompressedBytes = limits.maxDirectoryCompressedBytes
            maxDirectoryDecompressedBytes = limits.maxDirectoryDecompressedBytes
            maxDirectoryEntriesBacking = limits.maxDirectoryEntries
            maxDirectoryEntriesWasExplicit = true
            maxTileCompressedBytes = limits.maxTileCompressedBytes
            maxTileDecompressedBytes = limits.maxTileDecompressedBytes
            maxDirectoryDepth = limits.maxDirectoryDepth
            maxLeafDirectoryCacheEntries = limits.maxLeafDirectoryCacheEntries
            maxVarintBytes = limits.maxVarintBytes
        }

        /** Builds immutable limits from this builder. */
        public fun build(): ArchiveLimits =
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
    }

    /** Factory for Kotlin DSL construction. */
    public companion object {
        /** Builds [ArchiveLimits] with a Kotlin DSL. */
        @HiddenFromObjC
        public fun build(configure: Builder.() -> Unit): ArchiveLimits =
            Builder().apply(configure).build()
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
            decompressors = emptyMap(),
        )

    /** Returns a mutable builder initialized from these options. */
    @HiddenFromObjC public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [ArchiveOpenOptions]. */
    @HiddenFromObjC
    public class Builder public constructor() {
        /** Validation behavior for archive parsing and traversal. */
        public var validationMode: ValidationMode = ValidationMode.Strict

        /** Operational read limits. */
        public var limits: ArchiveLimits = ArchiveLimits()
        private val decompressors: MutableMap<CompressionCode, Decompressor> = mutableMapOf()

        internal constructor(options: ArchiveOpenOptions) : this() {
            validationMode = options.validationMode
            limits = options.limits
            decompressors += options.decompressors
        }

        /** Registers [decompressor] for [compression]. */
        public fun decompressor(
            compression: CompressionCode,
            decompressor: Decompressor,
        ): Builder = apply {
            decompressors[compression] = decompressor
        }

        /** Builds immutable options from this builder. */
        public fun build(): ArchiveOpenOptions =
            ArchiveOpenOptions(
                validationMode = validationMode,
                limits = limits,
                decompressors = decompressors.toMap(),
            )
    }

    /** Factory for Kotlin DSL construction. */
    public companion object {
        /** Builds [ArchiveOpenOptions] with a Kotlin DSL. */
        @HiddenFromObjC
        public fun build(configure: Builder.() -> Unit): ArchiveOpenOptions =
            Builder().apply(configure).build()
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
