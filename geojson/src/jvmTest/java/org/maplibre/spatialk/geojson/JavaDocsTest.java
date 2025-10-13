package org.maplibre.spatialk.geojson;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import kotlinx.serialization.SerializationException;
import kotlinx.serialization.json.JsonElementBuildersKt;
import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonObjectBuilder;
import org.junit.Test;
import org.maplibre.spatialk.geojson.dsl.FeatureBuilder;
import org.maplibre.spatialk.geojson.dsl.FeatureCollectionBuilder;
import org.maplibre.spatialk.geojson.dsl.GeometryCollectionBuilder;
import org.maplibre.spatialk.geojson.dsl.LineStringBuilder;
import org.maplibre.spatialk.geojson.dsl.MultiLineStringBuilder;
import org.maplibre.spatialk.geojson.dsl.MultiPointBuilder;
import org.maplibre.spatialk.geojson.dsl.MultiPolygonBuilder;
import org.maplibre.spatialk.geojson.dsl.PolygonBuilder;

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

@SuppressWarnings("unused")
public class JavaDocsTest {
  @Test
  public void positionExample() {
    // --8<-- [start:positionJava]
    Position position = new Position(-75.0, 45.0);

    // Access values
    double longitude = position.getLongitude();
    double latitude = position.getLatitude();
    Double altitude = position.getAltitude();
    // --8<-- [end:positionJava]
  }

  @Test
  public void pointExample() {
    // --8<-- [start:pointJava]
    Point point = new Point(-75.0, 45.0);

    System.out.println(point.getCoordinates().getLongitude());
    // Prints: -75.0
    // --8<-- [end:pointJava]
  }

  @Test
  public void multiPointExample() {
    // --8<-- [start:multiPointJava]
    MultiPoint multiPoint = new MultiPoint(new Position(-75.0, 45.0), new Position(-79.0, 44.0));
    // --8<-- [end:multiPointJava]
  }

  @Test
  public void lineStringExample() {
    // --8<-- [start:lineStringJava]
    LineString lineString = new LineString(new Position(-75.0, 45.0), new Position(-79.0, 44.0));
    // --8<-- [end:lineStringJava]
  }

  @Test
  public void multiLineStringExample() {
    // --8<-- [start:multiLineStringJava]
    MultiLineString multiLineString =
        new MultiLineString(
            new LineString(new Position(12.3, 45.6), new Position(78.9, 12.3)),
            new LineString(new Position(87.6, 54.3), new Position(21.9, 56.4)));
    // --8<-- [end:multiLineStringJava]
  }

  @Test
  public void polygonExample() {
    // --8<-- [start:polygonJava]
    Polygon polygon =
        new Polygon(
            new LineString(
                new Position(-79.87, 43.42),
                new Position(-78.89, 43.49),
                new Position(-79.07, 44.02),
                new Position(-79.95, 43.87),
                new Position(-79.87, 43.42)),
            new LineString(
                new Position(-79.75, 43.81),
                new Position(-79.56, 43.85),
                new Position(-79.7, 43.88),
                new Position(-79.75, 43.81)));
    // --8<-- [end:polygonJava]
  }

  @Test
  public void multiPolygonExample() {
    // --8<-- [start:multiPolygonJava]
    Polygon polygon =
        new Polygon(
            new LineString(
                new Position(-79.87, 43.42),
                new Position(-78.89, 43.49),
                new Position(-79.07, 44.02),
                new Position(-79.95, 43.87),
                new Position(-79.87, 43.42)),
            new LineString(
                new Position(-79.75, 43.81),
                new Position(-79.56, 43.85),
                new Position(-79.7, 43.88),
                new Position(-79.75, 43.81)));
    MultiPolygon multiPolygon = new MultiPolygon(polygon, polygon);
    // --8<-- [end:multiPolygonJava]
  }

  @Test
  public void geometryCollectionExample() {
    // --8<-- [start:geometryCollectionJava]
    Point point = new Point(-75.0, 45.0);
    LineString lineString = new LineString(new Position(-75.0, 45.0), new Position(-79.0, 44.0));
    GeometryCollection<Geometry> geometryCollection = new GeometryCollection<>(point, lineString);
    // --8<-- [end:geometryCollectionJava]
  }

