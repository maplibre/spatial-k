package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.family

val malaysia =
    family(
        "Malaysia",
        "Surveying units used in Malaysian mapping: full Sears definitions in East Malaysia; truncated Sears and Benoit definitions in West Malaysia. See [JUPEM (2009)](https://www.jupem.gov.my/jupem18a/assets/uploads/files/pekeliling/38724-pek-1-2009.pdf) and [IHO Revised Kertau](https://registry.iho.int/fdd/view5.do?idx=2334&type=5&valueType=0).",
        epsgReference,
    ) {
        length(
            "Sears1922Feet",
            0.3047994715386762,
            "ft",
            note = "British foot (Sears 1922), EPSG 9041",
        )
        length(
            "Sears1922Yards",
            0.9143984146160287,
            "yd",
            note = "British yard (Sears 1922), EPSG 9040",
        )
        length(
            "Sears1922Chains",
            20.116765121552632,
            "ch",
            note = "British chain (Sears 1922), EPSG 9042",
        )
        length(
            "Sears1922Links",
            0.2011676512155263,
            "link",
            note = "British link (Sears 1922), EPSG 9043",
        )
        length(
            "Sears1922TruncatedFeet",
            0.30479933333333337,
            "ft",
            note = "British foot (Sears 1922 truncated), EPSG 9300",
        )
        length(
            "Sears1922TruncatedYards",
            0.914398,
            "yd",
            note = "British yard (Sears 1922 truncated), EPSG 9099",
        )
        length(
            "Sears1922TruncatedChains",
            20.116756,
            "ch",
            note = "British chain (Sears 1922 truncated), EPSG 9301",
        )
        length(
            "Sears1922TruncatedLinks",
            0.20116756,
            "link",
            note = "British link (Sears 1922 truncated), EPSG 9302",
        )
        length(
            "Benoit1895BFeet",
            0.30479973476327077,
            "ft",
            note = "British foot (Benoit 1895 B), EPSG 9061",
        )
        length(
            "Benoit1895BYards",
            0.9143992042898124,
            "yd",
            note = "British yard (Benoit 1895 B), EPSG 9060",
        )
        length(
            "Benoit1895BChains",
            20.116782494375872,
            "ch",
            note = "British chain (Benoit 1895 B), EPSG 9062",
        )
        length(
            "Benoit1895BLinks",
            0.2011678249437587,
            "link",
            note = "British link (Benoit 1895 B), EPSG 9063",
        )
    }
