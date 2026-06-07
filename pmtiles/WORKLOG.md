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
