package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val assamReference =
    Reference(
        "Assam Resettlement Policy Framework (2022), section 7.2",
        "https://igr.assam.gov.in/sites/default/files/swf_utility_folder/departments/asdma_revenue_uneecopscloud_com_oid_70/menu/document/airbmp_rpf_0.pdf",
    )

val india =
    family(
        "India",
        "Indian surveying definitions: 1937 used from Bangladesh to Vietnam, 1962 in Pakistan, and 1975 in India. See [BGS unit usage](https://webapps.bgs.ac.uk/data/vocabularies/viewdata.cfm?name=DIC_UNIT_OF_MEASURE&row=21).",
        epsgReference,
    ) {
        length("SurveyFeet1937", 0.30479841, "ft", note = "Indian foot (1937), EPSG 9081")
        length("SurveyYards1937", 0.91439523, "yd", note = "Indian yard (1937), EPSG 9085")
        length("SurveyFeet1962", 0.3047996, "ft", note = "Indian foot (1962), EPSG 9082")
        length("SurveyYards1962", 0.9143988, "yd", note = "Indian yard (1962), EPSG 9086")
        length("SurveyFeet1975", 0.3047995, "ft", note = "Indian foot (1975), EPSG 9083")
        length("SurveyYards1975", 0.9143985, "yd", note = "Indian yard (1975), EPSG 9087")
    }

// Assamese labels: https://culturalaffairs.assam.gov.in/as/information-services/যাদুঘৰ
val indiaAssam =
    family(
        "India.Assam",
        "Assam land areas. Foot-based counts use the ordinary foot (0.3048 meters), documented in the [1976 conversion schedule](https://www.py.gov.in/sites/default/files/revenuemanual3part1acts.pdf).",
        assamReference,
    ) {
        area(
            "Bigha",
            14400 * squareFoot,
            "বিঘা",
            note = "1 Assam bigha = 14400 international square feet = 5 katha",
        )
        area(
            "Katha",
            2880 * squareFoot,
            "কঠা",
            note = "1 Assam katha = 2880 international square feet = 20 lecha",
        )
        area(
            "Lecha",
            144 * squareFoot,
            "লেচা",
            note = "1 Assam lecha = 144 international square feet",
        )
    }
