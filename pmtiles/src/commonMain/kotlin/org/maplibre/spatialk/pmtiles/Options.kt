@file:OptIn(ExperimentalObjCRefinement::class)

package org.maplibre.spatialk.pmtiles

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ShouldRefineInSwift

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
    @ShouldRefineInSwift public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [ArchiveLimits]. */
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
 * Operational limits for PMTiles archive writes.
 *
 * @property maxRootDirectoryBytes Maximum compressed root directory bytes in the canonical first 16
 *   KiB archive layout. Defaults to 16,257 bytes.
 * @property maxMetadataBytes Maximum metadata bytes before and after internal compression. Defaults
 *   to 16 MiB.
 * @property maxDirectoryBytes Maximum directory bytes before and after internal compression.
 *   Defaults to 16 MiB.
 * @property maxTileBytes Maximum tile payload bytes before and after tile compression. Defaults to
 *   64 MiB.
 * @property maxArchiveBytes Maximum final archive bytes. Defaults to [ULong.MAX_VALUE].
 */
public class ArchiveWriteLimits
private constructor(
    public val maxRootDirectoryBytes: ULong,
    public val maxMetadataBytes: ULong,
    public val maxDirectoryBytes: ULong,
    public val maxTileBytes: ULong,
    public val maxArchiveBytes: ULong,
) {
    /** Creates limits with the documented default values. */
    public constructor() :
        this(
            maxRootDirectoryBytes = MAX_CANONICAL_ROOT_DIRECTORY_BYTES.toULong(),
            maxMetadataBytes = (16 * 1024 * 1024).toULong(),
            maxDirectoryBytes = (16 * 1024 * 1024).toULong(),
            maxTileBytes = (64 * 1024 * 1024).toULong(),
            maxArchiveBytes = ULong.MAX_VALUE,
        )

    init {
        require(maxRootDirectoryBytes in 1uL..MAX_CANONICAL_ROOT_DIRECTORY_BYTES.toULong()) {
            "maxRootDirectoryBytes must be between 1 and $MAX_CANONICAL_ROOT_DIRECTORY_BYTES."
        }
        require(maxMetadataBytes > 0uL) { "maxMetadataBytes must be positive." }
        require(maxDirectoryBytes > 0uL) { "maxDirectoryBytes must be positive." }
        require(maxTileBytes > 0uL) { "maxTileBytes must be positive." }
        require(maxArchiveBytes > 0uL) { "maxArchiveBytes must be positive." }
    }

    /** Returns a mutable builder initialized from these limits. */
    @ShouldRefineInSwift public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [ArchiveWriteLimits]. */
    public class Builder public constructor() {
        private val defaults: ArchiveWriteLimits = ArchiveWriteLimits()

        /** Maximum compressed root directory bytes. */
        public var maxRootDirectoryBytes: ULong = defaults.maxRootDirectoryBytes

        /** Maximum metadata bytes before and after internal compression. */
        public var maxMetadataBytes: ULong = defaults.maxMetadataBytes

        /** Maximum directory bytes before and after internal compression. */
        public var maxDirectoryBytes: ULong = defaults.maxDirectoryBytes

        /** Maximum tile payload bytes before and after tile compression. */
        public var maxTileBytes: ULong = defaults.maxTileBytes

        /** Maximum final archive bytes. */
        public var maxArchiveBytes: ULong = defaults.maxArchiveBytes

        internal constructor(limits: ArchiveWriteLimits) : this() {
            maxRootDirectoryBytes = limits.maxRootDirectoryBytes
            maxMetadataBytes = limits.maxMetadataBytes
            maxDirectoryBytes = limits.maxDirectoryBytes
            maxTileBytes = limits.maxTileBytes
            maxArchiveBytes = limits.maxArchiveBytes
        }

        /** Builds immutable limits from this builder. */
        public fun build(): ArchiveWriteLimits =
            ArchiveWriteLimits(
                maxRootDirectoryBytes = maxRootDirectoryBytes,
                maxMetadataBytes = maxMetadataBytes,
                maxDirectoryBytes = maxDirectoryBytes,
                maxTileBytes = maxTileBytes,
                maxArchiveBytes = maxArchiveBytes,
            )
    }

    /** Factory for Kotlin DSL construction. */
    public companion object {
        /** Builds [ArchiveWriteLimits] with a Kotlin DSL. */
        @HiddenFromObjC
        public fun build(configure: Builder.() -> Unit): ArchiveWriteLimits =
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
    @ShouldRefineInSwift public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [TileReadCoalescing]. */
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
    @ShouldRefineInSwift public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [ArchiveOpenOptions]. */
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
        @ShouldRefineInSwift
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

/**
 * Options used when writing a PMTiles archive.
 *
 * @property internalCompression Compression used for metadata and directories. Defaults to
 *   [CompressionCodes.None].
 * @property tileCompression Compression represented by stored tile payload bytes. Defaults to
 *   [CompressionCodes.None].
 * @property limits Operational write limits. Defaults to [ArchiveWriteLimits].
 * @property deduplicateTilePayloads Whether identical tile payloads should be stored once. Defaults
 *   to true.
 */
public class ArchiveWriteOptions
private constructor(
    @ShouldRefineInSwift public val internalCompression: CompressionCode,
    @ShouldRefineInSwift public val tileCompression: CompressionCode,
    public val limits: ArchiveWriteLimits,
    public val deduplicateTilePayloads: Boolean,
    internal val compressors: Map<CompressionCode, Compressor>,
) {
    /** Creates options with the documented default values. */
    public constructor() :
        this(
            internalCompression = CompressionCodes.None,
            tileCompression = CompressionCodes.None,
            limits = ArchiveWriteLimits(),
            deduplicateTilePayloads = true,
            compressors = emptyMap(),
        )

    /** Returns a mutable builder initialized from these options. */
    @ShouldRefineInSwift public fun toBuilder(): Builder = Builder(this)

    /** Mutable Kotlin builder for [ArchiveWriteOptions]. */
    public class Builder public constructor() {
        private val defaults: ArchiveWriteOptions = ArchiveWriteOptions()

        /** Compression used for metadata and directories. */
        public var internalCompression: CompressionCode = defaults.internalCompression

        /** Compression represented by stored tile payload bytes. */
        public var tileCompression: CompressionCode = defaults.tileCompression

        /** Operational write limits. */
        public var limits: ArchiveWriteLimits = defaults.limits

        /** Whether identical tile payloads should be stored once. */
        public var deduplicateTilePayloads: Boolean = defaults.deduplicateTilePayloads

        private val compressors: MutableMap<CompressionCode, Compressor> =
            defaults.compressors.toMutableMap()

        internal constructor(options: ArchiveWriteOptions) : this() {
            internalCompression = options.internalCompression
            tileCompression = options.tileCompression
            limits = options.limits
            deduplicateTilePayloads = options.deduplicateTilePayloads
            compressors += options.compressors
        }

        /** Registers [compressor] for [compression]. */
        @ShouldRefineInSwift
        public fun compressor(
            compression: CompressionCode,
            compressor: Compressor,
        ): Builder = apply {
            compressors[compression] = compressor
        }

        /** Builds immutable options from this builder. */
        public fun build(): ArchiveWriteOptions =
            ArchiveWriteOptions(
                internalCompression = internalCompression,
                tileCompression = tileCompression,
                limits = limits,
                deduplicateTilePayloads = deduplicateTilePayloads,
                compressors = compressors.toMap(),
            )
    }

    /** Factory for Kotlin DSL construction. */
    public companion object {
        /** Builds [ArchiveWriteOptions] with a Kotlin DSL. */
        @HiddenFromObjC
        public fun build(configure: Builder.() -> Unit): ArchiveWriteOptions =
            Builder().apply(configure).build()
    }
}

private const val MIN_INITIAL_READ_BYTES = 16 * 1024
private const val MAX_CANONICAL_ROOT_DIRECTORY_BYTES = 16 * 1024 - 127
