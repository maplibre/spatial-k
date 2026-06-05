import Foundation
import SpatialKPmtiles
import XCTest

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

final class SwiftDocsTest: XCTestCase {
    func testByteRangeDataSource() async throws {
        let pmTilesData = try fixtureData("pmtiles-js-test-fixture-1")

        // --8<-- [start:byteRangeDataSource]
        final class DataByteRangeSource: NSObject, ByteRangeDataSource {
            private let data: Data

            init(data: Data) {
                self.data = data
            }

            func size() -> UInt64 {
                UInt64(data.count)
            }

            func read(offset: UInt64, length: UInt64) async throws -> Data {
                let start = Int(offset)
                let end = start + Int(length)
                return data.subdata(in: start..<end)
            }
        }
        // --8<-- [end:byteRangeDataSource]

        let source = DataByteRangeSource(data: pmTilesData)
        let archive =
            try await PmTiles.shared.open(source: source)
        archive.close()

        XCTAssertEqual(pmTilesData.count, 468)
    }

    func testOpenArchive() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("protomaps-vector-odbl-firenze"))

        // --8<-- [start:openArchive]
        let archive =
            try await PmTiles.shared.open(source: source)
        defer { archive.close() }

        let header = archive.header
        let metadata = try await archive.metadata()
        let tile = try await archive.readStoredTile(z: 0, x: 0, y: 0)
        let tileRange = try await archive.findTileRange(z: 0, x: 0, y: 0)
        // --8<-- [end:openArchive]

        let coord = try TileCoord(z: 0, x: 0, y: 0)
        XCTAssertEqual(header.minZoom, 0)
        XCTAssertTrue(header.isClustered)
        XCTAssertEqual(header.counts.addressedTiles, 92)
        XCTAssertEqual(header.tileType, TileTypeCodes.shared.mvt)
        XCTAssertEqual(header.tileCompression, CompressionCodes.shared.gzip)
        XCTAssertEqual(metadata.name, "Protomaps Basemap")
        XCTAssertNotNil(metadata.summary)
        XCTAssertTrue(metadata.type?.known == KnownTilesetKind.baseLayer)
        XCTAssertEqual(TileTypeCodes.shared.mvt, 1)
        XCTAssertEqual(UInt32(99), 99)
        XCTAssertTrue(TilesetKind(known: KnownTilesetKind.baseLayer).known == KnownTilesetKind.baseLayer)
        XCTAssertNil(TilesetKind(value: "custom").known)
        XCTAssertNotNil(tile)
        XCTAssertNotNil(tileRange)
        let tileById = try await archive.readStoredTile(tileId: 0)
        let tileRangeByCoord = try await archive.findTileRange(coord: coord)
        let tileRangeById = try await archive.findTileRange(tileId: 0)
        XCTAssertNotNil(tileById)
        XCTAssertNotNil(tileRangeByCoord)
        XCTAssertEqual(tileRangeByCoord?.archiveRange, tileRangeById?.archiveRange)
        XCTAssertEqual(tile, tileById)
        let unwrappedTile = try XCTUnwrap(tile)
        XCTAssertEqual(unwrappedTile.byteCount, UInt64(unwrappedTile.data.count))
        let unwrappedRange = try XCTUnwrap(tileRange)
        XCTAssertEqual(unwrappedRange.archiveRange.length, UInt64(unwrappedTile.data.count))
        XCTAssertEqual(try coord.toTileId(), 0)
        let coordFromId = try TileCoord(tileId: 0)
        XCTAssertEqual(coordFromId.z, 0)
        XCTAssertEqual(coordFromId.x, 0)
        XCTAssertEqual(coordFromId.y, 0)
    }

    func testDecompressedTiles() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        // --8<-- [start:decompressedTiles]
        let archive =
            try await PmTiles.shared.open(source: source)
        defer { archive.close() }

        let tile = try await archive.readDecompressedTile(z: 0, x: 0, y: 0)
        // --8<-- [end:decompressedTiles]

        let unwrappedTile = try XCTUnwrap(tile)
        XCTAssertEqual(unwrappedTile.compression, CompressionCodes.shared.none)
        XCTAssertEqual(unwrappedTile.data.firstByte, 0x1a)
        let tileById = try await archive.readDecompressedTile(tileId: 0)
        XCTAssertNotNil(tileById)
    }

    func testBatchTiles() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("go-pmtiles-unclustered"))

        // --8<-- [start:batchTiles]
        let archive =
            try await PmTiles.shared.open(source: source)
        defer { archive.close() }

        let coords = [
            try TileCoord(z: 1, x: 0, y: 0),
            try TileCoord(z: 1, x: 0, y: 1),
        ]
        let results =
            try await archive.readStoredTiles(
                coords: coords
            )
        // --8<-- [end:batchTiles]

        XCTAssertEqual(results.count, 2)
        XCTAssertTrue(results.allSatisfy(\.isFound))
        let defaultCoalescedBytes: UInt64 = TileReadCoalescing().maxCoalescedBytes
        XCTAssertEqual(defaultCoalescedBytes, 512 * 1024)
        XCTAssertEqual(TileReadCoalescing(maxCoalescedBytes: 0, maxGapBytes: 0).maxCoalescedBytes, 0)
        let coalescing =
            TileReadCoalescing()
                .with(maxGapBytes: 64)
                .with(maxCoalescedBytes: 2048)
        let maxGapBytes: UInt64 = coalescing.maxGapBytes
        XCTAssertEqual(coalescing.maxCoalescedBytes, 2048)
        XCTAssertEqual(maxGapBytes, 64)
    }

    func testCustomDecompressor() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        // --8<-- [start:customDecompressor]
        final class BrotliDecompressor: NSObject, DataDecompressor {
            func decompress(
                data: Data,
                limits: DecompressionLimits
            ) async throws -> Data {
                let decoded = decodeBrotli(data)
                if UInt64(decoded.count) > limits.maxDecompressedBytes {
                    throw PmTilesException(
                        code: PmTilesErrorCode.limitExceeded,
                        message: "Decoded output exceeds \(limits.maxDecompressedBytes) bytes."
                    ).asError()
                }
                return decoded
            }
        }

        let options =
            ArchiveOpenOptions().withDecompressor(
                compression: CompressionCodes.shared.brotli,
                decompressor: BrotliDecompressor()
            )

        let archive = try await PmTiles.shared.open(source: source, options: options)
        defer { archive.close() }

        let tile = try await archive.readDecompressedTile(z: 0, x: 0, y: 0)
        // --8<-- [end:customDecompressor]

        XCTAssertNotNil(tile)
        XCTAssertTrue(options.validationMode == ValidationMode.strict)
        XCTAssertEqual(CompressionCodes.shared.brotli, 3)
        let lenientOptions = options.with(validationMode: ValidationMode.lenient)
        let limitedOptions = options.with(limits: ArchiveLimits())
        let combinedOptions = options.with(validationMode: ValidationMode.lenient, limits: ArchiveLimits())
        let metadataLimitedOptions =
            options.with(limits: ArchiveLimits().with(maxMetadataBytes: 1024))
        let directoryLimitedOptions =
            options.with(
                limits: ArchiveLimits()
                    .with(maxDirectoryEntries: 16)
                    .with(maxDirectoryDecompressedBytes: 2048)
            )
        XCTAssertTrue(lenientOptions.validationMode == ValidationMode.lenient)
        XCTAssertEqual(limitedOptions.limits.maxInitialReadBytes, 16 * 1024)
        XCTAssertTrue(combinedOptions.validationMode == ValidationMode.lenient)
        XCTAssertEqual(ArchiveLimits().maxInitialReadBytes, 16 * 1024)
        let maxMetadataBytes: UInt64 = metadataLimitedOptions.limits.maxMetadataBytes
        let maxDirectoryDecompressedBytes: UInt64 =
            directoryLimitedOptions.limits.maxDirectoryDecompressedBytes
        XCTAssertEqual(maxMetadataBytes, 1024)
        XCTAssertEqual(maxDirectoryDecompressedBytes, 2048)
        XCTAssertEqual(directoryLimitedOptions.limits.maxDirectoryEntries, 16)
    }

    func testCustomDecompressorThrowsSwiftErrorAtOpen() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        final class ThrowingDecompressor: NSObject, DataDecompressor {
            func decompress(
                data: Data,
                limits: DecompressionLimits
            ) async throws -> Data {
                throw NSError(domain: "SpatialKPmtilesSwiftTests", code: 1)
            }
        }

        let options =
            ArchiveOpenOptions().withDecompressor(
                compression: CompressionCodes.shared.gzip,
                decompressor: ThrowingDecompressor()
            )
        do {
            _ = try await PmTiles.shared.open(source: source, options: options)
            XCTFail("Expected custom decompressor to throw.")
        } catch {
            let pmTilesError = try XCTUnwrap((error as NSError).kotlinException as? PmTilesException)
            XCTAssertTrue(pmTilesError.code == PmTilesErrorCode.decompressionFailed)
        }
    }

    func testCustomDecompressorThrowsPmTilesErrorAtOpen() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        final class LimitExceededDecompressor: NSObject, DataDecompressor {
            func decompress(
                data: Data,
                limits: DecompressionLimits
            ) async throws -> Data {
                throw PmTilesException(
                    code: PmTilesErrorCode.limitExceeded,
                    message: "Decoded output exceeds \(limits.maxDecompressedBytes) bytes."
                ).asError()
            }
        }

        let options =
            ArchiveOpenOptions().withDecompressor(
                compression: CompressionCodes.shared.gzip,
                decompressor: LimitExceededDecompressor()
            )

        do {
            _ = try await PmTiles.shared.open(source: source, options: options)
            XCTFail("Expected custom decompressor to throw.")
        } catch {
            let pmTilesError = try XCTUnwrap((error as NSError).kotlinException as? PmTilesException)
            XCTAssertTrue(pmTilesError.code == PmTilesErrorCode.limitExceeded)
        }
    }

    func testCustomDecompressorReceivesUInt64Limits() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        final class RecordingDecompressor: NSObject, DataDecompressor {
            private(set) var maxCompressedBytes: UInt64?
            private(set) var maxDecompressedBytes: UInt64?

            func decompress(
                data: Data,
                limits: DecompressionLimits
            ) async throws -> Data {
                maxCompressedBytes = limits.maxCompressedBytes
                maxDecompressedBytes = limits.maxDecompressedBytes
                throw PmTilesException(
                    code: PmTilesErrorCode.limitExceeded,
                    message: "Stop after recording decompression limits."
                ).asError()
            }
        }

        let decompressor = RecordingDecompressor()
        let options =
            ArchiveOpenOptions().withDecompressor(
                compression: CompressionCodes.shared.gzip,
                decompressor: decompressor
            )

        do {
            _ = try await PmTiles.shared.open(source: source, options: options)
            XCTFail("Expected custom decompressor to throw.")
        } catch {
            let pmTilesError = try XCTUnwrap((error as NSError).kotlinException as? PmTilesException)
            XCTAssertTrue(pmTilesError.code == PmTilesErrorCode.limitExceeded)
        }

        XCTAssertEqual(
            decompressor.maxCompressedBytes,
            UInt64(ArchiveLimits().maxDirectoryCompressedBytes)
        )
        XCTAssertEqual(
            decompressor.maxDecompressedBytes,
            UInt64(ArchiveLimits().maxDirectoryDecompressedBytes)
        )
    }

    func testLenientWarnings() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-mlt"))

        // --8<-- [start:lenientWarnings]
        let archive =
            try await PmTiles.shared.open(
                source: source,
                options: ArchiveOpenOptions().with(validationMode: ValidationMode.lenient)
            )
        defer { archive.close() }

        let warnings = archive.warnings
        // --8<-- [end:lenientWarnings]

        XCTAssertTrue(warnings.contains { $0.code == ArchiveWarningCode.emptyRootDirectory })
        XCTAssertTrue(ArchiveOpenOptions().validationMode == ValidationMode.strict)
        XCTAssertTrue(ArchiveOpenOptions().with(validationMode: ValidationMode.lenient).validationMode == ValidationMode.lenient)
    }

    func testThrowsSwiftError() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-invalid"))

        do {
            _ =
                try await PmTiles.shared.open(source: source)
            XCTFail("Expected invalid archive to throw.")
        } catch {
            let pmTilesError = try XCTUnwrap((error as NSError).kotlinException as? PmTilesException)
            XCTAssertTrue(pmTilesError.code == PmTilesErrorCode.invalidMagic)
        }
    }

    func testInvalidTileCoordThrowsSwiftError() throws {
        do {
            _ = try TileCoord(z: 1, x: 2, y: 0)
            XCTFail("Expected invalid tile coordinate to throw.")
        } catch {
            let pmTilesError = try XCTUnwrap((error as NSError).kotlinException as? PmTilesException)
            XCTAssertTrue(pmTilesError.code == PmTilesErrorCode.invalidTileCoordinate)
        }
    }
}

private final class TestByteRangeDataSource: NSObject, ByteRangeDataSource {
    private let data: Data
    private(set) var readCount = 0

    init(data: Data) {
        self.data = data
    }

    func size() -> UInt64 {
        UInt64(data.count)
    }

    func read(offset: UInt64, length: UInt64) async throws -> Data {
        readCount += 1
        let start = Int(offset)
        let end = start + Int(length)
        return data.subdata(in: start..<end)
    }
}

private func fixtureData(_ name: String) throws -> Data {
    let url = try XCTUnwrap(Bundle.module.url(forResource: name, withExtension: "pmtiles"))
    return try Data(contentsOf: url)
}

private func decodeBrotli(_ data: Data) -> Data {
    data
}

private extension Data {
    var firstByte: UInt8? {
        first
    }
}