  @Test
  public void featureExample() {
    // --8<-- [start:featureJava]
    Point point = new Point(-75.0, 45.0);

    JsonObjectBuilder properties = new JsonObjectBuilder();
    JsonElementBuildersKt.put(properties, "size", 9999);
    Feature<Point, JsonObject> feature = new Feature<>(point, properties.build(), null, null);

    Integer size = Feature.getIntProperty(feature, "size");
    Point geometry = feature.getGeometry(); // point
    // --8<-- [end:featureJava]
  }

  @Test
  public void featureCollectionExample() {
    // --8<-- [start:featureCollectionJava]
    Point point = new Point(-75.0, 45.0);
    Feature<Point, JsonObject> pointFeature = new Feature<>(point, null, null, null);
    FeatureCollection<Point, JsonObject> featureCollection = new FeatureCollection<>(pointFeature);
    // --8<-- [end:featureCollectionJava]
  }

  @Test
  public void boundingBoxExample() {
    // --8<-- [start:boundingBoxJava]
    BoundingBox bbox = new BoundingBox(11.6, 45.1, 12.7, 45.7);
    Position southwest = bbox.getSouthwest();
    Position northeast = bbox.getNortheast();
    // --8<-- [end:boundingBoxJava]
  }

  @Test
  public void serializationToJsonExample() {
    // --8<-- [start:serializationToJsonJava]
    Point point = new Point(-75.0, 45.0);
    Feature<Point, JsonObject> feature = new Feature<>(point, null, null, null);
    FeatureCollection<Point, JsonObject> featureCollection = new FeatureCollection<>(feature);
    String json = FeatureCollection.toJson(featureCollection);
    System.out.println(json);
    // --8<-- [end:serializationToJsonJava]
  }

  @Test
  public void serializationFromJsonExample() {
    assertThrows(
        SerializationException.class,
        () -> {
          // --8<-- [start:serializationFromJsonJava1]
          // Throws exception if the JSON cannot be deserialized to a Point
          Point myPoint =
              Point.fromJson("{\"type\": \"MultiPoint\", \"coordinates\": [[-75.0, 45.0]]}");
          // --8<-- [end:serializationFromJsonJava1]
        });

    // --8<-- [start:serializationFromJsonJava2]
    // Returns null if an error occurs
    Point nullable =
        Point.fromJsonOrNull("{\"type\": \"MultiPoint\", \"coordinates\": [[-75.0, 45.0]]}");
    // --8<-- [end:serializationFromJsonJava2]
    assertNull(nullable);
  }

  @Test
  public void dslMultiPointExample() {
    // --8<-- [start:dslMultiPointJava]
    MultiPointBuilder builder = new MultiPointBuilder();
    builder.add(-75.0, 45.0, null);
    builder.add(new Position(-78.0, 44.0));
    builder.add(new Point(88.0, 34.0));
    MultiPoint multiPoint = builder.build();
    // --8<-- [end:dslMultiPointJava]
  }

  @Test
  public void dslLineStringExample() {
    // --8<-- [start:dslLineStringJava]
    LineStringBuilder builder = new LineStringBuilder();
    builder.add(45.0, 45.0, null);
    builder.add(0.0, 0.0, null);
    LineString lineString = builder.build();
    // --8<-- [end:dslLineStringJava]
  }

  @Test
  public void dslMultiLineStringExample() {
    // --8<-- [start:dslMultiLineStringJava]
    LineStringBuilder lineBuilder = new LineStringBuilder();
    lineBuilder.add(45.0, 45.0, null);
    lineBuilder.add(0.0, 0.0, null);
    LineString simpleLine = lineBuilder.build();

    MultiLineStringBuilder builder = new MultiLineStringBuilder();
    builder.add(simpleLine);

    LineStringBuilder lineBuilder2 = new LineStringBuilder();
    lineBuilder2.add(44.4, 55.5, null);
    lineBuilder2.add(55.5, 66.6, null);
    builder.add(lineBuilder2.build());

    MultiLineString multiLineString = builder.build();
    // --8<-- [end:dslMultiLineStringJava]
  }

  @Test
  public void dslPolygonExample() {
    // --8<-- [start:dslPolygonJava]
    PolygonBuilder builder = new PolygonBuilder();

    LineString ring1 =
        new LineString(
            new Position(45.0, 45.0),
            new Position(0.0, 0.0),
            new Position(12.0, 12.0),
            new Position(45.0, 45.0));
    builder.add(ring1);

    LineString ring2 =
        new LineString(
            new Position(4.0, 4.0),
            new Position(2.0, 2.0),
            new Position(3.0, 3.0),
            new Position(4.0, 4.0));
    builder.add(ring2);

    Polygon polygon = builder.build();
    // --8<-- [end:dslPolygonJava]
  }

