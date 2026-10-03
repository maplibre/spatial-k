package org.maplibre.spatialk.codegen

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName

// Edit this catalog, run mise run generate:units, and commit the generated Kotlin.
// Scope: length, area, and angle units in official use somewhere on or after 1950,
// at scales suitable for terrestrial mapping.
// Keep source references and native defining ratios.
// common = true adds a Units alias and extensions; country/subregion names qualify regional units.

private val siReference =
    Reference("SI Brochure", "https://www.bipm.org/documents/20126/41483022/SI-Brochure-9-EN.pdf")
private val customaryReference =
    Reference("NIST Handbook 44", "https://www.nist.gov/document/2026-nist-handbook-44-appendix-c")
private val gonReference =
    Reference(
        "EU units directive",
        "https://eur-lex.europa.eu/legal-content/EN/ALL/?uri=CELEX:01980L0181-20200613",
    )
private val cableReference =
    Reference(
        "Official international cable definition",
        "https://www.itlos.org/fileadmin/itlos/documents/cases/case_no_2/merits/E0320AM.pdf",
    )

private val surveyReference =
    Reference(
        "NIST survey-foot conversions",
        "https://www.nist.gov/pml/us-surveyfoot/revised-unit-conversion-factors",
    )
private val epsgReference =
    Reference(
        "EPSG conversions in PROJ",
        "https://github.com/OSGeo/PROJ/blob/047e5dbc74ef872828c06f24c6f67bb7b55432cf/data/sql/unit_of_measure.sql",
    )
private val nauticalHistoryReference =
    Reference(
        "IHO nautical-mile definitions (1950)",
        "https://ihr.iho.int/articles/the-nautical-mile/",
    )
private val ihoCableReference =
    Reference(
        "IHO Hydrographic Dictionary, cable",
        "https://portal.iho.int/iho-ohi/S32/engView.php?page=30",
    )
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
private val chinaReference =
    Reference(
        "China National Bureau of Statistics",
        "https://www.stats.gov.cn/hd/cjwtjd/202302/t20230207_1902270.html",
    )
private val assamReference =
    Reference(
        "Assam Resettlement Policy Framework (2022), section 7.2",
        "https://igr.assam.gov.in/sites/default/files/swf_utility_folder/departments/asdma_revenue_uneecopscloud_com_oid_70/menu/document/airbmp_rpf_0.pdf",
    )
private val nepalReference =
    Reference(
        "Nepal CDC Mathematics Grade 9 (2022), page 116",
        "https://lib.moecdc.gov.np/catalog/opac_css/index.php?id=13786&lvl=notice_display",
    )
private val nordicAngleReference =
    Reference(
        "Finnish Roads and Waterways Administration SI guide (1975), page 52",
        "https://www.doria.fi/bitstream/10024/132346/1/tie755.pdf",
    )
private val compassReference =
    Reference(
        "Brunton OmniSight specification",
        "https://conibe.com/images/archivos/Brunton-Omnisight-ss.pdf",
    )
private val angularHourReference =
    Reference("NOAA solar-position calculations", "https://gml.noaa.gov/grad/solcalc/solareqns.PDF")

private val surveyFoot = 1200.0 / 3937
private val surveySquareFoot = surveyFoot * surveyFoot
private const val squareFoot = 0.09290304

