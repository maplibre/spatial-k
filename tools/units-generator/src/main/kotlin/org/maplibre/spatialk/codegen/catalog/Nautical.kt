package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val cableReference =
    Reference(
        "Official international cable definition",
        "https://www.itlos.org/fileadmin/itlos/documents/cases/case_no_2/merits/E0320AM.pdf",
    )

val nautical =
    family("Nautical", "International nautical lengths.", siReference) {
        length(
            "Fathoms",
            1.8288,
            "fathom",
            note = "1 fathom = 6 international feet",
            reference = customaryReference,
        )
        length(
            "InternationalCables",
            185.2,
            "cable",
            note = "1 international cable = 1/10 international nautical mile",
            reference = cableReference,
        )
        length("NauticalMiles", 1852.0, "nmi", common = true)
    }
