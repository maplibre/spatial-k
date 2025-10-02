package org.maplibre.spatialk.units;

import static org.maplibre.spatialk.units.Imperial.Acres;
import static org.maplibre.spatialk.units.Imperial.Miles;
import static org.maplibre.spatialk.units.International.*;
import static org.maplibre.spatialk.units.extensions.Utils.convert;

import org.junit.Test;

// These snippets are primarily intended to be included in documentation. Though they exist as
// part of the test suite, they are not intended to be comprehensive tests.

@SuppressWarnings("unused")
public class JavaDocsTest {
  @Test
  public void conversion() {
    // --8<-- [start:conversion]
    double distanceKm = convert(123.0, Miles, Kilometers);
    System.out.println(Kilometers.format(distanceKm, 2));

    double areaSqM = convert(45.0, Acres, SquareMeters);
    System.out.println(SquareMeters.format(areaSqM, 2));
    // --8<-- [end:conversion]
  }

  @Test
  public void customUnits() {
    // --8<-- [start:customUnits]
    // how many football fields could fit on the earth's oceans?
    AreaUnit AmericanFootballField = new AreaUnit(109.728 * 48.8, "football fields");
    double earthRadiusM = convert(6371.0, Kilometers, Meters);
    double earthSurfaceSqM = 4 * Math.PI * earthRadiusM * earthRadiusM;
    double oceanSurfaceSqM = 0.7 * earthSurfaceSqM;
    double result = convert(oceanSurfaceSqM, SquareMeters, AmericanFootballField);
    // --8<-- [end:customUnits]
  }
}
