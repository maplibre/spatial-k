package org.maplibre.spatialk.units

import kotlin.math.PI

public abstract class LengthUnit(metersPerUnit: Double) : Unit<Dimension.Length>() {
    final override val magnitude: Double = metersPerUnit

    public open operator fun times(other: LengthUnit): AreaUnit =
        object : AreaUnit(magnitude * other.magnitude) {
            override val symbol: String = "${this@LengthUnit.symbol}-${other.symbol}"
        }

    /** SI units of length. */
    public sealed class International(metersPerUnit: Double, final override val symbol: String) :
        LengthUnit(metersPerUnit) {
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
    public sealed class Geodesy(metersPerUnit: Double, final override val symbol: String) :
        LengthUnit(metersPerUnit) {
        public data object Radians : Geodesy(6_371_008.8, "rad")

        public data object Degrees : Geodesy(Radians.magnitude * PI / 180, "°") {
            override fun format(value: Double, decimalPlaces: Int): String =
                "${value.toRoundedString(decimalPlaces)}$symbol"
        }

        public data object Minutes : Geodesy(Degrees.magnitude * PI / (180 * 60), "arcmin")

        public data object Seconds : Geodesy(Minutes.magnitude * PI / (180 * 60 * 60), "arcsec")
    }

    /** British Imperial and US Customary units of length. */
    public sealed class Imperial(metersPerUnit: Double, final override val symbol: String) :
        LengthUnit(metersPerUnit) {
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
