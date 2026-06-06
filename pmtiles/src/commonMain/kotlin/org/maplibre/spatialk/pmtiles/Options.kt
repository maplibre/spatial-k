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
 *   because PMTiles v3 root directories must be fully contained in the first 16 KiB. Defaults to 16
 *   KiB.
 * @property maxMetadataBytes Maximum metadata payload bytes. Defaults to 16 MiB.
 * @property maxDirectoryCompressedBytes Maximum compressed directory bytes. Defaults to 16 MiB.
 * @property maxDirectoryDecompressedBytes Maximum decompressed directory bytes. Defaults to 16 MiB.
 * @property maxDirectoryEntries Maximum entries in one decoded directory, derived from
 *   [maxDirectoryDecompressedBytes]. Defaults to 986895 entries.
 * @property maxTileCompressedBytes Maximum compressed tile payload bytes. Defaults to 64 MiB.
 * @property maxTileDecompressedBytes Maximum decompressed tile payload bytes. Defaults to 64 MiB.
 * @property maxDirectoryDepth Maximum leaf-directory traversal depth. Defaults to 3.
 * @property maxLeafDirectoryCacheEntries Maximum leaf directories cached per archive. Defaults
 *   to 128.
 * @property maxVarintBytes Maximum bytes in a decoded varint. Defaults to 10.
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
    /** Creates limits with the documented default values. */
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
        private val defaults: ArchiveLimits = ArchiveLimits()

        /** Maximum bytes read during archive open. */
        public var maxInitialReadBytes: ULong = defaults.maxInitialReadBytes

        /** Maximum metadata payload bytes. */
        public var maxMetadataBytes: ULong = defaults.maxMetadataBytes

        /** Maximum compressed directory bytes. */
        public var maxDirectoryCompressedBytes: ULong = defaults.maxDirectoryCompressedBytes

        /** Maximum decompressed directory bytes. */
        public var maxDirectoryDecompressedBytes: ULong = defaults.maxDirectoryDecompressedBytes

        /** Maximum compressed tile payload bytes. */
        public var maxTileCompressedBytes: ULong = defaults.maxTileCompressedBytes

        /** Maximum decompressed tile payload bytes. */
        public var maxTileDecompressedBytes: ULong = defaults.maxTileDecompressedBytes

        /** Maximum leaf-directory traversal depth. */
        public var maxDirectoryDepth: Int = defaults.maxDirectoryDepth

        /** Maximum leaf directories cached per archive. */
        public var maxLeafDirectoryCacheEntries: Int = defaults.maxLeafDirectoryCacheEntries

        /** Maximum bytes in a decoded varint. */
        public var maxVarintBytes: Int = defaults.maxVarintBytes

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
 *   tile payload reads may be larger. Set to zero to disable coalescing. Defaults to 512 KiB.
 * @property maxGapBytes Maximum unread bytes to include between adjacent tile payloads. Defaults to
 *   zero.
 */
public class TileReadCoalescing
private constructor(
    public val maxCoalescedBytes: ULong,
    public val maxGapBytes: ULong,
) {
    /** Creates coalescing options with the documented default values. */
    public constructor() : this(maxCoalescedBytes = (512 * 1024).toULong(), maxGapBytes = 0uL)

    /** Returns a mutable builder initialized from these coalescing options. */
    @HiddenFromObjC public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [TileReadCoalescing]. */
    @HiddenFromObjC
    public class Builder public constructor() {
        private val defaults: TileReadCoalescing = TileReadCoalescing()

        /** Maximum bytes when combining multiple tile payload reads. */
        public var maxCoalescedBytes: ULong = defaults.maxCoalescedBytes

        /** Maximum unread bytes to include between adjacent tile payloads. */
        public var maxGapBytes: ULong = defaults.maxGapBytes

        internal constructor(coalescing: TileReadCoalescing) : this() {
            maxCoalescedBytes = coalescing.maxCoalescedBytes
            maxGapBytes = coalescing.maxGapBytes
        }

        /** Builds immutable coalescing options from this builder. */
        public fun build(): TileReadCoalescing =
            TileReadCoalescing(
                maxCoalescedBytes = maxCoalescedBytes,
                maxGapBytes = maxGapBytes,
            )
    }

    /** Factory for Kotlin DSL construction. */
    public companion object {
        /** Builds [TileReadCoalescing] with a Kotlin DSL. */
        @HiddenFromObjC
        public fun build(configure: Builder.() -> Unit): TileReadCoalescing =
            Builder().apply(configure).build()
    }
}

/**
 * Options used when opening a PMTiles archive.
 *
 * @property validationMode Validation behavior for archive parsing and traversal. Defaults to
 *   [ValidationMode.Strict].
 * @property limits Operational read limits. Defaults to [ArchiveLimits].
 */
public class ArchiveOpenOptions
private constructor(
    public val validationMode: ValidationMode,
    public val limits: ArchiveLimits,
    internal val decompressors: Map<CompressionCode, Decompressor>,
) {
    /** Creates options with the documented default values. */
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
        private val defaults: ArchiveOpenOptions = ArchiveOpenOptions()

        /** Validation behavior for archive parsing and traversal. */
        public var validationMode: ValidationMode = defaults.validationMode

        /** Operational read limits. */
        public var limits: ArchiveLimits = defaults.limits
        private val decompressors: MutableMap<CompressionCode, Decompressor> =
            defaults.decompressors.toMutableMap()

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
