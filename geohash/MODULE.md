# Module geohash

Geohash cells and OpenStreetMap shortlinks.

Create a Geohash cell from a WGS84 position:

```kotlin
val cell =
    Geohash.of(
        Position(longitude = 10.40744, latitude = 57.64911),
        length = 11,
    )

cell.text // "u4pruydqqvj"
cell.center
cell.boundingBox
cell.children
cell.neighbors.east
```

Parse an OpenStreetMap shortlink from a code, a `/go/` path, or a URL:

```kotlin
val shortlink = OsmShortlink.parse("https://osm.org/go/0EEQjE--")
```
