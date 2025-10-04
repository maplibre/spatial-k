package org.maplibre.spatialk.turf;

import static org.maplibre.spatialk.turf.measurement.Measurement.offset;
import static org.maplibre.spatialk.units.International.Kilometers;

import org.junit.Test;
import org.maplibre.spatialk.geojson.Position;

public class JavaDocsTest {
  @Test
  public void example() {
    // --8<-- [start:example]
    Position origin = new Position(-75.0, 45.0);
    Position dest = offset(origin, 100, Kilometers, 0);
    dest.getLongitude();
    dest.getLatitude();
    // --8<-- [end:example]
  }
}
