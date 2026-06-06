@_exported import SpatialKPmtilesKotlin
import Foundation

public protocol ByteRangeDataSource {
    /// Returns the stable archive size in bytes.
    ///
    /// Implementations may be called from multiple Swift tasks at the same time and should be safe
    /// for concurrent use.
    func size() async throws -> UInt64

    /// Reads exactly `length` bytes starting at `offset`.
    ///
    /// Implementations may be called from multiple Swift tasks at the same time and should be safe
    /// for concurrent use.
    func read(offset: UInt64, length: UInt64) async throws -> Data
}

public protocol DataDecompressor {
    /// Returns decompressed data for one PMTiles compression format.
    ///
    /// Implementations may be called from multiple Swift tasks at the same time and should be safe
    /// for concurrent use.
    func decompress(data: Data, limits: DecompressionLimits) async throws -> Data
}

public extension PmTiles {
    static func open(
        source: ByteRangeDataSource,
        options: ArchiveOpenOptions = ArchiveOpenOptions()
    ) async throws -> PmTilesArchive {
        try await shared.__open(
            SwiftByteRangeSourceAdapter(source: source),
            options: options
        )
    }
}

public extension ArchiveOpenOptions {
    static func build(
        _ configure: (ArchiveOpenOptions.Builder) -> Void
    ) -> ArchiveOpenOptions {
        ArchiveOpenOptions().configured(configure)
    }

    func configured(
        _ configure: (ArchiveOpenOptions.Builder) -> Void
    ) -> ArchiveOpenOptions {
        let builder = __toBuilder()
        configure(builder)
        return builder.build()
    }
}

public extension ArchiveLimits {
    static func build(
        _ configure: (ArchiveLimits.Builder) -> Void
    ) -> ArchiveLimits {
        ArchiveLimits().configured(configure)
    }

    func configured(
        _ configure: (ArchiveLimits.Builder) -> Void
    ) -> ArchiveLimits {
        let builder = __toBuilder()
        configure(builder)
        return builder.build()
    }
}

public extension TileReadCoalescing {
    static func build(
        _ configure: (TileReadCoalescing.Builder) -> Void
    ) -> TileReadCoalescing {
        TileReadCoalescing().configured(configure)
    }

    func configured(
        _ configure: (TileReadCoalescing.Builder) -> Void
    ) -> TileReadCoalescing {
        let builder = __toBuilder()
        configure(builder)
        return builder.build()
    }
}

public extension ArchiveOpenOptions.Builder {
    func decompressor(
        _ compression: UInt32,
        _ decompressor: DataDecompressor
    ) {
        _ = __decompressorCompression(
            compression,
            decompressor: SwiftDataDecompressorAdapter(decompressor: decompressor)
        )
    }
}

public extension PmTilesArchive {
    func readStoredTiles(
        coords: [TileCoord],
        coalescing: TileReadCoalescing = TileReadCoalescing()
    ) async throws -> [TileReadResult] {
        try await __readStoredTilesCoords(coords, coalescing: coalescing)
    }

    func readDecompressedTiles(
        coords: [TileCoord],
        coalescing: TileReadCoalescing = TileReadCoalescing()
    ) async throws -> [TileReadResult] {
        try await __readDecompressedTilesCoords(coords, coalescing: coalescing)
    }
}

public extension ArchiveTile {
    var payload: Data {
        __payload.toNSData() as Data
    }
}

private final class SwiftByteRangeSourceAdapter: NSObject, KotlinByteRangeSource {
    private let source: ByteRangeDataSource

    init(source: ByteRangeDataSource) {
        self.source = source
    }

    func size() async throws -> KotlinULong {
        KotlinULong(unsignedLongLong: try await source.size())
    }

    func read(range: ByteRange) async throws -> ByteString {
        let data =
            try await source.read(
                offset: range.offset,
                length: range.length
            )
        return ByteStringAppleKt.toByteString(data)
    }
}

private final class SwiftDataDecompressorAdapter: NSObject, KotlinDecompressor {
    private let decompressor: DataDecompressor

    init(decompressor: DataDecompressor) {
        self.decompressor = decompressor
    }

    func decompress(
        bytes: ByteString,
        limits: DecompressionLimits
    ) async throws -> ByteString {
        let decompressed =
            try await decompressor.decompress(
                data: bytes.toNSData() as Data,
                limits: limits
            )
        return ByteStringAppleKt.toByteString(decompressed)
    }
}
