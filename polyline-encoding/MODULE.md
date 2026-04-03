# Module polyline-encoding

Encoding and decoding of coordinate sequences using the [Google Encoded Polyline Algorithm](https://developers.google.com/maps/documentation/utilities/polylinealgorithm).

## Usage

```kotlin
val positions = listOf(
    Position(latitude = 38.5, longitude = -120.2),
    Position(latitude = 40.7, longitude = -120.95),
    Position(latitude = 43.252, longitude = -126.453),
)

val encoded = PolylineEncoding.encode(positions)     // "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
val decoded = PolylineEncoding.decode(encoded)       // back to positions

// Safe variant — returns null instead of throwing on malformed input
val safe = PolylineEncoding.decodeOrNull(encoded)
```

## Precision

The `precision` parameter controls how many decimal digits are preserved (default: `5`).

| Value | Resolution | Common use |
|-------|-----------|------------|
| `5`   | ~1.1 m    | Google Maps, OSRM, Valhalla |
| `6`   | ~0.11 m   | OpenTripPlanner, some routing APIs |

Encode and decode must use the same precision value.
