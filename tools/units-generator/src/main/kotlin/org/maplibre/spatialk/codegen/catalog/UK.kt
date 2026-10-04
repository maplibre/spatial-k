package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val ihoCableReference =
    Reference(
        "IHO Hydrographic Dictionary, cable",
        "https://portal.iho.int/iho-ohi/S32/engView.php?page=30",
    )

val uk =
    family(
        "UK",
        "British nautical and surveying definitions. The 1936 survey foot is used in British and Irish metric conversions. See [BGS unit usage](https://webapps.bgs.ac.uk/data/vocabularies/viewdata.cfm?name=DIC_UNIT_OF_MEASURE&row=21).",
        epsgReference,
    ) {
        length("SurveyFeet1936", 0.3048007491, "ft", note = "British foot (1936), EPSG 9095")
        length(
            "AdmiraltyCables",
            608 * 0.3048,
            "cable",
            note = "1 Admiralty cable = 608 international feet",
            reference = ihoCableReference,
        )
    }
