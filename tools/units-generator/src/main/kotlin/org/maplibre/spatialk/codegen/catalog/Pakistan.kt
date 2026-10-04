package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.family

val pakistan =
    family(
        "Pakistan",
        "Indian 1962 surveying definitions used in Pakistan. See [BGS unit usage](https://webapps.bgs.ac.uk/data/vocabularies/viewdata.cfm?name=DIC_UNIT_OF_MEASURE&row=21).",
        epsgReference,
    ) {
        length("IndianFeet1962", 0.3047996, "ft", note = "Indian foot (1962), EPSG 9082")
        length("IndianYards1962", 0.9143988, "yd", note = "Indian yard (1962), EPSG 9086")
    }
