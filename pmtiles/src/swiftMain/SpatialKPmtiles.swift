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

public struct CompressionCode: Hashable, RawRepresentable, Sendable {
    public let rawValue: UInt32

    public init(rawValue: UInt32) {
        self.rawValue = rawValue
    }

    public static let unknown = CompressionCode(rawValue: 0)
    public static let none = CompressionCode(rawValue: 1)
    public static let gzip = CompressionCode(rawValue: 2)
    public static let brotli = CompressionCode(rawValue: 3)
    public static let zstd = CompressionCode(rawValue: 4)
}

public struct TileTypeCode: Hashable, RawRepresentable, Sendable {
    public let rawValue: UInt32

    public init(rawValue: UInt32) {
        self.rawValue = rawValue
    }

    public static let unknown = TileTypeCode(rawValue: 0)
    public static let mvt = TileTypeCode(rawValue: 1)
    public static let png = TileTypeCode(rawValue: 2)
    public static let jpeg = TileTypeCode(rawValue: 3)
    public static let webp = TileTypeCode(rawValue: 4)
    public static let avif = TileTypeCode(rawValue: 5)
    public static let mlt = TileTypeCode(rawValue: 6)
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
        _ compression: CompressionCode,
        _ decompressor: DataDecompressor
    ) {
        _ = __decompressorCompression(
            compression.rawValue,
            decompressor: SwiftDataDecompressorAdapter(decompressor: decompressor)
        )
    }
}

public extension ArchiveHeader {
    var internalCompression: CompressionCode {
        CompressionCode(rawValue: __internalCompression)
    }

    var tileCompression: CompressionCode {
        CompressionCode(rawValue: __tileCompression)
    }

    var tileType: TileTypeCode {
        TileTypeCode(rawValue: __tileType)
    }
}

public extension PmTilesArchive {
    var tileType: TileTypeCode {
        TileTypeCode(rawValue: __tileType)
    }

    var internalCompression: CompressionCode {
        CompressionCode(rawValue: __internalCompression)
    }

    var tileCompression: CompressionCode {
        CompressionCode(rawValue: __tileCompression)
    }

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

    var tileType: TileTypeCode {
        TileTypeCode(rawValue: __tileType)
    }

    var compression: CompressionCode {
        CompressionCode(rawValue: __compression)
    }
}

public extension TileRange {
    var tileType: TileTypeCode {
        TileTypeCode(rawValue: __tileType)
    }

    var compression: CompressionCode {
        CompressionCode(rawValue: __compression)
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
