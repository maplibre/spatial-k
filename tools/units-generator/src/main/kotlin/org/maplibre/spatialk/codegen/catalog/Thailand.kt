package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val thaiReference =
    Reference(
        "Thai Agricultural Land Reform Office",
        "https://alro.go.th/uploads/org/mukdahan/download/article/article_20190822204852.pdf",
    )

private val waReference =
    Reference(
        "Thai government technical-college conversion chart",
        "https://ctc.chontech.ac.th/files/2204271515202353_24051514142105.pdf",
    )

val thailand =
    family(
        "Thailand",
        "Thai land measures: rai, ngan, square wa, and wa. See [Treasury Department notation](https://www.treasury.go.th/en/services/land-assessment/search-land-price).",
        thaiReference,
    ) {
        area("SquareWa", 4.0, "ตร.วา", note = "1 square wa = 4 square meters")
        area("Ngan", 400.0, "งาน", note = "1 ngan = 100 square wa = 400 square meters")
        area("Rai", 1600.0, "ไร่", note = "1 rai = 4 ngan = 1600 square meters")
        length("Wa", 2.0, "วา", squared = "SquareWa", reference = waReference)
    }
