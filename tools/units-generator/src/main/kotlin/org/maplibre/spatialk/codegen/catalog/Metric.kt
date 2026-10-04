package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.family

val metric =
    family("Metric", "Metric lengths and land areas.", siReference) {
        area("SquareMillimeters", 0.000001, "mm²")
        area("SquareCentimeters", 0.0001, "cm²")
        area("SquareMeters", 1.0, "m²", common = true)
        area("SquareKilometers", 1_000_000.0, "km²", common = true)
        length("Millimeters", 0.001, "mm", common = true, squared = "SquareMillimeters")
        length("Centimeters", 0.01, "cm", common = true, squared = "SquareCentimeters")
        length("Meters", 1.0, "m", common = true, squared = "SquareMeters")
        length("Kilometers", 1_000.0, "km", common = true, squared = "SquareKilometers")
        area("Centiares", 1.0, "ca")
        area("Deciares", 10.0, "da")
        area("Ares", 100.0, "a")
        area("Decares", 1_000.0, "daa")
        area("Hectares", 10_000.0, "ha", common = true)
        area("SquareDecimeters", 0.01, "dm²")
        area("SquareDecameters", 100.0, "dam²")
        area("SquareHectometers", 10_000.0, "hm²")
        length("Decimeters", 0.1, "dm", squared = "SquareDecimeters")
        length("Decameters", 10.0, "dam", squared = "SquareDecameters")
        length("Hectometers", 100.0, "hm", squared = "SquareHectometers")
    }
