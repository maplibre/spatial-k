package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val nepalReference =
    Reference(
        "Nepal CDC Mathematics Grade 9 (2022), page 116",
        "https://lib.moecdc.gov.np/catalog/opac_css/index.php?id=13786&lvl=notice_display",
    )

// Nepali labels: https://giwmscdntwo.gov.np/media/pdf_upload/40_kj0tmxh.pdf (page 100).
val nepal = family("Nepal", "Nepali land measures, qualified by region.", nepalReference)

val nepalHill =
    family(
        "Nepal.Hill",
        "Hill-region land areas, defined by the textbook's foot counts and subdivision ratios using its stated 0.3048-meter foot.",
        nepalReference,
    ) {
        area(
            "Ropani",
            5476 * squareFoot,
            "रोपनी",
            note = "1 ropani = 5476 international square feet = 16 aana",
        )
        area(
            "Aana",
            5476.0 / 16 * squareFoot,
            "आना",
            note = "1 aana = 1/16 ropani = 4 paisa",
        )
        area(
            "Paisa",
            5476.0 / 64 * squareFoot,
            "पैसा",
            note = "1 paisa = 1/64 ropani = 4 daam",
        )
        area("Daam", 5476.0 / 256 * squareFoot, "दाम", note = "1 daam = 1/256 ropani")
    }

val nepalTerai =
    family(
        "Nepal.Terai",
        "Terai-region land areas. A bigha is 20 kattha, each of 20 dhur.",
        nepalReference,
    ) {
        area(
            "Bigha",
            72900 * squareFoot,
            "बिघा",
            note = "1 Terai bigha = 72900 international square feet = 20 kattha",
        )
        area(
            "Kattha",
            3645 * squareFoot,
            "कट्ठा",
            note = "1 Terai kattha = 3645 international square feet = 20 dhur",
        )
        area(
            "Dhur",
            182.25 * squareFoot,
            "धुर",
            note = "1 Terai dhur = 182.25 international square feet",
        )
    }
