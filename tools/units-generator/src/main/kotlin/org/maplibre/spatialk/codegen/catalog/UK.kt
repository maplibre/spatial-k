package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val ihoCableReference =
    Reference(
        "IHO Hydrographic Dictionary, cable",
        "https://portal.iho.int/iho-ohi/S32/engView.php?page=30",
    )

val uk =
    family("UK", "British nautical lengths.", epsgReference) {
        length(
            "AdmiraltyCables",
            608 * 0.3048,
            "cable",
            note = "1 Admiralty cable = 608 international feet",
            reference = ihoCableReference,
        )
    }
