package org.maplibre.spatialk.units;

import static org.maplibre.spatialk.units.Units.*;
import static org.maplibre.spatialk.units.Units.Acres;
import static org.maplibre.spatialk.units.Units.Miles;
import static org.maplibre.spatialk.units.extensions.Utils.convert;

import kotlin.Unit;
import org.junit.Test;
import org.maplibre.spatialk.units.catalog.Thailand;

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
  public void customFormats() {
    // --8<-- [start:customFormats]
    // Thai land area, such as 1 ไร่ 3 งาน 50 ตร.วา
    AreaFormat thaiLand =
        AreaFormat.build(
            builder -> {
              builder.compound(
                  " ",
                  true,
                  compound -> {
                    compound.part(Thailand.Rai);
                    compound.part(Thailand.Ngan);
                    compound.part(Thailand.SquareWa);
                    return Unit.INSTANCE;
                  });
              builder.unit(SquareMeters);
              return Unit.INSTANCE;
            });
    // parse returns square meters for areas, meters for lengths, and degrees for rotations
    double plotSqM = thaiLand.parse("1 ไร่ 3 งาน 50 ตร.วา");
    System.out.println(thaiLand.format(3200.0, Thailand.Rai, 0)); // 2 ไร่
    // --8<-- [end:customFormats]
  }

  @Test
  public void formatPresets() {
    // --8<-- [start:formatPresets]
    double angleDeg = RotationFormat.Dms.parse("12° 30′ 15″");
    double maxheightM = LengthFormat.Osm.parse("4.2"); // meters by default

    // OSM tags railway track gauge in millimeters, like gauge=1435
    LengthFormat gaugeFormat =
        LengthFormat.build(
            LengthFormat.Osm,
            builder -> {
              builder.setDefaultUnit(Millimeters);
              return Unit.INSTANCE;
            });
    double gaugeM = gaugeFormat.parse("1435");
    // --8<-- [end:formatPresets]
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
