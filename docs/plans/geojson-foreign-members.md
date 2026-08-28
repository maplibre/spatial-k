# Plan: GeoJSON foreign members

Implement RFC 7946 §6.1 foreign members on every `GeoJsonObject`. This closes
[maplibre/spatial-k#128](https://github.com/maplibre/spatial-k/issues/128) and unblocks reading GOFS
`zone_id` (a string sibling of `properties` on Feature, not a property).

Do not add a third generic to `Feature`. Do not emit a JSON key named `foreignMembers`. Do not treat
foreign values as GeoJSON types.

## Design (do not revisit)

- Store leftovers as `JsonObject` on `GeoJsonObject`.
- Default to a shared empty instance. Omit the bag from JSON when empty.
- Flatten on JSON encode; lift unknown keys on JSON decode.
- Typed access is a second step: `decodeForeignMembers<T>()`.
- Reject reserved GeoJSON member names in the bag (RFC §7.1).
- Preserve extras through Turf rebuilds (`mapProperties`, `mapGeometry`).
- Non-JSON formats (CBOR / Protobuf): keep existing round-trips for objects without extras. If
  extras are present, encode them as a nested `foreignMembers` map of `String` to JSON-text `String`
  (same idea as `FeatureIdSerializer`), or skip them if that is far simpler and document the
  limitation. Do **not** emit a nested `foreignMembers` key in JSON.
- Do not implement configurable Preserve / Ignore / Reject in this change (`#266`). Default behavior
  is Preserve.
- Do not commit this plan file as user-facing docs in the final API. Leave it on the branch; do not
  link it from the Astro docs site.

## Current behavior to replace

- `GeoJson.jsonFormat` has `ignoreUnknownKeys = true`, so extras are dropped.
- Custom serializers only read/write spec members.
- `Feature<G, P>` types `properties` only. GOFS `zone_id` is invisible.

`ignoreUnknownKeys = true` stays. After this change it still matters for typed `P` data classes
(unknown _property_ keys), not for Feature-level extras.

## API

### `GeoJsonObject`

Add:

```kotlin
/**
 * Members not defined by RFC 7946. Empty when none are present.
 * GeoJSON semantics do not apply to these values (RFC 7946 §6.1).
 */
public val foreignMembers: JsonObject
```

Shared empty instance (internal is fine if used as a constructor default in this module):

```kotlin
internal val EmptyForeignMembers: JsonObject = JsonObject(emptyMap())
```

Use it as the default on every constructor so the no-extras path does not allocate a new map per
object.

### Typed decode / encode helpers

Add package-level or `GeoJsonObject` extensions. Hide reified helpers from ObjC with
`@HiddenFromObjC` (`kotlin.native.HiddenFromObjC`), matching `pmtiles`.

```kotlin
@HiddenFromObjC
public inline fun <reified T : Any> GeoJsonObject.decodeForeignMembers(): T

@HiddenFromObjC
public inline fun <reified T : Any> T.toForeignMembers(): JsonObject
```

`decodeForeignMembers` uses `GeoJson.jsonFormat.decodeFromJsonElement`. `toForeignMembers` encodes a
`@Serializable` object to `JsonObject` (must be a JSON object, not a primitive/array).

KDoc must say:

- These are **not** `Feature.properties`.
- Foreign values are not GeoJSON (RFC example: `centerline` is not a LineString).
- GOFS example: `decodeForeignMembers<GofsZoneMembers>()` where
  `@Serializable data class GofsZoneMembers(val zone_id: String)`.

Optional Java-friendly accessor on `GeoJsonObject.Companion` (same style as
`Feature.containsProperty`):

```kotlin
public fun GeoJsonObject.getForeignMember(key: String): JsonElement?
```

Do not add a family of `getStringForeignMember` helpers unless it stays tiny. Java can use
`getForeignMembers().get(key)`.

### Constructors

Add `foreignMembers: JsonObject = EmptyForeignMembers` as the **last** parameter of every primary
and secondary constructor on:

- `Point` (including `Point(lon, lat, alt, bbox)`)
- `MultiPoint`
- `LineString`
- `MultiLineString`
- `Polygon`
- `MultiPolygon`
- `GeometryCollection`
- `Feature` (after `bbox`)
- `FeatureCollection`

Keep `@JvmOverloads`. Existing Kotlin/Java call sites that omit the new argument must keep
compiling.

### Validation

In each type's `init` (or a shared helper called from `init`):

```kotlin
internal fun validateForeignMembers(foreignMembers: JsonObject, reserved: Set<String>)
```

Throw `IllegalArgumentException` if any bag key is reserved.

Reserved sets (object's spec members **plus** RFC §7.1 bans):

| Type                                                                  | Reserved keys                                                                           |
| --------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon | `type`, `bbox`, `coordinates`, `geometry`, `properties`, `features`                     |
| GeometryCollection                                                    | `type`, `bbox`, `geometries`, `geometry`, `properties`, `features`                      |
| Feature                                                               | `type`, `bbox`, `geometry`, `properties`, `id`, `coordinates`, `geometries`, `features` |
| FeatureCollection                                                     | `type`, `bbox`, `features`, `coordinates`, `geometries`, `geometry`, `properties`       |

`id` on a Point is a valid foreign member. `crs` is a valid foreign member everywhere. `id` on a
Feature is reserved.

### DSL builders

Add `public var foreignMembers: JsonObject = EmptyForeignMembers` to:

- `LineStringBuilder`
- `PolygonBuilder`
- `MultiPointBuilder`
- `MultiLineStringBuilder`
- `MultiPolygonBuilder`
- `GeometryCollectionBuilder`
- `FeatureBuilder`
- `FeatureCollectionBuilder`

Pass it through `build()`. Update KDoc on `buildFeature` / `addFeature` to mention `foreignMembers`
alongside `id` and `bbox`.

## Serialization

Custom serializers already exist. Extend them. kotlinx.serialization has no `@JsonAnyGetter`.

### Shared helper

Add something like `geojson/serialization/ForeignMembers.kt` (internal):

- `reserved` sets
- `validateForeignMembers`
- `JsonObject.without(reserved): JsonObject` (empty → `EmptyForeignMembers`)
- `encodeGeoJsonObject(encoder, specObject, foreignMembers)` for the JSON path
- `decodeGeoJsonObject(decoder): JsonObject` for the JSON path

### JSON encode (all object serializers)

If `encoder is JsonEncoder`:

1. Build a `JsonObject` with spec members in the **current field order** (existing exact-string
   tests must keep passing when the bag is empty).
2. Append each foreign member as a sibling.
3. `encoder.encodeJsonElement(...)`.
4. Never write a key named `foreignMembers`.

Current orders to preserve when the bag is empty:

- Geometry: `type`, optional `bbox`, `coordinates`
- GeometryCollection: `type`, optional `bbox`, `geometries`
- Feature: `type`, `geometry`, `properties`, optional `id`, optional `bbox`
- FeatureCollection: `type`, optional `bbox`, `features`

Omit `bbox` / `id` when null **in JSON**, same as today (`encoder !is JsonEncoder` still writes them
for other formats).

### JSON decode

If `decoder is JsonDecoder`:

1. `decodeJsonElement()` as `JsonObject`.
2. Decode spec fields from that object with the existing nested serializers
   (`json.decodeFromJsonElement(...)`).
3. Remainder = foreign members.
4. Construct the type (validation runs in `init`).

Do not parse foreign values as `Geometry` / `Feature`. Keep `JsonElement`.

`JsonContentPolymorphicSerializer` (`GeoJsonObjectSerializer`, `GeometrySerializer`, …) still
selects by `type`, then the concrete serializer lifts extras. Nested geometries inside Feature must
keep their own extras.

### Non-JSON path

Keep the existing `encodeStructure` / `decodeStructure` logic. Add an optional `foreignMembers`
descriptor element only if you implement non-JSON preservation. If skipped, objects with an empty
bag must still CBOR-roundtrip (`NonJsonFormatTest`).

### Serializers to update

- `BaseGeometrySerializer` and every `construct(...)` override (`PointSerializer`,
  `MultiPointSerializer`, `LineStringSerializer`, `MultiLineStringSerializer`, `PolygonSerializer`,
  `MultiPolygonSerializer`)
- `GeometryCollectionSerializer`
- `FeatureSerializer`
- `FeatureCollectionSerializer`

`construct` gains a `foreignMembers: JsonObject` argument.

## Turf and other rebuilds

These reconstruct `Feature` / `FeatureCollection` and must pass `foreignMembers` through:

- `turf/.../featureconversion/mapProperties.kt`
- `turf/.../featureconversion/mapGeometry.kt`

`copy(bbox = ...)` in `withComputedBbox` and `RTree` already preserves a new data-class property.
Confirm; do not drop extras.

`nearestPointTo` and GPX `toGeoJson()` create **new** objects; they should keep the empty default.
Do not invent extras there.

`GeometryCollection.toFeatureCollection` uses `buildFeature`; extras can be set in the builder
block. No special case required.

## Documentation

Update `docs/src/content/docs/geojson/index.mdx`:

- After the Feature / FeatureCollection sections (or under Serialization), add a short **Foreign
  members** section.
- Explain RFC §6.1, that extras are not GeoJSON-typed, and that they are **not** `properties`.
- Show GOFS `zone_id` as the worked example (Kotlin + JSON tabs), using snippets from
  `KotlinDocsTest` (and Java if it stays readable).
- Mention `decodeForeignMembers` / `toForeignMembers` / DSL `foreignMembers = ...`.

Add matching region-tagged snippets in:

- `geojson/src/commonTest/kotlin/.../KotlinDocsTest.kt`
- `geojson/src/jvmTest/java/.../JavaDocsTest.java` (construct + read the bag; skip reified decode)

KDoc on `GeoJsonObject.foreignMembers` should link RFC 7946 §6.1.

## Tests

Add `geojson/src/commonTest/kotlin/.../ForeignMembersTest.kt`. Cover:

1. **GOFS Feature `zone_id`**: decode a Feature (and a FeatureCollection) with `properties.name` and
   sibling `zone_id`; read via bag and via `decodeForeignMembers<GofsZoneMembers>()`.
2. **Round-trip**: `fromJson(json).toJson()` preserves `zone_id` (use `assertJsonEquals` from
   `geojson/.../utils/Json.kt`).
3. **Write path**: construct with `toForeignMembers()` or `buildJsonObject`; encoded JSON has
   `zone_id` as a sibling, not under `foreignMembers` or `properties`.
4. **RFC `title`** foreign member on Feature.
5. **RFC `centerline`**: a foreign object that looks like a LineString is **not** parsed as
   `Geometry`. It stays a `JsonObject` in the bag.
6. **Geometry extras**: Point with `crs` or `title` round-trips.
7. **Nested extras**: Feature extras plus geometry extras, independently.
8. **FeatureCollection extras** on the collection object itself.
9. **Empty bag omitted** from JSON (`"foreignMembers"` key must not appear).
10. **Reserved name rejected** on construct
    (`Feature(..., foreignMembers =
    buildJsonObject { put("geometry", ...) })` throws).
11. **Equality**: two Features that differ only by `zone_id` are not equal. Two with empty bags are
    equal to today's no-arg objects.
12. **Typed properties still work**: `Feature.fromJson<Point, NameProp>` with unknown keys _inside_
    `properties` still follows `ignoreUnknownKeys` (unknown property keys ignored; Feature-level
    extras captured).
13. **Polymorphic**: `GeoJsonObject.fromJson` / `Geometry.fromJson` keep extras.
14. **DSL**: `buildFeature { foreignMembers = ... }` and geometry builders.
15. **Turf**: `mapProperties` / `mapGeometry` preserve `zone_id`. `withComputedBbox()` preserves
    extras.

Keep using `testutil` / existing JSON helpers for floats. Do not use `assertEquals` for coordinate
doubles.

Existing exact-string serialization tests must still pass when the bag is empty. Prefer
`assertJsonEquals` for new tests (order-insensitive).

Add a CBOR case in `NonJsonFormatTest` if you preserve extras there; if not, add a comment that
foreign members are JSON-only.

## ABI / format / lint

- `explicitApi()` is on. Every new public member needs KDoc.
- Update ABI dumps after the API change. The module uses Kotlin `abiValidation()` in
  `published-library.gradle.kts`. Find the dump-update task (typically `updateLegacyAbi` / dump ABI)
  and run it for `:geojson` and `:turf` if Turf public signatures change (`mapProperties` /
  `mapGeometry` parameter lists should **not** change; they should only pass the new field through).
- Run formatters on touched files: `hk fix [FILES...]` or `mise run fix` if needed.
- Do not add `@Suppress` to paper over real issues.

## Implementation order

1. Helper (`EmptyForeignMembers`, reserved sets, `validateForeignMembers`).
2. `GeoJsonObject` property + decode/encode helpers.
3. Data-class constructors + `init` validation (all nine types).
4. Serializers (shared JSON flatten/lift, then each serializer).
5. DSL builders.
6. Turf `mapProperties` / `mapGeometry`.
7. Tests.
8. Docs snippets + `index.mdx`.
9. ABI dump update.
10. `mise exec -- ./gradlew :geojson:jvmTest :turf:jvmTest` (and Detekt / compile if that is cheap).
    Fix until green.

## Out of scope

- Configurable serialization (`#266`).
- Making polymorphic serializers format-agnostic (`#238`) beyond not breaking CBOR tests.
- GOFS-specific types in this library.
- Changing GOFS; we only consume `zone_id` as a foreign member.
- A third type parameter on `Feature`.

## Style

- Match existing KDoc, `@JvmOverloads`, `@JvmStatic`, `@PublishedApi`, `@Language("json")` patterns.
- Do not rename files or “clean up” unrelated code.
- Do not add comments that restate the code.
- Prefer small internal helpers over copy-pasted JSON partition logic in every serializer.
