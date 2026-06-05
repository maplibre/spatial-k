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
 * @property maxDirectoryEntries Maximum entries in one decoded directory, derived from
 *   [maxDirectoryDecompressedBytes].
 * @property maxTileCompressedBytes Maximum compressed tile payload bytes.
 * @property maxTileDecompressedBytes Maximum decompressed tile payload bytes.
 * @property maxDirectoryDepth Maximum leaf-directory traversal depth.
 * @property maxLeafDirectoryCacheEntries Maximum leaf directories cached per archive.
 * @property maxVarintBytes Maximum bytes in a decoded varint.
 */
public class ArchiveLimits
private constructor(
    public val maxInitialReadBytes: ULong,
    public val maxMetadataBytes: ULong,
    public val maxDirectoryCompressedBytes: ULong,
    public val maxDirectoryDecompressedBytes: ULong,
    public val maxTileCompressedBytes: ULong,
    public val maxTileDecompressedBytes: ULong,
    public val maxDirectoryDepth: Int,
    public val maxLeafDirectoryCacheEntries: Int,
    public val maxVarintBytes: Int,
) {
    public constructor() :
        this(
            maxInitialReadBytes = (16 * 1024).toULong(),
            maxMetadataBytes = (16 * 1024 * 1024).toULong(),
            maxDirectoryCompressedBytes = (16 * 1024 * 1024).toULong(),
            maxDirectoryDecompressedBytes = (16 * 1024 * 1024).toULong(),
            maxTileCompressedBytes = (64 * 1024 * 1024).toULong(),
            maxTileDecompressedBytes = (64 * 1024 * 1024).toULong(),
            maxDirectoryDepth = 3,
            maxLeafDirectoryCacheEntries = 128,
            maxVarintBytes = 10,
        )

    public val maxDirectoryEntries: Int =
        minOf(
                maxDirectoryDecompressedBytes / 17uL,
                Int.MAX_VALUE.toULong(),
            )
            .toInt()

    init {
        require(maxInitialReadBytes >= MIN_INITIAL_READ_BYTES.toULong()) {
            "maxInitialReadBytes must be at least $MIN_INITIAL_READ_BYTES."
        }
        require(maxDirectoryDecompressedBytes > 0uL) {
            "maxDirectoryDecompressedBytes must be positive."
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
        public var maxInitialReadBytes: ULong = (16 * 1024).toULong()

        /** Maximum metadata payload bytes. */
        public var maxMetadataBytes: ULong = (16 * 1024 * 1024).toULong()

        /** Maximum compressed directory bytes. */
        public var maxDirectoryCompressedBytes: ULong = (16 * 1024 * 1024).toULong()

        /** Maximum decompressed directory bytes. */
        public var maxDirectoryDecompressedBytes: ULong = (16 * 1024 * 1024).toULong()

        /** Maximum compressed tile payload bytes. */
        public var maxTileCompressedBytes: ULong = (64 * 1024 * 1024).toULong()

        /** Maximum decompressed tile payload bytes. */
        public var maxTileDecompressedBytes: ULong = (64 * 1024 * 1024).toULong()

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
    public val maxCoalescedBytes: ULong = (512 * 1024).toULong(),
    public val maxGapBytes: ULong = 0uL,
) {
    public constructor() : this(maxCoalescedBytes = (512 * 1024).toULong(), maxGapBytes = 0uL)
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
