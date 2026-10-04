package org.maplibre.spatialk.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName

enum class Dimension(val quantity: String, val base: String) {
    AREA("Area", "square meters"),
    LENGTH("Length", "meters"),
    ROTATION("Rotation", "degrees");

    val quantityType
        get() = ClassName("org.maplibre.spatialk.units", quantity)

    val unitType
        get() = ClassName("org.maplibre.spatialk.units", "${quantity}Unit")
}

data class Reference(val title: String, val url: String)

data class UnitDefinition(
    val family: String,
    val name: String,
    val dimension: Dimension,
    val factor: CodeBlock,
    val symbol: String,
    val source: Reference,
    val commonName: String?,
    val squaredUnit: String? = null,
    val note: String? = null,
) {
    val helper
        get() = commonName?.replaceFirstChar { it.lowercase() }

    val member
        get() = MemberName(catalogClass(family), name)

    val definition
        get() = note ?: "1 $symbol = ${factor.toString().replace("_", "")} ${dimension.base}"
}

class Family(
    val name: String,
    val description: String,
    private val source: Reference,
) {
    val units = mutableListOf<UnitDefinition>()

    fun area(
        unit: String,
        factor: Double,
        symbol: String,
        common: Boolean = false,
        note: String? = null,
    ) {
        units +=
            UnitDefinition(
                name,
                unit,
                Dimension.AREA,
                CodeBlock.of("%L", factor),
                symbol,
                source,
                if (common) unit else null,
                note = note,
            )
    }

    fun length(
        unit: String,
        factor: Double,
        symbol: String,
        common: Boolean = false,
        commonName: String = unit,
        squared: String? = null,
        note: String? = null,
        reference: Reference = source,
    ) {
        units +=
            UnitDefinition(
                name,
                unit,
                Dimension.LENGTH,
                CodeBlock.of("%L", factor),
                symbol,
                reference,
                if (common) commonName else null,
                squared,
                note,
            )
    }

    fun rotation(
        unit: String,
        factor: CodeBlock,
        symbol: String,
        common: Boolean = false,
        note: String? = null,
        reference: Reference = source,
    ) {
        units +=
            UnitDefinition(
                name,
                unit,
                Dimension.ROTATION,
                factor,
                symbol,
                reference,
                if (common) unit else null,
                note = note,
            )
    }
}

fun catalogClass(family: String): ClassName =
    ClassName.bestGuess("org.maplibre.spatialk.units.catalog.$family")

fun family(
    name: String,
    description: String,
    source: Reference,
    define: Family.() -> Unit = {},
): Family = Family(name, description, source).apply(define)
