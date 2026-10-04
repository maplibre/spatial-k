package org.maplibre.spatialk.codegen.catalog

import com.squareup.kotlinpoet.CodeBlock
import org.maplibre.spatialk.codegen.family

val finland =
    family(
        "Finland",
        "Finnish angular divisions, with 6000 piiru per turn.",
        nordicAngleReference,
    ) {
        rotation(
            "Mils6000",
            CodeBlock.of("360.0 / 6000"),
            "piiru",
            note = "1 piiru = 1/6000 turn",
        )
    }
