# Ported Functions

## Measurement

- [x] [`along`](../api/turf/org.maplibre.spatialk.turf.measurement/locate-along.html)
- [x] [`area`](../api/turf/org.maplibre.spatialk.turf.measurement/area.html)
- [x] [`bbox`](../api/turf/org.maplibre.spatialk.turf.measurement/compute-bbox.html)
- [x] [`bboxPolygon`](../api/turf/org.maplibre.spatialk.turf.measurement/to-polygon.html)
- [x] [`bearing`](../api/turf/org.maplibre.spatialk.turf.measurement/bearing-to.html)
- [x] [`center`](../api/turf/org.maplibre.spatialk.turf.measurement/center.html)
- [ ] `centerOfMass`
- [ ] `centroid`
- [x] [`destination`](../api/turf/org.maplibre.spatialk.turf.measurement/offset.html)
- [x] [`distance`](../api/turf/org.maplibre.spatialk.turf.measurement/distance.html)
- [x] [`envelope`](../api/turf/org.maplibre.spatialk.turf.measurement/envelope.html)
- [x] [`greatCircle`](../api/turf/org.maplibre.spatialk.turf.measurement/great-circle.html)
- [x] [`length`](../api/turf/org.maplibre.spatialk.turf.measurement/length.html)
- [x] [`midpoint`](../api/turf/org.maplibre.spatialk.turf.measurement/midpoint.html)
- [ ] `pointOnFeature`
- [ ] `polygonTangents`
- [x] [`pointToLineDistance`](../api/turf/org.maplibre.spatialk.turf.measurement/distance.html)
- [ ] `rhumbBearing`
- [ ] `rhumbDestination`
- [x] [`rhumbDistance`](../api/turf/org.maplibre.spatialk.turf.measurement/rhumb-distance.html)
- [x] [`square`](../api/turf/org.maplibre.spatialk.turf.measurement/square.html)

## Coordinate Mutation

- [ ] `cleanCoords`
- [ ] `flip`
- [ ] `rewind`
- [x] [`round`](../api/turf/org.maplibre.spatialk.turf.coordinatemutation/round.html)
- [ ] `truncate`

## Transformation

- [ ] `bboxClip`
- [x] [`bezierSpline`](../api/turf/org.maplibre.spatialk.turf.transformation/bezier-spline.html)
- [ ] `buffer`
- [x] [`circle`](../api/turf/org.maplibre.spatialk.turf.transformation/circle.html)
- [ ] `clone`
- [ ] `concave`
- [ ] `convex`
- [ ] `difference`
- [ ] `dissolve`
- [ ] `intersect`
- [ ] `lineOffset`
- [x] [`simplify`](../api/turf/org.maplibre.spatialk.turf.transformation/simplify.html)
- [ ] `tessellate`
- [ ] `transformRotate`
- [ ] `transformTranslate`
- [ ] `transformScale`
- [ ] `union`
- [ ] `voronoi`

## Feature Conversion

- [x] [`combine`](../api/turf/org.maplibre.spatialk.turf.featureconversion/combine.html)
- [x] [`explode`](../api/turf/org.maplibre.spatialk.turf.featureconversion/explode.html)
- [ ] `flatten`
- [x] [`lineToPolygon`](../api/turf/org.maplibre.spatialk.turf.featureconversion/to-polygon.html)
- [ ] `polygonize`
- [x] [`polygonToLine`](../api/turf/org.maplibre.spatialk.turf.featureconversion/to-multi-line-string.html)

## Miscellaneous

- [ ] `kinks`
- [ ] `lineArc`
- [ ] `lineChunk`
- [ ] [`lineIntersect`](../api/turf/org.maplibre.spatialk.turf.misc/intersect.html)
      Partially implemented.
- [ ] `lineOverlap`
- [ ] `lineSegment`
- [x] [`lineSlice`](../api/turf/org.maplibre.spatialk.turf.misc/slice.html)
- [ ] `lineSliceAlong`
- [ ] `lineSplit`
- [ ] `mask`
- [x] [`nearestPointOnLine`](../api/turf/org.maplibre.spatialk.turf.misc/nearest-point-to.html)
- [ ] `sector`
- [ ] `shortestPath`
- [ ] `unkinkPolygon`

## Helper

Use the [GeoJson DSL](../geojson/index.md#geojson-dsl) instead.

## Random

- [ ] `randomPosition`
- [ ] `randomPoint`
- [ ] `randomLineString`
- [ ] `randomPolygon`

## Data

- [ ] `sample`

## Interpolation

- [ ] `interpolate`
- [ ] `isobands`
- [ ] `isolines`
- [ ] `planepoint`
- [ ] `tin`

## Joins

- [x] [`pointsWithinPolygon`](../api/turf/org.maplibre.spatialk.turf.misc/filter-inside.html)
- [ ] `tag`

## Grids

- [ ] `hexGrid`
- [ ] `pointGrid`
- [x] [`squareGrid`](../api/turf/org.maplibre.spatialk.turf.grids/square-grid.html)
- [ ] `triangleGrid`

## Classification

- [x] [`nearestPoint`](../api/turf/org.maplibre.spatialk.turf.misc/nearest-point-to.html)

## Aggregation

- [ ] `collect`
- [ ] `clustersDbscan`
- [ ] `clustersKmeans`

## Meta

Many GeoJson objects implement `Collection`, enabling standard Kotlin collection
operations to replace Turf meta functions.

## Assertations

- [ ] `collectionOf`
- [ ] `containsNumber`
- [ ] `geojsonType`
- [ ] `featureOf`

## Booleans

- [ ] `booleanClockwise`
- [ ] `booleanContains`
- [ ] `booleanCrosses`
- [ ] `booleanDisjoint`
- [ ] `booleanEqual`
- [ ] `booleanOverlap`
- [ ] `booleanParallel`
- [x] [`booleanPointInPolygon`](../api/turf/org.maplibre.spatialk.turf.booleans/contains.html)
- [ ] `booleanPointOnLine`
- [ ] `booleanWithin`

## Unit Conversion

For converting between units of length, area, and angle, see the
[Units](../units/index.md) module.

- [ ] `toMercator`
- [ ] `toWgs84`
