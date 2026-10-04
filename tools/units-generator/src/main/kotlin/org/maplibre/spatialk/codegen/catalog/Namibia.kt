package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.family

val namibia =
    family(
        "Namibia",
        "German legal meters used in Namibia's Schwarzeck coordinate systems. See the [Roads Authority Survey Manual (2014), section 1.2](https://www.ra.org.na/cms/Lists/Downloads/Attachments/19/SURVEY%20MANUAL_oct_%202014.pdf).",
        epsgReference,
    ) {
        length(
            "GermanLegalMeters",
            1.0000135965,
            "m",
            note = "German legal meter, EPSG 9031: 1.0000135965 SI meters",
        )
    }
