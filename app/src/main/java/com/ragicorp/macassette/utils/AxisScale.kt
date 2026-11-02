package com.ragicorp.macassette.utils

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

// Steps are 1, 2 or 5 times a power of ten, never below 1
private val NICE_STEP_FACTORS = listOf(1.0, 2.0, 5.0, 10.0)

data class AxisScale(
    val min: Double,
    val max: Double,
    val step: Double,
) {
    val tickCount = ((max - min) / step).roundToInt() + 1
}

// A scale with round bounds and step covering the values and 0, with about targetIntervals steps between its bounds
fun axisScale(
    values: List<Double>,
    targetIntervals: Int,
): AxisScale {
    val minValue = minOf(values.minOrNull() ?: 0.0, 0.0)
    val maxValue = maxOf(values.maxOrNull() ?: 0.0, 0.0)
    val rawStep = (maxValue - minValue) / targetIntervals
    val step =
        if (rawStep <= 1.0) {
            1.0
        } else {
            val magnitude = 10.0.pow(floor(log10(rawStep)))
            NICE_STEP_FACTORS.first { it * magnitude >= rawStep } * magnitude
        }
    val min = floor(minValue / step) * step
    val max = ceil(maxValue / step) * step
    return AxisScale(min = min, max = if (max == min) min + step else max, step = step)
}