  @Test
  public void dslMultiPolygonExample() {
    // --8<-- [start:dslMultiPolygonJava]
    PolygonBuilder polyBuilder = new PolygonBuilder();
    LineString ring1 =
        new LineString(
            new Position(45.0, 45.0),
            new Position(0.0, 0.0),
            new Position(12.0, 12.0),
            new Position(45.0, 45.0));
    polyBuilder.add(ring1);

    LineString ring2 =
        new LineString(
            new Position(4.0, 4.0),
            new Position(2.0, 2.0),
            new Position(3.0, 3.0),
            new Position(4.0, 4.0));
    polyBuilder.add(ring2);
    Polygon simplePolygon = polyBuilder.build();

    MultiPolygonBuilder builder = new MultiPolygonBuilder();
    builder.add(simplePolygon);

    PolygonBuilder polyBuilder2 = new PolygonBuilder();
    LineString ring3 =
        new LineString(
            new Position(12.0, 0.0),
            new Position(0.0, 12.0),
            new Position(-12.0, 0.0),
            new Position(5.0, 5.0),
            new Position(12.0, 0.0));
    polyBuilder2.add(ring3);
    builder.add(polyBuilder2.build());

    MultiPolygon multiPolygon = builder.build();
    // --8<-- [end:dslMultiPolygonJava]
  }

  @Test
  public void dslGeometryCollectionExample() {
    // --8<-- [start:dslGeometryCollectionJava]
    Point simplePoint = new Point(-75.0, 45.0, 100.0);

    LineString simpleLine = new LineString(new Position(45.0, 45.0), new Position(0.0, 0.0));

    PolygonBuilder polyBuilder = new PolygonBuilder();
    LineString ring1 =
        new LineString(
            new Position(45.0, 45.0),
            new Position(0.0, 0.0),
            new Position(12.0, 12.0),
            new Position(45.0, 45.0));
    polyBuilder.add(ring1);

    LineString ring2 =
        new LineString(
            new Position(4.0, 4.0),
            new Position(2.0, 2.0),
            new Position(3.0, 3.0),
            new Position(4.0, 4.0));
    polyBuilder.add(ring2);
    Polygon simplePolygon = polyBuilder.build();

    GeometryCollectionBuilder<Geometry> builder = new GeometryCollectionBuilder<>();
    builder.add(simplePoint);
    builder.add(simpleLine);
    builder.add(simplePolygon);
    GeometryCollection<Geometry> geometryCollection = builder.build();
    // --8<-- [end:dslGeometryCollectionJava]
  }

  @Test
  public void dslFeatureExample() {
    // --8<-- [start:dslFeatureJava]
    Point point = new Point(-75.0, 45.0);
    JsonObjectBuilder propsBuilder = new JsonObjectBuilder();
    JsonElementBuildersKt.put(propsBuilder, "name", "Hello World");
    JsonElementBuildersKt.put(propsBuilder, "value", 13);
    JsonElementBuildersKt.put(propsBuilder, "cool", true);

    FeatureBuilder<Point, JsonObject> builder = new FeatureBuilder<>(point, propsBuilder.build());
    builder.setId("point1");
    builder.setBbox(new BoundingBox(-76.9, 44.1, -74.2, 45.7));
    Feature<Point, JsonObject> feature = builder.build();
    // --8<-- [end:dslFeatureJava]
  }

  @Test
  public void dslFeatureCollectionExample() {
    // --8<-- [start:dslFeatureCollectionJava]
    Point point = new Point(-75.0, 45.0);
    JsonObjectBuilder propsBuilder = new JsonObjectBuilder();
    JsonElementBuildersKt.put(propsBuilder, "name", "Hello");

    FeatureBuilder<Point, JsonObject> featureBuilder =
        new FeatureBuilder<>(point, propsBuilder.build());
    Feature<Point, JsonObject> feature = featureBuilder.build();

    FeatureCollectionBuilder<Point, JsonObject> builder = new FeatureCollectionBuilder<>();
    builder.add(feature);
    FeatureCollection<Point, JsonObject> featureCollection = builder.build();
    // --8<-- [end:dslFeatureCollectionJava]
  }
}
