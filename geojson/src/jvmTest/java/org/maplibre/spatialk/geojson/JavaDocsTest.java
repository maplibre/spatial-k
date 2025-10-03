package org.maplibre.spatialk.geojson;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import kotlinx.serialization.SerializationException;
import kotlinx.serialization.json.JsonElementBuildersKt;
import kotlinx.serialization.json.JsonObjectBuilder;
import org.junit.Test;

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
    Point point = new Point(new Position(-75.0, 45.0));

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
    Point point = new Point(new Position(-75.0, 45.0));
    LineString lineString = new LineString(new Position(-75.0, 45.0), new Position(-79.0, 44.0));
    GeometryCollection geometryCollection = new GeometryCollection(point, lineString);
    // --8<-- [end:geometryCollectionJava]
  }

  @Test
  public void featureExample() {
    // --8<-- [start:featureJava]
    Point point = new Point(new Position(-75.0, 45.0));

    JsonObjectBuilder properties = new JsonObjectBuilder();
    JsonElementBuildersKt.put(properties, "size", 9999);
    Feature<Point> feature = new Feature<>(point, properties.build(), null, null);

    Integer size = feature.getIntProperty("size");
    Point geometry = feature.getGeometry(); // point
    // --8<-- [end:featureJava]
  }

  @Test
  public void featureCollectionExample() {
    // --8<-- [start:featureCollectionJava]
    Point point = new Point(new Position(-75.0, 45.0));
    Feature<Point> pointFeature = new Feature<>(point, null, null, null);
    FeatureCollection featureCollection = new FeatureCollection(new Feature[] {pointFeature}, null);
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
    Point point = new Point(new Position(-75.0, 45.0));
    Feature<Point> feature = new Feature<>(point, null, null, null);
    FeatureCollection featureCollection = new FeatureCollection(new Feature[] {feature}, null);

    String json = featureCollection.toJson();
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
}
