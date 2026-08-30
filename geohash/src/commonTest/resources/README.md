# Geohash fixtures

Checked-in vectors used by the geohash tests. Each JSON row has a `source` field. This file records
where the suites came from and why they can live in this MIT repository.

Coordinate order in these files is longitude, then latitude. Several upstream suites pass latitude
first. The rows here are flipped to match Spatial K.

## Compatible copies

### `ngeohash` (`sunng87/node-geohash`)

MIT. Author Ning Sun. Upstream
[LICENSE](https://github.com/sunng87/node-geohash/blob/master/LICENSE) has no copyright line. A copy
is in `licenses/ngeohash-MIT.txt`.

Rows come from [`tests/test.js`](https://github.com/sunng87/node-geohash/blob/master/tests/test.js).

Skipped integer hashes, `ENCODE_AUTO`, hashes longer than 12 characters, and covering/`bboxes`.
Those either exceed `Geohash.MaxLength` or test APIs this module does not have.

### `libgeohash` (`simplegeo/libgeohash`)

BSD-3-Clause. Copyright 2009 SimpleGeo. The upstream LICENSE file omits the copyright line that
appears in the C headers. `licenses/libgeohash-BSD-3-Clause.txt` keeps both.

Rows come from
[`geohash_test.c`](https://github.com/simplegeo/libgeohash/blob/master/geohash_test.c).

Skipped `geohash_encode(..., 0)`, which libgeohash treats as precision 6. This module requires an
explicit length.

### `mmcloughlin/geohash`

MIT. Copyright 2015 Michael McLoughlin. Copy in `licenses/mmcloughlin-MIT.txt`.

Rows come from the named cases in
[`geohash_test.go`](https://github.com/mmcloughlin/geohash/blob/master/geohash_test.go)
(`TestWikipediaExample`, `TestLeadingZero`).

The generated files `testcases_test.go`, `neighbors_testcases_test.go`, and `decodecases_test.go`
were not imported.

## Facts and generated output

### Wikipedia Geohash article

`u4pruydqqvj` at `(10.40744, 57.64911)` is a published coordinate and hash pair. The row records
that pair only. It does not copy article prose.

<https://en.wikipedia.org/wiki/Geohash>

### OpenStreetMap shortlinks

`openstreetmap-website` is GPL-2.0. None of its Ruby or test source is in this tree.

`osm/encode.json` is output of the published encode algorithm (wiki description plus the 32-bit wrap
used by OpenStreetMap Rails). Running a program to produce numbers is not a copy of that program.

`osm/parse.json` uses shortlink codes from
[the Shortlink wiki page](https://wiki.openstreetmap.org/wiki/Shortlink) (`0EEQjE--`, `0EEQjEEb`)
and the documented historical `@` / `=` characters. Expected boxes for those codes were produced by
the same published decode steps. `QQFq@mz--` is the `@` form of a generated code that contains `~`.

## Files

- `geohash/encode.json` — encode vectors from ngeohash, libgeohash, mmcloughlin, and the Wikipedia
  pair
- `geohash/decode.json` — ngeohash center check and libgeohash exact boxes
- `geohash/neighbors.json` — full 8-neighbor maps from ngeohash and libgeohash
- `geohash/neighbor_steps.json` — single-step ngeohash neighbor moves
- `osm/encode.json` — generated OpenStreetMap shortlink encodings
- `osm/parse.json` — wiki codes and historical character forms
