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

- [ ] `combine`
- [ ] `explode`
- [ ] `flatten`
- [ ] `lineToPolygon`
- [ ] `polygonize`
- [ ] `polygonToLine`

## Miscellaneous

- [ ] `kinks`
- [ ] `lineArc`
- [ ] `lineChunk`
- [x] [`lineIntersect`](../api/turf/org.maplibre.spatialk.turf.misc/intersect.html)
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

- [ ] `pointsWithinPolygon`
- [ ] `tag`

## Grids

- [ ] `hexGrid`
- [ ] `pointGrid`
- [x] [`squareGrid`](../api/turf/org.maplibre.spatialk.turf.grids/square-grid.html)
- [ ] `triangleGrid`

## Classification

- [ ] `nearestPoint`

## Aggregation

- [ ] `collect`
- [ ] `clustersDbscan`
- [ ] `clustersKmeans`

## Meta

- [x] [`coordAll`](../api/turf/org.maplibre.spatialk.turf.meta/flatten-coordinates.html)
- [ ] `coordEach`
- [ ] `coordReduce`
- [ ] `featureEach`
- [ ] `featureReduce`
- [ ] `flattenEach`
- [ ] `flattenReduce`
- [ ] `getCoord`
- [ ] `getCoords`
- [ ] `getGeom`
- [ ] `getType`
- [ ] `geomEach`
- [ ] `geomReduce`
- [ ] `propEach`
- [ ] `segmentEach`
- [ ] `segmentReduce`
- [ ] `getCluster`
- [ ] `clusterEach`
- [ ] `clusterReduce`

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

For converting between units of length and area, see the
[Units](../units/index.md) module.

- [x] [`azimuthToBearing`](../api/turf/org.maplibre.spatialk.turf.unitconversion/azimuth-to-bearing.html)
- [x] [`bearingToAzimuth`](../api/turf/org.maplibre.spatialk.turf.unitconversion/bearing-to-azimuth.html)
- [x] [`radiansToDegrees`](../api/turf/org.maplibre.spatialk.turf.unitconversion/radians-to-degrees.html)
- [x] [`degreesToRadians`](../api/turf/org.maplibre.spatialk.turf.unitconversion/degrees-to-radians.html)
- [ ] `toMercator`
- [ ] `toWgs84`
