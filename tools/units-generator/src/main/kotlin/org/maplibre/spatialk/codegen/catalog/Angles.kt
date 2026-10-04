package org.maplibre.spatialk.codegen.catalog

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val gonReference =
    Reference(
        "EU units directive",
        "https://eur-lex.europa.eu/legal-content/EN/ALL/?uri=CELEX:01980L0181-20200613",
    )

private val compassReference =
    Reference(
        "Brunton OmniSight specification",
        "https://conibe.com/images/archivos/Brunton-Omnisight-ss.pdf",
    )

private val angularHourReference =
    Reference("NOAA solar-position calculations", "https://gml.noaa.gov/grad/solcalc/solareqns.PDF")

val angles =
    family(
        "Angles",
        "Angular units. Degrees, arcminutes, and arcseconds can also be formatted as DMS.",
        siReference,
    ) {
        rotation(
            "Radians",
            CodeBlock.of("180.0 / %M", MemberName("kotlin.math", "PI")),
            "rad",
            common = true,
            note = "1 radian = 180/π degrees",
        )
        rotation(
            "Gradians",
            CodeBlock.of("0.9"),
            "gon",
            note = "1 gradian = 0.9 degree; a full turn is 400 gradians",
            reference = gonReference,
        )
        rotation("Degrees", CodeBlock.of("1.0"), "°", common = true)
        rotation(
            "Turns",
            CodeBlock.of("360.0"),
            "turn",
            common = true,
            note = "1 turn = 360 degrees (one revolution)",
            reference = gonReference,
        )
        rotation(
            "Milliradians",
            CodeBlock.of("0.001 * 180.0 / %M", MemberName("kotlin.math", "PI")),
            "mrad",
            note = "1 milliradian = 1/1000 radian",
        )
        rotation(
            "Microradians",
            CodeBlock.of("0.000001 * 180.0 / %M", MemberName("kotlin.math", "PI")),
            "µrad",
            note = "1 microradian = 1/1000000 radian (EPSG 9109)",
            reference = epsgReference,
        )
        rotation(
            "MilliarcSeconds",
            CodeBlock.of("1.0 / 3600000"),
            "mas",
            note = "1 milliarcsecond = 1/1000 arcsecond (EPSG 1031)",
            reference = epsgReference,
        )
        rotation(
            "CentesimalMinutes",
            CodeBlock.of("0.9 / 100"),
            "cgon",
            note = "1 centesimal minute = 1/100 gradian (EPSG 9112)",
            reference = epsgReference,
        )
        rotation(
            "CentesimalSeconds",
            CodeBlock.of("0.9 / 10000"),
            "cc",
            note = "1 centesimal second = 1/10000 gradian (EPSG 9113)",
            reference = epsgReference,
        )
        rotation(
            "Mils6400",
            CodeBlock.of("360.0 / 6400"),
            "mil",
            note = "1 mil = 1/6400 turn",
            reference = compassReference,
        )
        rotation(
            "AngularHours",
            CodeBlock.of("15.0"),
            "h",
            note = "1 angular hour = 15 degrees",
            reference = angularHourReference,
        )
        rotation(
            "AngularMinutes",
            CodeBlock.of("15.0 / 60"),
            "min",
            note = "1 angular minute = 1/60 angular hour",
            reference = angularHourReference,
        )
        rotation(
            "AngularSeconds",
            CodeBlock.of("15.0 / 3600"),
            "s",
            note = "1 angular second = 1/3600 angular hour",
            reference = angularHourReference,
        )
        rotation(
            "ArcMinutes",
            CodeBlock.of("1.0 / 60"),
            "′",
            common = true,
            note = "1 arcminute = 1/60 degree",
        )
        rotation(
            "ArcSeconds",
            CodeBlock.of("1.0 / 60 / 60"),
            "″",
            common = true,
            note = "1 arcsecond = 1/3600 degree",
        )
    }
