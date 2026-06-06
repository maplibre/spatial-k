import Foundation
import SpatialKPmtiles
import XCTest

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

final class SwiftDocsTest: XCTestCase {
    func testByteRangeDataSource() async throws {
        let pmTilesData = try fixtureData("pmtiles-js-test-fixture-1")

        // --8<-- [start:byteRangeDataSource]
        final class DataByteRangeSource: ByteRangeDataSource {
            private let data: Data

            init(data: Data) {
                self.data = data
            }

            func size() async throws -> UInt64 {
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
            try await PmTiles.open(source: source)
        archive.close()
    }

    func testOpenArchive() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("protomaps-vector-odbl-firenze"))

        // --8<-- [start:openArchive]
        let archive =
            try await PmTiles.open(source: source)
        defer { archive.close() }

        let header = archive.header
        let metadata = try await archive.metadata()
        let tile = try await archive.readStoredTile(z: 0, x: 0, y: 0)
        let payload = tile?.payload
        let tileRange = try await archive.findTileRange(z: 0, x: 0, y: 0)
        // --8<-- [end:openArchive]

        let coord = try TileCoord(z: 0, x: 0, y: 0)
        _ = header
        _ = metadata
        _ = TileTypeCodes.shared.mvt
        _ = CompressionCodes.shared.gzip
        XCTAssertNotNil(tile)
        XCTAssertNotNil(payload)
        let tileById = try await archive.readStoredTile(tileId: 0)
        let tileRangeByCoord = try await archive.findTileRange(coord: coord)
        let tileRangeById = try await archive.findTileRange(tileId: 0)
        XCTAssertNotNil(tileById)
        _ = tileRange
        _ = tileRangeByCoord
        _ = tileRangeById
        _ = try coord.toTileId()
    }

    func testDecompressedTiles() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        // --8<-- [start:decompressedTiles]
        let archive =
            try await PmTiles.open(source: source)
        defer { archive.close() }

        let tile = try await archive.readDecompressedTile(z: 0, x: 0, y: 0)
        // --8<-- [end:decompressedTiles]

        XCTAssertNotNil(tile)
        _ = CompressionCodes.shared.none
        let tileById = try await archive.readDecompressedTile(tileId: 0)
        XCTAssertNotNil(tileById)
    }

    func testBatchTiles() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("go-pmtiles-unclustered"))

        // --8<-- [start:batchTiles]
        let archive =
            try await PmTiles.open(source: source)
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

        XCTAssertFalse(results.isEmpty)
        _ = TileReadCoalescing.build { coalescing in
            coalescing.maxCoalescedBytes = 0
            coalescing.maxGapBytes = 0
        }
        let coalescing =
            TileReadCoalescing.build { coalescing in
                coalescing.maxGapBytes = 64
                coalescing.maxCoalescedBytes = 2048
            }
        _ = coalescing
    }

    func testCustomDecompressor() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        // --8<-- [start:customDecompressor]
        final class BrotliDecompressor: DataDecompressor {
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
            ArchiveOpenOptions.build { options in
                options.decompressor(CompressionCodes.shared.brotli, BrotliDecompressor())
            }

        let archive = try await PmTiles.open(source: source, options: options)
        defer { archive.close() }

        let tile = try await archive.readDecompressedTile(z: 0, x: 0, y: 0)
        // --8<-- [end:customDecompressor]

        XCTAssertNotNil(tile)
        let lenientOptions = options.configured { options in
            options.validationMode = ValidationMode.lenient
        }
        let limitedOptions = options.configured { options in
            options.limits = ArchiveLimits()
        }
        let combinedOptions = options.configured { options in
            options.validationMode = ValidationMode.lenient
            options.limits = ArchiveLimits()
        }
        let metadataLimitedOptions =
            options.configured { options in
                options.limits = ArchiveLimits.build { limits in
                    limits.maxMetadataBytes = 1024
                }
            }
        let directoryLimitedOptions =
            options.configured { options in
                options.limits = ArchiveLimits.build { limits in
                    limits.maxDirectoryDecompressedBytes = 2048
                }
            }
        _ = lenientOptions
        _ = limitedOptions
        _ = combinedOptions
        _ = metadataLimitedOptions
        _ = directoryLimitedOptions
    }

    func testCustomDecompressorThrowsSwiftErrorAtOpen() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        final class ThrowingDecompressor: DataDecompressor {
            func decompress(
                data: Data,
                limits: DecompressionLimits
            ) async throws -> Data {
                throw NSError(domain: "SpatialKPmtilesSwiftTests", code: 1)
            }
        }

        let options =
            ArchiveOpenOptions.build { options in
                options.decompressor(CompressionCodes.shared.gzip, ThrowingDecompressor())
            }
        do {
            _ = try await PmTiles.open(source: source, options: options)
            XCTFail("Expected custom decompressor to throw.")
        } catch {
            let pmTilesError = try XCTUnwrap((error as NSError).kotlinException as? PmTilesException)
            XCTAssertTrue(pmTilesError.code == PmTilesErrorCode.decompressionFailed)
        }
    }

    func testCustomDecompressorThrowsPmTilesErrorAtOpen() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        final class LimitExceededDecompressor: DataDecompressor {
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
            ArchiveOpenOptions.build { options in
                options.decompressor(CompressionCodes.shared.gzip, LimitExceededDecompressor())
            }

        do {
            _ = try await PmTiles.open(source: source, options: options)
            XCTFail("Expected custom decompressor to throw.")
        } catch {
            let pmTilesError = try XCTUnwrap((error as NSError).kotlinException as? PmTilesException)
            XCTAssertTrue(pmTilesError.code == PmTilesErrorCode.limitExceeded)
        }
    }

    func testCustomDecompressorReceivesUInt64Limits() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-1"))

        final class RecordingDecompressor: DataDecompressor {
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
            ArchiveOpenOptions.build { options in
                options.decompressor(CompressionCodes.shared.gzip, decompressor)
            }

        do {
            _ = try await PmTiles.open(source: source, options: options)
            XCTFail("Expected custom decompressor to throw.")
        } catch {
            let pmTilesError = try XCTUnwrap((error as NSError).kotlinException as? PmTilesException)
            XCTAssertTrue(pmTilesError.code == PmTilesErrorCode.limitExceeded)
        }

        XCTAssertNotNil(decompressor.maxCompressedBytes)
        XCTAssertNotNil(decompressor.maxDecompressedBytes)
    }

    func testLenientWarnings() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-test-fixture-mlt"))

        // --8<-- [start:lenientWarnings]
        let archive =
            try await PmTiles.open(
                source: source,
                options: ArchiveOpenOptions.build { options in
                    options.validationMode = ValidationMode.lenient
                }
            )
        defer { archive.close() }

        let warnings = archive.warnings
        // --8<-- [end:lenientWarnings]

        XCTAssertFalse(warnings.isEmpty)
        _ = ArchiveOpenOptions.build { options in
            options.validationMode = ValidationMode.lenient
        }
    }

    func testThrowsSwiftError() async throws {
        let source = TestByteRangeDataSource(data: try fixtureData("pmtiles-js-invalid"))

        do {
            _ =
                try await PmTiles.open(source: source)
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

private final class TestByteRangeDataSource: ByteRangeDataSource {
    private let data: Data
    private(set) var readCount = 0

    init(data: Data) {
        self.data = data
    }

    func size() async throws -> UInt64 {
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
