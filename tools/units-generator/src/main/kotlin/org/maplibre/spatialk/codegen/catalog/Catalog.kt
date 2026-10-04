package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference

// Edit the catalog files in this package, run mise run generate:units, and commit the generated
// Kotlin.
// Scope: length, area, and angle units in official use somewhere on or after 2000,
// at scales suitable for terrestrial mapping.
// Keep source references and native defining ratios. Symbols use established notation,
// with local script for regional measures.
// common = true adds a Units alias and extensions. Regional catalogs group units
// associated with a country or region; descriptions identify where variants are used.

val siReference =
    Reference("SI Brochure", "https://www.bipm.org/documents/20126/41483022/SI-Brochure-9-EN.pdf")

val customaryReference =
    Reference("NIST Handbook 44", "https://www.nist.gov/document/2026-nist-handbook-44-appendix-c")

val epsgReference =
    Reference(
        "EPSG conversions in PROJ",
        "https://github.com/OSGeo/PROJ/blob/047e5dbc74ef872828c06f24c6f67bb7b55432cf/data/sql/unit_of_measure.sql",
    )

val nordicAngleReference =
    Reference(
        "Finnish Roads and Waterways Administration SI guide (1975), page 52",
        "https://www.doria.fi/bitstream/10024/132346/1/tie755.pdf",
    )

const val squareFoot = 0.09290304

val catalog =
    listOf(
        metric,
        customary,
        angles,
        nautical,
        us,
        uk,
        malaysia,
        trinidadAndTobago,
        india,
        indiaAssam,
        namibia,
        thailand,
        china,
        nepal,
        nepalHill,
        nepalTerai,
        finland,
        sweden,
    )
