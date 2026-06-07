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

public protocol ByteDataSink {
    /// Appends bytes to the output in write order.
    func write(data: Data) async throws

    /// Flushes pending output.
    func flush() async throws

    /// Closes the output.
    func close() async throws
}

public protocol DataCompressor {
    /// Returns compressed data for one PMTiles compression format.
    func compress(data: Data, limits: CompressionLimits) async throws -> Data
}

public struct CompressionCode: Hashable, RawRepresentable, Sendable {
    public let rawValue: UInt32

    public init(rawValue: UInt32) {
        self.rawValue = rawValue
    }

    public static let unknown = CompressionCode(rawValue: CompressionCodes.shared.__unknown)
    public static let none = CompressionCode(rawValue: CompressionCodes.shared.__none)
    public static let gzip = CompressionCode(rawValue: CompressionCodes.shared.__gzip)
    public static let brotli = CompressionCode(rawValue: CompressionCodes.shared.__brotli)
    public static let zstd = CompressionCode(rawValue: CompressionCodes.shared.__zstd)
}

public struct TileTypeCode: Hashable, RawRepresentable, Sendable {
    public let rawValue: UInt32

    public init(rawValue: UInt32) {
        self.rawValue = rawValue
    }

    public static let unknown = TileTypeCode(rawValue: TileTypeCodes.shared.__unknown)
    public static let mvt = TileTypeCode(rawValue: TileTypeCodes.shared.__mvt)
    public static let png = TileTypeCode(rawValue: TileTypeCodes.shared.__png)
    public static let jpeg = TileTypeCode(rawValue: TileTypeCodes.shared.__jpeg)
    public static let webp = TileTypeCode(rawValue: TileTypeCodes.shared.__webp)
    public static let avif = TileTypeCode(rawValue: TileTypeCodes.shared.__avif)
    public static let mlt = TileTypeCode(rawValue: TileTypeCodes.shared.__mlt)
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

    static func write(
        sink: ByteDataSink,
        tiles: [ArchiveWriteTile],
        config: ArchiveWriteConfig = ArchiveWriteConfig(),
        options: ArchiveWriteOptions = ArchiveWriteOptions()
    ) async throws {
        try await shared.__write(
            SwiftByteSinkAdapter(sink: sink),
            tiles: tiles,
            config: config,
            options: options
        )
    }

    static func writeToData(
        tiles: [ArchiveWriteTile],
        config: ArchiveWriteConfig = ArchiveWriteConfig(),
        options: ArchiveWriteOptions = ArchiveWriteOptions()
    ) async throws -> Data {
        let bytes =
            try await shared.__write(
                toByteStringTiles: tiles,
                config: config,
                options: options
            )
        return bytes.toNSData() as Data
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

public extension ArchiveWriteConfig {
    static func build(
        _ configure: (ArchiveWriteConfig.Builder) -> Void
    ) -> ArchiveWriteConfig {
        ArchiveWriteConfig().configured(configure)
    }

    func configured(
        _ configure: (ArchiveWriteConfig.Builder) -> Void
    ) -> ArchiveWriteConfig {
        let builder = __toBuilder()
        configure(builder)
        return builder.build()
    }

    var tileType: TileTypeCode {
        TileTypeCode(rawValue: __tileType)
    }
}

public extension ArchiveWriteConfig.Builder {
    func setTileType(_ tileType: TileTypeCode) {
        self.tileType = tileType.rawValue
    }
}

public extension ArchiveWriteLimits {
    static func build(
        _ configure: (ArchiveWriteLimits.Builder) -> Void
    ) -> ArchiveWriteLimits {
        ArchiveWriteLimits().configured(configure)
    }

    func configured(
        _ configure: (ArchiveWriteLimits.Builder) -> Void
    ) -> ArchiveWriteLimits {
        let builder = __toBuilder()
        configure(builder)
        return builder.build()
    }
}

public extension ArchiveWriteOptions {
    static func build(
        _ configure: (ArchiveWriteOptions.Builder) -> Void
    ) -> ArchiveWriteOptions {
        ArchiveWriteOptions().configured(configure)
    }

    func configured(
        _ configure: (ArchiveWriteOptions.Builder) -> Void
    ) -> ArchiveWriteOptions {
        let builder = __toBuilder()
        configure(builder)
        return builder.build()
    }

    var internalCompression: CompressionCode {
        CompressionCode(rawValue: __internalCompression)
    }

    var tileCompression: CompressionCode {
        CompressionCode(rawValue: __tileCompression)
    }
}

public extension ArchiveWriteOptions.Builder {
    func setInternalCompression(_ compression: CompressionCode) {
        internalCompression = compression.rawValue
    }

    func setTileCompression(_ compression: CompressionCode) {
        tileCompression = compression.rawValue
    }

    func compressor(
        _ compression: CompressionCode,
        _ compressor: DataCompressor
    ) {
        _ = __compressorCompression(
            compression.rawValue,
            compressor: SwiftDataCompressorAdapter(compressor: compressor)
        )
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

public extension ArchiveWriteTile {
    static func stored(coord: TileCoord, data: Data) -> ArchiveWriteTile {
        companion.stored(
            coord: coord,
            payload: ByteStringAppleKt.toByteString(data)
        )
    }

    static func uncompressed(coord: TileCoord, data: Data) -> ArchiveWriteTile {
        companion.uncompressed(
            coord: coord,
            payload: ByteStringAppleKt.toByteString(data)
        )
    }

    var payload: Data {
        __payload.toNSData() as Data
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

private final class SwiftByteSinkAdapter: NSObject, KotlinByteSink {
    private let sink: ByteDataSink

    init(sink: ByteDataSink) {
        self.sink = sink
    }

    func write(bytes: ByteString) async throws {
        try await sink.write(data: bytes.toNSData() as Data)
    }

    func flush() async throws {
        try await sink.flush()
    }

    func close() async throws {
        try await sink.close()
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

private final class SwiftDataCompressorAdapter: NSObject, KotlinCompressor {
    private let compressor: DataCompressor

    init(compressor: DataCompressor) {
        self.compressor = compressor
    }

    func compress(
        bytes: ByteString,
        limits: CompressionLimits
    ) async throws -> ByteString {
        let compressed =
            try await compressor.compress(
                data: bytes.toNSData() as Data,
                limits: limits
            )
        return ByteStringAppleKt.toByteString(compressed)
    }
}
