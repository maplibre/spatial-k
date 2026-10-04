package org.maplibre.spatialk.codegen.catalog

import com.squareup.kotlinpoet.CodeBlock
import org.maplibre.spatialk.codegen.family

val sweden =
    family(
        "Sweden",
        "Historical Swedish angular divisions, with 6300 streck per turn.",
        nordicAngleReference,
    ) {
        rotation(
            "Streck6300",
            CodeBlock.of("360.0 / 6300"),
            "streck",
            note = "1 historical streck = 1/6300 turn",
        )
    }
