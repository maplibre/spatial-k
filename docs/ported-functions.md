# Ported Functions

The following functions have been ported as of version `0.2.0` of this library.

You can view porting progress for the next release
[here](https://github.com/maplibre/spatial-k/milestone/1).

## Measurement

- [x] [`along`](./api/turf/org.maplibre.spatialk.turf.measurement/along.html)
- [x] [`area`](./api/turf/org.maplibre.spatialk.turf.measurement/area.html)
- [x] [`bbox`](./api/turf/org.maplibre.spatialk.turf.measurement/bbox.html)
- [x] [`bboxPolygon`](./api/turf/org.maplibre.spatialk.turf.measurement/bbox-polygon.html)
- [x] [`bearing`](./api/turf/org.maplibre.spatialk.turf.measurement/bearing.html)
- [x] [`center`](./api/turf/org.maplibre.spatialk.turf.measurement/center.html)
- [ ] `centerOfMass`
- [ ] `centroid`
- [x] [`destination`](./api/turf/org.maplibre.spatialk.turf.measurement/destination.html)
- [x] [`distance`](./api/turf/org.maplibre.spatialk.turf.measurement/distance.html)
- [x] [`envelope`](./api/turf/org.maplibre.spatialk.turf.measurement/envelope.html)
- [x] [`greatCircle`](./api/turf/org.maplibre.spatialk.turf.measurement/great-circle.html)
- [x] [`length`](./api/turf/org.maplibre.spatialk.turf.measurement/length.html)
- [x] [`midpoint`](./api/turf/org.maplibre.spatialk.turf.measurement/midpoint.html)
- [ ] `pointOnFeature`
- [ ] `polygonTangents`
- [x] [`pointToLineDistance`](./api/turf/org.maplibre.spatialk.turf.measurement/point-to-line-distance.html)
- [ ] `rhumbBearing`
- [ ] `rhumbDestination`
- [x] [`rhumbDistance`](./api/turf/org.maplibre.spatialk.turf.measurement/rhumb-distance.html)
- [x] [`square`](./api/turf/org.maplibre.spatialk.turf.measurement/square.html)

## Coordinate Mutation

- [ ] `cleanCoords`
- [ ] `flip`
- [ ] `rewind`
- [x] [`round`](./api/turf/org.maplibre.spatialk.turf.coordinatemutation/round.html)
- [ ] `truncate`

## Transformation

- [ ] `bboxClip`
- [x] [`bezierSpline`](./api/turf/org.maplibre.spatialk.turf.transformation/bezier-spline.html)
- [ ] `buffer`
- [x] [`circle`](./api/turf/org.maplibre.spatialk.turf.transformation/circle.html)
- [ ] `clone`
- [ ] `concave`
- [ ] `convex`
- [ ] `difference`
- [ ] `dissolve`
- [ ] `intersect`
- [ ] `lineOffset`
- [x] [`simplify`](./api/turf/org.maplibre.spatialk.turf.transformation/simplify.html)
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
- [x] [`lineIntersect`](./api/turf/org.maplibre.spatialk.turf.misc/line-intersect.html)
      Partially implemented.
- [ ] `lineOverlap`
- [ ] `lineSegment`
- [x] [`lineSlice`](./api/turf/org.maplibre.spatialk.turf.misc/line-slice.html)
- [ ] `lineSliceAlong`
- [ ] `lineSplit`
- [ ] `mask`
- [x] [`nearestPointOnLine`](./api/turf/org.maplibre.spatialk.turf.misc/nearest-point-on-line.html)
- [ ] `sector`
- [ ] `shortestPath`
- [ ] `unkinkPolygon`

## Helper

Use the [GeoJson DSL](./geojson.md#geojson-dsl) instead.

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
- [x] [`squareGrid`](./api/turf/org.maplibre.spatialk.turf.grids/square-grid.html)
- [ ] `triangleGrid`

## Classification

- [ ] `nearestPoint`

## Aggregation

- [ ] `collect`
- [ ] `clustersDbscan`
- [ ] `clustersKmeans`

## Meta

- [x] [`coordAll`](./api/turf/org.maplibre.spatialk.turf.meta/coord-all.html)
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
- [x] [`booleanPointInPolygon`](./api/turf/org.maplibre.spatialk.turf.booleans/point-in-polygon.html)
- [ ] `booleanPointOnLine`
- [ ] `booleanWithin`

## Unit Conversion

For converting between units of length and area, see the [Units](./units.md)
module.

- [x] [`azimuthToBearing`](./api/turf/org.maplibre.spatialk.turf.unitconversion/azimuth-to-bearing.html)
- [x] [`bearingToAzimuth`](./api/turf/org.maplibre.spatialk.turf.unitconversion/bearing-to-azimuth.html)
- [x] [`radiansToDegrees`](./api/turf/org.maplibre.spatialk.turf.unitconversion/radians-to-degrees.html)
- [x] [`degreesToRadians`](./api/turf/org.maplibre.spatialk.turf.unitconversion/degrees-to-radians.html)
- [ ] `toMercator`
- [ ] `toWgs84`
