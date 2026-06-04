# PMTiles Swift Interop Refactor Report

This report tracks the focused refactors made to make the PMTiles Objective-C export practical from
Swift. Each entry references the rewritten branch commit that introduced the change.

## Refactors

### `01c6eed` Flatten PMTiles fixtures for SwiftPM

```swift
let url = Bundle.module.url(forResource: "pmtiles-js-test-fixture-1", withExtension: "pmtiles")!
```

Justification: SwiftPM resources are easiest to consume when fixture names are unique and
addressable directly from `Bundle.module`.

Impact: Swift tests can reuse the Kotlin fixture corpus through a single linked resource directory
instead of maintaining copied fixtures.

### `67a21c1` Name PMTiles errors explicitly for Swift

```swift
XCTAssertEqual(error.code, .invalidMagic)
```

Justification: Kotlin enum names and exception constructors need explicit ObjC names where the
generated Swift spelling would otherwise be awkward or overloaded.

Impact: Swift assertions and catch blocks can use predictable lower-camel error and warning cases.

### `de3ff04` Add known PMTiles code wrappers

```swift
if archive.header.tileCompression.isGzip { ... }
let fallback = archive.header.tileType.knownOr(.mvt)
```

Justification: `Compression` and `TileType` are open-ended PMTiles code values, but Swift callers
still need ergonomic branches for known values.

Impact: The API preserves unknown future codes while providing Swift-friendly predicates and
fallbacks for known PMTiles values.

### `4401da2` Add known PMTiles metadata kind wrappers

```swift
if metadata.type?.isBaseLayer == true { ... }
```

Justification: Tileset metadata kind is also open-ended, so companion constants would not model the
raw string domain cleanly in Swift.

Impact: Swift can inspect known metadata kinds without losing support for unknown metadata strings.

### `ca30bff` Simplify PMTiles header primitives

```swift
XCTAssertTrue(archive.header.isClustered)
XCTAssertEqual(archive.header.counts.addressedTiles.valueOr(0), 92)
```

Justification: Nullable Kotlin unsigned values and a boxed `Clustered` wrapper export poorly to
Swift for simple header fields.

Impact: Header counts expose `rawValue`, `isKnown`, and `valueOr(_:)`; clustered exports as a native
boolean property.

### `9bf2063` Validate PMTiles tile coordinates at construction

```swift
let coord = try TileCoord(z: 0, x: 0, y: 0)
let id = try coord.toTileId()
let roundTrip = try TileCoord(tileId: id)
```

Justification: Swift should not need the Kotlin `TileIds` singleton for common coordinate and TileID
conversion.

Impact: `TileCoord` initializers validate inputs and export as throwing Swift initializers, while
`TileIds` remains a Kotlin-only utility.

### `9599641` Hide reader-produced PMTiles value constructors

```swift
let range = try await archive.findTileRange(coord: coord)
XCTAssertEqual(range?.archiveRange.length, 123)
```

Justification: Reader-produced models should appear as output values, not as large Swift
constructors or generated copy APIs.

Impact: Swift autocomplete is smaller for headers, sections, ranges, bounds, centers, and warnings.

### `b2ab1bd` Snapshot PMTiles tile payloads

```swift
let data = tile.data
XCTAssertEqual(tile.byteCount, UInt64(data.count))
```

Justification: Exporting Kotlin `ByteArray` directly makes Swift callers handle mutable Kotlin
storage and awkward array interop.

Impact: Tile payloads are copied into immutable Kotlin storage, expose `byteCount`, and bridge to
Foundation `Data` on Apple targets.

### `cdd2acc` Move PMTiles opening to a factory object

```swift
let archive = try await PmTiles.shared.open(source: source)
```

Justification: A named factory object imports more naturally to Swift than opening through an
archive companion.

Impact: Swift call sites use `PmTiles.shared.open(...)`; `PmTilesArchive` construction stays
internal to the reader.

### `01721dc` Rename PMTiles archive read APIs for Swift

```swift
let tile = try await archive.readDecompressedTile(z: 0, x: 0, y: 0)
let range = try await archive.findTileRange(tileId: 0)
let warnings = archive.warnings
```

Justification: `get*` names and function-style warnings read like Kotlin internals rather than Swift
archive operations.

Impact: Swift gets `readStoredTile`, `readDecompressedTile`, `findTileRange`, batch result objects,
and a property-style warnings snapshot.

### `b1291ce` Rename PMTiles metadata description to summary

```swift
XCTAssertEqual(metadata.summary, "Basemap")
```

Justification: `description` collides with Swift/Objective-C conventions and can export with
underscored spelling.

Impact: Typed PMTiles metadata has a clean `summary` property in Swift and Kotlin.

### `9c30002` Add Swift-friendly PMTiles option builders

```swift
let limits = ArchiveLimits().withMaxMetadataBytes(1024)
let options = ArchiveOpenOptions(validationMode: .lenient).with(limits: limits)
```

Justification: Kotlin data-class `copy` and companion presets are not good Swift API shapes, and
byte limits should match PMTiles `UInt64` offsets.

Impact: Swift callers can construct and update options with explicit constructors and `with...`
methods while Kotlin keeps hidden `copy` helpers.

### `6329e73` Add Apple Data PMTiles bridges

```swift
func read(offset: UInt64, length: UInt64) async throws -> Data
func decompress(data: Data, limits: DecompressionLimits) throws -> Data
```

Justification: Swift implementations should deal in `Data`, `UInt64`, and throwing methods instead
of Kotlin byte arrays or boxed primitives.

Impact: Apple callers can provide archive sources and custom decompressors with native Foundation
types.

### `a2a8644` Update PMTiles tests for Swift-friendly APIs

```kotlin
assertEquals(true, archive.header.tileType.isMvt)
assertEquals(true, archive.readStoredTiles(coords).single().isFound)
```

Justification: Kotlin tests should prove the refactored API remains usable from Kotlin while
matching the Swift-facing surface.

Impact: Existing behavioral coverage now exercises the new factory, method names, wrappers, and
batch result shape.

### `3b2542a` Add SwiftPM harness for PMTiles interop tests

```toml
[tasks."test:swift"]
run = "swift test --package-path pmtiles/src"
```

Justification: Swift consumability needs a real Swift module that imports the Kotlin/Native
Objective-C export.

Impact: `mise run test:swift` builds the PMTiles XCFramework and runs SwiftPM XCTest against it.

### `acd1fe5` Add PMTiles Swift docs tests

```swift
let archive = try await PmTiles.shared.open(source: source)
let tile = try await archive.readDecompressedTile(coord: coord)
```

Justification: The documented Swift snippets should compile and assert the trickiest interop paths.

Impact: The docs snippets double as a Swift consumability suite for opening archives, reading tiles,
metadata, options, limits, warnings, and custom decompression.

### `778d3ad` Sync PMTiles API dumps

```text
pmtiles/api/pmtiles.api
pmtiles/api/pmtiles.klib.api
```

Justification: Public API changes must be reflected in the Kotlin binary API snapshots.

Impact: The generated API dumps match the refactored PMTiles public surface.

## Validation

The branch is intended to validate with:

```bash
mise run fix
mise exec -- ./gradlew :pmtiles:jvmTest :pmtiles:macosArm64Test
mise run test:swift
mise exec -- ./gradlew mkdocsBuild
git diff --check && git diff --cached --check
```
