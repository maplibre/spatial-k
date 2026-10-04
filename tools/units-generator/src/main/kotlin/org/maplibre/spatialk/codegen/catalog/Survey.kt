package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.family

val survey =
    family(
        "Survey",
        "Surveying definitions shared across countries. The Indian 1937 definition is used from Bangladesh to Vietnam; the British 1936 foot is used in British and Irish metric conversions. See [BGS unit usage](https://webapps.bgs.ac.uk/data/vocabularies/viewdata.cfm?name=DIC_UNIT_OF_MEASURE&row=21).",
        epsgReference,
    ) {
        length("IndianFeet1937", 0.30479841, "ft", note = "Indian foot (1937), EPSG 9081")
        length("IndianYards1937", 0.91439523, "yd", note = "Indian yard (1937), EPSG 9085")
        length("BritishFeet1936", 0.3048007491, "ft", note = "British foot (1936), EPSG 9095")
    }
