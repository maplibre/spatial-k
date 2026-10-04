package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.family

val trinidadAndTobago =
    family(
        "TrinidadAndTobago",
        "Clarke units used in Trinidad and Tobago surveying. See the [government surveying report (2000), pages 5 and 21](https://www.terrainstitute.org/lupap/lupap_pdf/S003.pdf).",
        epsgReference,
    ) {
        length(
            "ClarkeFeet",
            0.3047972654,
            "ft",
            note = "Clarke foot, EPSG 9005: 0.3047972654 meters",
        )
        length(
            "ClarkeLinks",
            0.201166195164,
            "link",
            note = "Clarke link, EPSG 9039: 0.201166195164 meters",
        )
    }
