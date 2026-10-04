package org.maplibre.spatialk.codegen.catalog

import org.maplibre.spatialk.codegen.Reference
import org.maplibre.spatialk.codegen.family

private val surveyReference =
    Reference(
        "NIST survey-foot conversions",
        "https://www.nist.gov/pml/us-surveyfoot/revised-unit-conversion-factors",
    )

private val surveyFoot = 1200.0 / 3937

private val surveySquareFoot = surveyFoot * surveyFoot

val us =
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
    }
