package org.maplibre.spatialk.geohash;

import org.junit.Test;
import org.maplibre.spatialk.geojson.BoundingBox;
import org.maplibre.spatialk.geojson.Position;

@SuppressWarnings("unused")
public class JavaDocsTest {
  @Test
  public void staticFactoriesAndCellProperties() {
    Position position = new Position(10.40744, 57.64911);
    Geohash encoded = Geohash.of(position, 11);
    Geohash parsed = Geohash.parse("u4pruydqqvj");
    Geohash nullable = Geohash.parseOrNull("U4PRUYDQQVJ");

    String text = parsed.getText();
    BoundingBox boundingBox = parsed.getBoundingBox();
    GeohashNeighbors neighbors = parsed.getNeighbors();
    Geohash east = neighbors.getEast();
    Geohash offset = parsed.offsetBy(1, 0);
    java.util.List<Geohash> children = parsed.getChildren();
  }

  @Test
  public void shortlinkStaticFactories() {
    OsmShortlink encoded = OsmShortlink.of(new Position(0.054, 51.510), 9);
    OsmShortlink parsed = OsmShortlink.parse("https://osm.org/go/0EEQjE--");
    OsmShortlink nullable = OsmShortlink.parseOrNull("0EEQjE--");
  }
}
