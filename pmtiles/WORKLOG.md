# PMTiles Writer Work Log

## 2026-06-06 - Write-Side Binary Primitives

- Started with `PLAN.md` phase 1.1, after re-reading the related `SPEC.md` and `RESEARCH.md`
  sections.
- Added a production `BinaryWriter` instead of promoting byte-writing helpers from
  `HeaderFixtures.kt`.
- Matched existing fixture behavior for PMTiles position encoding: multiply by 10,000,000 and
  truncate toward zero.
- Added an explicit `UInt8` overflow check after noticing that a naive byte mask would silently
  corrupt header fields such as compression and tile type codes.
- Kept tests independent by asserting exact expected bytes for fixed-width values, varints, and
  position encoding.

## 2026-06-06 - Header Serialization

- Added `HeaderWriter.kt` as production internal code and kept `HeaderFixtures.kt` unchanged for
  now.
- Shared the reader's header validation by making `validateHeader` internal, avoiding duplicate
  section/root/coordinate validation.
- Header serializer tests assert independent byte slices for magic/version, offsets, counts, flags,
  codes, zooms, and position fields, then also parse the header as a round-trip check.
- The first root-location negative test accidentally failed section-layout validation first because
  the archive size was too small; adjusted it to isolate root-first-16-KiB validation.

## 2026-06-06 - Directory Encoding

- Added production raw directory encoding in `DirectoryEncoding.kt`; compression remains a later
  codec-pipeline concern.
- Kept existing decoder tests on their current fixture helper, then added encoder tests with
  independent expected byte strings plus a decoder round-trip assertion.
- Added encoder-side validation for empty inputs, negative TileIDs, non-increasing TileIDs,
  non-positive lengths, negative run lengths, and offset overflow.
- Confirmed the production `encodeDirectory(List<DirectoryEntry>)` overload does not disturb the
  existing test-only vararg helper used by decoder tests.

## 2026-06-06 - Writer Options And Compressor API

- Added `Compressor` and `CompressionLimits` parallel to the reader's `Decompressor` surface.
- Added `ArchiveWriteLimits` and `ArchiveWriteOptions` instead of overloading read options.
- Kept the compressor registry model aligned with the reader path: one registry keyed by
  `CompressionCode`, defaulting only to `CompressionCodes.None`.
- Put the canonical root-directory byte limit in write limits with the `16 * 1024 - 127` default.
- Left actual compression execution for the later compression-pipeline phase; this chunk establishes
  the public option and registration surface only.

## 2026-06-06 - Public Writer Input Models

- Added `ByteSink` as the append-only suspending output counterpart to `ByteRangeSource`; no
  random-access or `kotlinx-io` dependency was introduced.
- Added writer-specific bounds, center, config, and tile input models instead of exposing parsed
  reader result models as writer inputs.
- Made tile payload mode explicit with stored and uncompressed paths, matching the research decision
  that tile compression must not be inferred from the archive compression code.
- Added writer-facing error codes for sink failures and malformed tile input in preparation for
  archive assembly.

## 2026-06-06 - Write-Side Compression Pipeline

- Added internal compressor registry execution parallel to the reader's decompressor execution path.
- Kept `CompressionCodes.None` as the only default compressor while preserving caller override
  behavior by compression code.
- Added limit checks before and after compression so custom compressors cannot return bytes beyond
  the configured writer limits.
- Added a `CompressionFailed` error code for unexpected compressor implementation failures; explicit
  `PmTilesException` and cancellation still pass through unchanged.

## 2026-06-06 - Metadata Serialization

- Added a production metadata encoder that validates raw JSON before final archive assembly.
- Kept v1 on raw metadata JSON, but enforced that it is a JSON object and that MVT metadata contains
  `vector_layers` as an array.
- Routed metadata compression through the writer compressor registry and `ArchiveWriteLimits` so the
  final writer will not need a separate metadata code path.

## 2026-06-06 - Tile Entry Assembly

- Added internal tile assembly for sorting by TileID, rejecting duplicates, computing stored payload
  offsets, and returning exact PMTiles counts.
- Implemented FNV-1a 128-bit hashing with independent test vectors generated from the published
  constants and algorithm.
- Deduplicates on logical input bytes before compression, which avoids recompressing duplicate
  uncompressed tile inputs and matches the policy recorded in `RESEARCH.md`.
- Added run-length coalescing only for consecutive TileIDs that resolve to the same stored payload
  offset and length; non-consecutive duplicates remain separate entries pointing to the first
  payload offset.

## 2026-06-06 - Directory Partitioning

- Added root/leaf directory construction on top of the production directory encoder and writer
  compressor registry.
- Direct roots are used when the compressed root fits the configured canonical first-read target.
- When direct roots do not fit, root entries point to compressed leaf directories with offsets
  relative to the leaf directory section; no nested leaf directory entries are produced.
- The first implementation uses one or more leaf chunks and increases chunk size until the
  compressed root fits; this satisfies the writer v1 root-to-leaf requirement without cloning the Go
  implementation's exact tuning.

## 2026-06-06 - Final Archive Assembly And Public Entry Points

- Added final archive preparation that stages header, root directory, metadata, leaf directories,
  and tile payloads before writing byte 0 to the caller sink.
- Added `PmTiles.write` for append-only sink output and `PmTiles.writeToByteString` for in-memory
  convenience/tests, both using the same prepared archive path.
- The sink path writes canonical section order sequentially, then flushes and closes the sink after
  successful writes; unsupported compression still fails before any sink bytes are written.
- Added reader round-trip coverage for a minimal PNG archive and sink failure wrapping.

## 2026-06-06 - Fixture-Derived Writer Round Trips

- Added writer tests that reuse existing upstream PMTiles fixtures at test runtime instead of
  checking in generated writer fixtures.
- Covered stored gzip MVT preservation by reading `pmtiles-js-test-fixture-1.pmtiles`, writing a new
  archive with the stored tile bytes, and comparing both stored and decompressed payloads through
  the public reader.
- Covered root-to-leaf writer output by reading all tiles through zoom 3 from the Stamen raster
  fixture, forcing a small root target, and verifying that the rewritten archive resolves a sampled
  tile through a leaf directory.
- The first test draft tried to call `rawMetadataJson()` inside a non-suspending builder lambda;
  moved suspending fixture reads ahead of option/config builders. This is also a useful constraint
  for future docs snippets.

## 2026-06-06 - Writer Docs And Swift Usability

- Updated Kotlin docs snippets with `ByteSink`, `PmTiles.writeToByteString`, and custom compressor
  examples.
- Added Swift writer shims for `ByteDataSink`, `DataCompressor`, `ArchiveWriteOptions`,
  `ArchiveWriteConfig`, writer entry points, and `ArchiveWriteTile` Data factories.
- Swift export gave the writer methods async names different from the ObjC selector names:
  `__write(_:tiles:config:options:)` and `__write(toByteStringTiles:config:options:)`; adjusted the
  shim after the first Swift compile failed on the completion-handler forms.
- Swift docs tests remain API-usability snippets, not behavior coverage; they prove sink output,
  in-memory archive writing, and compressor registration compile and run.
- Updated module and public docs from reader-only wording to reader/writer wording, keeping explicit
  caveats that filesystem/HTTP adapters and tile payload parsing/construction remain out of scope.
