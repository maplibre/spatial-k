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

    func testByteDataSink() async throws {
        // --8<-- [start:byteDataSink]
        final class InMemoryByteSink: ByteDataSink {
            private(set) var data = Data()
            private(set) var isFlushed = false
            private(set) var isClosed = false

            func write(data: Data) async throws {
                self.data.append(data)
            }

            func flush() async throws {
                isFlushed = true
            }

            func close() async throws {
                isClosed = true
            }
        }
        // --8<-- [end:byteDataSink]

        let sink = InMemoryByteSink()
        let tile =
            ArchiveWriteTile.stored(
                coord: try TileCoord(z: 0, x: 0, y: 0),
                data: Data([1, 2, 3])
            )

        try await PmTiles.write(sink: sink, tiles: [tile])

        XCTAssertFalse(sink.data.isEmpty)
        XCTAssertTrue(sink.isFlushed)
        XCTAssertTrue(sink.isClosed)
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
        _ = TileTypeCode.mvt
        _ = CompressionCode.gzip
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
        _ = CompressionCode.none
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

    func testWriteArchive() async throws {
        // --8<-- [start:writeArchive]
        let tile =
            ArchiveWriteTile.stored(
                coord: try TileCoord(z: 0, x: 0, y: 0),
                data: Data([0x89, 0x50, 0x4E, 0x47])
            )
        let config =
            ArchiveWriteConfig.build { config in
                config.setTileType(.png)
                config.metadataJson = #"{"name":"demo"}"#
            }

        let archiveData =
            try await PmTiles.writeToData(
                tiles: [tile],
                config: config
            )
        // --8<-- [end:writeArchive]

        let archive =
            try await PmTiles.open(source: TestByteRangeDataSource(data: archiveData))
        defer { archive.close() }
        let storedTile = try await archive.readStoredTile(z: 0, x: 0, y: 0)
        XCTAssertTrue(archive.tileType == .png)
        XCTAssertNotNil(storedTile)
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
                options.decompressor(.brotli, BrotliDecompressor())
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

    func testCustomCompressor() async throws {
        // --8<-- [start:customCompressor]
        final class PassthroughCompressor: DataCompressor {
            func compress(
                data: Data,
                limits: CompressionLimits
            ) async throws -> Data {
                if UInt64(data.count) > limits.maxCompressedBytes {
                    throw PmTilesException(
                        code: PmTilesErrorCode.limitExceeded,
                        message: "Encoded output exceeds \(limits.maxCompressedBytes) bytes."
                    ).asError()
                }
                return data
            }
        }

        let options =
            ArchiveWriteOptions.build { options in
                options.setInternalCompression(.brotli)
                options.compressor(.brotli, PassthroughCompressor())
            }

        let archiveData =
            try await PmTiles.writeToData(
                tiles: [
                    ArchiveWriteTile.stored(
                        coord: try TileCoord(z: 0, x: 0, y: 0),
                        data: Data([1, 2, 3])
                    )
                ],
                options: options
            )
        // --8<-- [end:customCompressor]

        XCTAssertFalse(archiveData.isEmpty)
        _ = options.configured { options in
            options.setTileCompression(.gzip)
            options.deduplicateTilePayloads = false
        }
        _ = ArchiveWriteLimits.build { limits in
            limits.maxRootDirectoryBytes = 256
        }
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
                options.decompressor(.gzip, ThrowingDecompressor())
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
                options.decompressor(.gzip, LimitExceededDecompressor())
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
                options.decompressor(.gzip, decompressor)
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
