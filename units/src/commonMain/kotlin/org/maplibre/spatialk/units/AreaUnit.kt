package org.maplibre.spatialk.units

public interface AreaUnit : UnitOfMeasure, Comparable<AreaUnit> {
    public val metersSquaredPerUnit: Double

    override fun compareTo(other: AreaUnit): Int =
        metersSquaredPerUnit.compareTo(other.metersSquaredPerUnit)

    public sealed class International(
        override val metersSquaredPerUnit: Double,
        override val symbol: String,
    ) : AreaUnit {
        public data object SquareMillimeters : International(0.000001, "mm²")

        public data object SquareCentimeters : International(0.0001, "cm²")

        public data object SquareMeters : International(1.0, "m²")

        public data object SquareKilometers : International(1_000_000.0, "km²")
    }

    public sealed class Metric(
        override val metersSquaredPerUnit: Double,
        override val symbol: String,
    ) : AreaUnit {
        public data object Centiares : Metric(1.0, "ca")

        public data object Deciares : Metric(10.0, "da")

        public data object Ares : Metric(100.0, "a")

        public data object Decares : Metric(1_000.0, "daa")

        public data object Hectares : Metric(10_000.0, "ha")
    }

    public sealed class Imperial(
        override val metersSquaredPerUnit: Double,
        override val symbol: String,
    ) : AreaUnit {
        public data object SquareInches : Imperial(.00064516, "in²")

        public data object SquareFeet : Imperial(0.09290304, "ft²")

        public data object SquareYards : Imperial(0.83612736, "yd²")

        public data object SquareMiles : Imperial(2_589_988.110336, "mi²")

        public data object SquareRods : Imperial(25.29285264, "rd²")

        public data object Acres : Imperial(4_046.8564224, "acre")
    }
}
