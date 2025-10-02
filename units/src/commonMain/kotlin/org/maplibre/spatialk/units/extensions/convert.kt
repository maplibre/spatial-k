@file:JvmName("Utils")
@file:JvmMultifileClass

package org.maplibre.spatialk.units.extensions

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import org.maplibre.spatialk.units.AreaUnit
import org.maplibre.spatialk.units.LengthUnit

public fun Double.convert(from: LengthUnit, to: LengthUnit): Double = toLength(from).toDouble(to)

public fun Double.convert(from: AreaUnit, to: AreaUnit): Double = toArea(from).toDouble(to)
