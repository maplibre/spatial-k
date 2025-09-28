package org.maplibre.spatialk.units

import kotlin.math.PI

public sealed interface UnitOfMeasure {
    public val symbol: String

    public fun format(value: Double, decimalPlaces: Int = Int.MAX_VALUE): String =
        "${value.toRoundedString(decimalPlaces)} $symbol"
}

public interface LengthUnit : UnitOfMeasure, Comparable<LengthUnit> {
    public val metersPerUnit: Double

    override fun compareTo(other: LengthUnit): Int = metersPerUnit.compareTo(other.metersPerUnit)

    /** Create an [AreaUnit] by multiplying this [LengthUnit] by the given [LengthUnit]. */
    public operator fun times(other: LengthUnit): AreaUnit {
        return object : AreaUnit {
            override val metersSquaredPerUnit: Double =
                this@LengthUnit.metersPerUnit * other.metersPerUnit

            override val symbol: String =
                if (this@LengthUnit == other) "${this@LengthUnit.symbol}²"
                else "${this@LengthUnit.symbol}-${other.symbol}"
        }
    }

    /** SI units of length. */
    public sealed class International(
        override val metersPerUnit: Double,
        override val symbol: String,
    ) : LengthUnit {
        public data object Millimeters : International(0.001, "mm") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Millimeters) AreaUnit.International.SquareMillimeters
                else super.times(other)
        }

        public data object Centimeters : International(0.01, "cm") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Centimeters) AreaUnit.International.SquareCentimeters
                else super.times(other)
        }

        public data object Meters : International(1.0, "m") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Meters) AreaUnit.International.SquareMeters else super.times(other)
        }

        public data object Kilometers : International(1000.0, "km") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Kilometers) AreaUnit.International.SquareKilometers
                else super.times(other)
        }
    }

    /**
     * Angular units measuring the length of an arc on the surface of the earth; approximated using
     * the average radius of the Earth (= 6,371.0088 km).
     */
    public sealed class Geodesy(override val metersPerUnit: Double, override val symbol: String) :
        LengthUnit {
        public data object Radians : Geodesy(6_371_008.8, "rad")

        public data object Degrees : Geodesy(Radians.metersPerUnit * PI / 180, "°") {
            override fun format(value: Double, decimalPlaces: Int): String =
                "${value.toRoundedString(decimalPlaces)}$symbol"
        }

        public data object Minutes : Geodesy(Degrees.metersPerUnit * PI / (180 * 60), "arcmin")

        public data object Seconds :
            Geodesy(Minutes.metersPerUnit * PI / (180 * 60 * 60), "arcsec")
    }

    /** British Imperial and US Customary units of length. */
    public sealed class Imperial(override val metersPerUnit: Double, override val symbol: String) :
        LengthUnit {
        public data object Inches : Imperial(.0254, "in") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Inches) AreaUnit.Imperial.SquareInches else super.times(other)
        }

        public data object Feet : Imperial(0.3048, "ft") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Feet) AreaUnit.Imperial.SquareFeet else super.times(other)
        }

        public data object Yards : Imperial(0.9144, "yd") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Yards) AreaUnit.Imperial.SquareYards else super.times(other)
        }

        public data object Links : Imperial(0.201168, "li")

        public data object Rods : Imperial(5.0292, "rd") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Rods) AreaUnit.Imperial.SquareRods else super.times(other)
        }

        public data object Chains : Imperial(20.1168, "ch")

        public data object Furlongs : Imperial(201.168, "fur")

        public data object Miles : Imperial(1609.344, "mi") {
            override fun times(other: LengthUnit): AreaUnit =
                if (other is Miles) AreaUnit.Imperial.SquareMiles else super.times(other)
        }

        public data object Leagues : Imperial(4828.032, "lea")

        public data object Fathoms : Imperial(1.852, "ftm")

        public data object Cables : Imperial(185.2, "cable")

        public data object NauticalMiles : Imperial(1852.0, "nmi")
    }
}
