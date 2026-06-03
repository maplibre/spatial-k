# PMTiles Release Notes

## Initial PMTiles v3 Reader

The initial `pmtiles` module is a Kotlin Multiplatform PMTiles v3 reader in package
`org.maplibre.spatialk.pmtiles`.

Included:

- Header parsing, root and leaf directory decoding, Hilbert TileID math, metadata reads, tile range
  lookup, compressed tile reads, and configured decompressed tile reads.
- Caller-provided `ByteRangeSource` as the only core IO abstraction.
- JVM and Kotlin/Native gzip decoding, including Apple targets.
- JavaScript and WASM target compilation for the reader API, with gzip decompression marked by a
  runtime `NotImplementedError`.
- Apple conveniences: `ByteRangeDataSource`, `PmTilesArchive.open(ByteRangeDataSource, ...)`, and
  `ArchiveTile.data: NSData`.
- Lenient warning access through `warningCount`, `warningAt(index)`, and Kotlin-only `warnings()`.

Unsupported in this release:

- PMTiles writing.
- PMTiles v1 or v2 reading.
- Built-in HTTP, filesystem, object-store, browser `Blob`/`File`, or Node source implementations.
- Foreign JavaScript export APIs: `@JsExport`, generated TypeScript declarations, and JavaScript
  facade APIs.
- JavaScript and WASM gzip decompression.
- Brotli or zstd decompression.
- Caller compression codec registration.
- Tile payload decoding and map renderer integration.

Release readiness artifacts:

- `pmtiles/SPEC.md` is the complete-state reader specification.
- `pmtiles/PLAN.md` is the implementation checkpoint plan.
- `pmtiles/SPEC-CHECKLIST.md` maps specification claims to implementation and tests.
- `pmtiles/fixtures/MANIFEST.md` records fixture provenance.
