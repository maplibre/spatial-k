package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.family

val customary =
    family(
        "Customary",
        "Land lengths and areas based on the international foot (exactly 0.3048 meters).",
        customaryReference,
    ) {
        area("SquareInches", 0.00064516, "in²")
        area("SquareFeet", 0.09290304, "ft²", common = true)
        area("SquareYards", 0.83612736, "yd²", common = true)
        area("SquareMiles", 2_589_988.110336, "mi²", common = true)
        area("SquareRods", 25.29285264, "rd²")
        area(
            "Acres",
            4_046.8564224,
            "acre",
            common = true,
            note = "1 acre = 43560 international square feet",
        )
        length("Inches", 0.0254, "in", common = true, squared = "SquareInches")
        length("Feet", 0.3048, "ft", common = true, squared = "SquareFeet")
        length("Yards", 0.9144, "yd", common = true, squared = "SquareYards")
        length(
            "LandMiles",
            1_609.344,
            "mi",
            common = true,
            commonName = "Miles",
            squared = "SquareMiles",
        )
        length(
            "GunterLinks",
            0.201168,
            "link",
            note = "1 Gunter link = 0.66 international feet",
        )
        length(
            "Rods",
            5.0292,
            "rod",
            squared = "SquareRods",
            note = "1 rod = 16.5 international feet; also called a pole or perch",
        )
        length(
            "GunterChains",
            20.1168,
            "ch",
            note = "1 Gunter chain = 66 international feet",
        )
        length("Furlongs", 201.168, "fur", note = "1 furlong = 660 international feet")
        length(
            "LandLeagues",
            4828.032,
            "lea",
            note = "1 land league = 3 international-foot miles",
        )
    }