val catalog =
    listOf(
        family("Metric", "Metric lengths and land areas.", siReference) {
            area("SquareMillimeters", 0.000001, "mm²")
            area("SquareCentimeters", 0.0001, "cm²")
            area("SquareMeters", 1.0, "m²", common = true)
            area("SquareKilometers", 1_000_000.0, "km²", common = true)
            length("Millimeters", 0.001, "mm", common = true, squared = "SquareMillimeters")
            length("Centimeters", 0.01, "cm", common = true, squared = "SquareCentimeters")
            length("Meters", 1.0, "m", common = true, squared = "SquareMeters")
            length("Kilometers", 1_000.0, "km", common = true, squared = "SquareKilometers")
            area("Centiares", 1.0, "ca")
            area("Deciares", 10.0, "da")
            area("Ares", 100.0, "a")
            area("Decares", 1_000.0, "daa")
            area("Hectares", 10_000.0, "ha", common = true)
            area("SquareDecimeters", 0.01, "dm²")
            area("SquareDecameters", 100.0, "dam²")
            area("SquareHectometers", 10_000.0, "hm²")
            length("Decimeters", 0.1, "dm", squared = "SquareDecimeters")
            length("Decameters", 10.0, "dam", squared = "SquareDecameters")
            length("Hectometers", 100.0, "hm", squared = "SquareHectometers")
        },
        family(
            "Customary",
            "Land lengths and areas based on the international foot (exactly 0.3048 meters).",
            customaryReference,
        ) {
            area("SquareInches", 0.00064516, "in²")
            area("SquareFeet", 0.09290304, "ft²", common = true)
            area("SquareYards", 0.83612736, "yd²", common = true)
            area("SquareMiles", 2_589_988.110336, "mi²", common = true)
            area("SquareRods", 25.29285264, "rd²")
            area(
                "Acres",
                4_046.8564224,
                "acre",
                common = true,
                note = "1 acre = 43560 international square feet",
            )
            length("Inches", 0.0254, "in", common = true, squared = "SquareInches")
            length("Feet", 0.3048, "ft", common = true, squared = "SquareFeet")
            length("Yards", 0.9144, "yd", common = true, squared = "SquareYards")
            length(
                "LandMiles",
                1_609.344,
                "mi",
                common = true,
                commonName = "Miles",
                squared = "SquareMiles",
            )
            length(
                "GunterLinks",
                0.201168,
                "link",
                note = "1 Gunter link = 0.66 international feet",
            )
            length(
                "Rods",
                5.0292,
                "rod",
                squared = "SquareRods",
                note = "1 rod = 16.5 international feet; also called a pole or perch",
            )
            length(
                "GunterChains",
                20.1168,
                "ch",
                note = "1 Gunter chain = 66 international feet",
            )
            length("Furlongs", 201.168, "fur", note = "1 furlong = 660 international feet")
            length(
                "LandLeagues",
                4828.032,
                "lea",
                note = "1 land league = 3 international-foot miles",
            )
        },
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
        },
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
        },
        family(
            "US",
            "U.S. units, including the survey-foot family for legacy data. The survey foot is 1200/3937 meters.",
            surveyReference,
        ) {
            area(
                "SurveySquareFeet",
                surveySquareFoot,
                "ft²",
                note = "1 survey square foot = (1200/3937)² square meters",
            )
            area(
                "SurveySquareRods",
                272.25 * surveySquareFoot,
                "rd²",
                note = "1 survey square rod = 272.25 survey square feet",
            )
            area(
                "SurveyAcres",
                43560 * surveySquareFoot,
                "acre",
                note = "1 survey acre = 43560 survey square feet",
            )
            area(
                "SurveySquareMiles",
                27878400 * surveySquareFoot,
                "mi²",
                note = "1 survey square mile = 27878400 survey square feet",
            )
            length(
                "SurveyFeet",
                surveyFoot,
                "ft",
                squared = "SurveySquareFeet",
                note = "1 survey foot = 1200/3937 meters",
            )
            length(
                "SurveyGunterLinks",
                0.66 * surveyFoot,
                "link",
                note = "1 survey Gunter link = 0.66 survey feet",
            )
            length(
                "SurveyRods",
                16.5 * surveyFoot,
                "rod",
                squared = "SurveySquareRods",
                note = "1 survey rod = 16.5 survey feet",
            )
            length(
                "SurveyGunterChains",
                66 * surveyFoot,
                "ch",
                note = "1 survey Gunter chain = 66 survey feet",
            )
            length(
                "SurveyFurlongs",
                660 * surveyFoot,
                "fur",
                note = "1 survey furlong = 660 survey feet",
            )
            length(
                "SurveyMiles",
                5280 * surveyFoot,
                "mi",
                squared = "SurveySquareMiles",
                note = "1 survey mile = 5280 survey feet",
            )
            length(
                "SurveyLandLeagues",
                15840 * surveyFoot,
                "lea",
                note = "1 survey land league = 15840 survey feet",
            )
            length(
                "SurveyFathoms",
                6 * surveyFoot,
                "fathom",
                note = "1 survey fathom = 6 survey feet",
            )
            length(
                "SurveyCables",
                720 * surveyFoot,
                "cable",
                note = "1 survey cable = 720 survey feet",
            )
            length(
                "CustomaryCables",
                219.456,
                "cable",
                note = "1 customary cable = 720 international feet",
            )
            length(
                "NauticalMilesPre1954",
                1853.248,
                "nmi",
                note = "1 pre-1954 U.S. nautical mile = 1853.248 meters",
                reference = nauticalHistoryReference,
            )
        },
        family(
            "UK",
            "British historical surveying and nautical definitions.",
            epsgReference,
        ) {
            length(
                "ClarkeFeet",
                0.3047972654,
                "ft",
                note = "Clarke foot, EPSG 9005: 0.3047972654 meters",
            )
            length(
                "ClarkeYards",
                0.9143917962,
                "yd",
                note = "Clarke yard, EPSG 9037: 0.9143917962 meters",
            )
            length(
                "ClarkeChains",
                20.1166195164,
                "ch",
                note = "Clarke chain, EPSG 9038: 20.1166195164 meters",
            )
            length(
                "ClarkeLinks",
                0.201166195164,
                "link",
                note = "Clarke link, EPSG 9039: 0.201166195164 meters",
            )
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
                "Benoit1895AFeet",
                0.3047997333333333,
                "ft",
                note = "British foot (Benoit 1895 A), EPSG 9051",
            )
            length(
                "Benoit1895AYards",
                0.9143992,
                "yd",
                note = "British yard (Benoit 1895 A), EPSG 9050",
            )
            length(
                "Benoit1895AChains",
                20.1167824,
                "ch",
                note = "British chain (Benoit 1895 A), EPSG 9052",
            )
            length(
                "Benoit1895ALinks",
                0.201167824,
                "link",
                note = "British link (Benoit 1895 A), EPSG 9053",
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
            length(
                "Feet1865",
                0.30480083333333335,
                "ft",
                note = "British foot (1865), EPSG 9070",
            )
            length("Feet1936", 0.3048007491, "ft", note = "British foot (1936), EPSG 9095")
            length(
                "AdmiraltyNauticalMiles",
                6080 * 0.3048,
                "nmi",
                note = "1 Admiralty nautical mile = 6080 international feet",
                reference = ihoCableReference,
            )
            length(
                "AdmiraltyCables",
                608 * 0.3048,
                "cable",
                note = "1 Admiralty cable = 608 international feet",
                reference = ihoCableReference,
            )
        },
        family("India", "Indian historical survey definitions from EPSG.", epsgReference) {
            length("SurveyFeet", 0.30479951024814694, "ft", note = "Indian foot, EPSG 9080")
            length("SurveyYards", 0.9143985307444408, "yd", note = "Indian yard, EPSG 9084")
            length("SurveyFeet1937", 0.30479841, "ft", note = "Indian foot (1937), EPSG 9081")
            length("SurveyYards1937", 0.91439523, "yd", note = "Indian yard (1937), EPSG 9085")
            length("SurveyFeet1962", 0.3047996, "ft", note = "Indian foot (1962), EPSG 9082")
            length("SurveyYards1962", 0.9143988, "yd", note = "Indian yard (1962), EPSG 9086")
            length("SurveyFeet1975", 0.3047995, "ft", note = "Indian foot (1975), EPSG 9083")
            length("SurveyYards1975", 0.9143985, "yd", note = "Indian yard (1975), EPSG 9087")
        },
        family(
            "India.Assam",
            "Assam land areas. Foot-based counts use the ordinary foot (0.3048 meters), documented in the [1976 conversion schedule](https://www.py.gov.in/sites/default/files/revenuemanual3part1acts.pdf).",
            assamReference,
        ) {
            area(
                "Bigha",
                14400 * squareFoot,
                "bigha",
                note = "1 Assam bigha = 14400 international square feet = 5 katha",
            )
            area(
                "Katha",
                2880 * squareFoot,
                "katha",
                note = "1 Assam katha = 2880 international square feet = 20 lecha",
            )
            area(
                "Lecha",
                144 * squareFoot,
                "lecha",
                note = "1 Assam lecha = 144 international square feet",
            )
        },
        family(
            "Ghana",
            "Gold Coast surveying units used in legacy Ghanaian coordinate systems.",
            epsgReference,
        ) {
            length(
                "GoldCoastFeet",
                0.3047997101815088,
                "ft",
                note = "Gold Coast foot, EPSG 9094",
            )
        },
        family(
            "Germany",
            "German legal units used in legacy coordinate systems, including Namibia's Schwarzeck datum.",
            epsgReference,
        ) {
            length(
                "LegalMeters",
                1.0000135965,
                "m",
                note = "German legal meter, EPSG 9031: 1.0000135965 SI meters",
            )
        },
        family("Thailand", "Thai land measures: rai, ngan, square wa, and wa.", thaiReference) {
            area("SquareWa", 4.0, "wa²", note = "1 square wa = 4 square meters")
            area("Ngan", 400.0, "ngan", note = "1 ngan = 100 square wa = 400 square meters")
            area("Rai", 1600.0, "rai", note = "1 rai = 4 ngan = 1600 square meters")
            length("Wa", 2.0, "wa", squared = "SquareWa", reference = waReference)
        },
        family(
            "China",
            "Chinese market land measures used in official agricultural reporting.",
            chinaReference,
        ) {
            area(
                "MarketMu",
                2000.0 / 3,
                "mu",
                note = "1 market mu = 1/15 hectare = 2000/3 square meters",
            )
        },
        family("Nepal", "Nepali land measures, qualified by region.", nepalReference),
        family(
            "Nepal.Hill",
            "Hill-region land areas, defined by the textbook's foot counts and subdivision ratios using its stated 0.3048-meter foot.",
            nepalReference,
        ) {
            area(
                "Ropani",
                5476 * squareFoot,
                "ropani",
                note = "1 ropani = 5476 international square feet = 16 aana",
            )
            area(
                "Aana",
                5476.0 / 16 * squareFoot,
                "aana",
                note = "1 aana = 1/16 ropani = 4 paisa",
            )
            area(
                "Paisa",
                5476.0 / 64 * squareFoot,
                "paisa",
                note = "1 paisa = 1/64 ropani = 4 daam",
            )
            area("Daam", 5476.0 / 256 * squareFoot, "daam", note = "1 daam = 1/256 ropani")
        },
        family(
            "Nepal.Terai",
            "Terai-region land areas. A bigha is 20 kattha, each of 20 dhur.",
            nepalReference,
        ) {
            area(
                "Bigha",
                72900 * squareFoot,
                "bigha",
                note = "1 Terai bigha = 72900 international square feet = 20 kattha",
            )
            area(
                "Kattha",
                3645 * squareFoot,
                "kattha",
                note = "1 Terai kattha = 3645 international square feet = 20 dhur",
            )
            area(
                "Dhur",
                182.25 * squareFoot,
                "dhur",
                note = "1 Terai dhur = 182.25 international square feet",
            )
        },
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
        },
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
        },
    )
