package org.maplibre.spatialk.pmtiles

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.coroutines.runBlocking

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class PmTilesBenchmark {
    private lateinit var rootOnlyBytes: ByteArray
    private lateinit var leafDirectoryBytes: ByteArray
    private lateinit var rootOnlyArchive: PmTilesArchive
    private lateinit var leafDirectoryArchive: PmTilesArchive
    private lateinit var decompressedArchive: PmTilesArchive

    @Setup
    fun setup() {
        rootOnlyBytes = readPmTilesFixture("upstream/pmtiles-js-test-data/test-fixture-1.pmtiles")
        leafDirectoryBytes =
            readPmTilesFixture("upstream/pmtiles-spec-v3/stamen-toner-raster-cc-by-odbl-z3.pmtiles")
        rootOnlyArchive = runBlocking {
            PmTilesArchive.open(InMemoryByteRangeSource(rootOnlyBytes))
        }
        leafDirectoryArchive = runBlocking {
            PmTilesArchive.open(InMemoryByteRangeSource(leafDirectoryBytes))
        }
        decompressedArchive = runBlocking {
            PmTilesArchive.open(
                InMemoryByteRangeSource(rootOnlyBytes),
                options = ArchiveOpenOptions(tileReadMode = TileReadMode.DecompressedBytes),
            )
        }
    }

    @Benchmark
    fun openArchive() {
        runBlocking {
            PmTilesArchive.open(InMemoryByteRangeSource(rootOnlyBytes)).close()
        }
    }

    @Benchmark
    fun rootTileRangeLookup() {
        val range = runBlocking { rootOnlyArchive.getTileRange(0, 0, 0) }
        require(range != null)
    }

    @Benchmark
    fun leafTileRangeLookup() {
        val range = runBlocking { leafDirectoryArchive.getTileRange(3, 4, 3) }
        require(range != null)
    }

    @Benchmark
    fun repeatedTileRangeLookup() {
        runBlocking {
            repeat(REPEATED_LOOKUPS) {
                require(leafDirectoryArchive.getTileRange(3, 4, 3) != null)
            }
        }
    }

    @Benchmark
    fun compressedTileRead() {
        val tile = runBlocking { rootOnlyArchive.getTileCompressed(0, 0, 0) }
        require(tile != null)
    }

    @Benchmark
    fun decompressedTileRead() {
        val tile = runBlocking { decompressedArchive.getTile(0, 0, 0) }
        require(tile != null && tile.wasDecompressed)
    }
}

private class InMemoryByteRangeSource(private val bytes: ByteArray) : ByteRangeSource {
    override suspend fun size(): ULong = bytes.size.toULong()

    override suspend fun read(range: ByteRange): ByteArray {
        val start = range.offset.toInt()
        return bytes.copyOfRange(start, start + range.length)
    }
}

private const val REPEATED_LOOKUPS = 100

internal expect fun readPmTilesFixture(path: String): ByteArray
