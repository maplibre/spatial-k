package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val chinaReference =
    Reference(
        "China National Bureau of Statistics",
        "https://www.stats.gov.cn/hd/cjwtjd/202302/t20230207_1902270.html",
    )

val china =
    family(
        "China",
        "Chinese market land measures used in official agricultural reporting.",
        chinaReference,
    ) {
        area(
            "MarketMu",
            2000.0 / 3,
            "亩",
            note = "1 market mu = 1/15 hectare = 2000/3 square meters",
        )
    }
