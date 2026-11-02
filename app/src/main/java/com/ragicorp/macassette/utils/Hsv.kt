package com.ragicorp.macassette.utils

import kotlin.math.abs
import kotlin.math.roundToInt

data class Hsv(
    val hue: Float,
    val saturation: Float,
    val value: Float,
)

// Hue in degrees [0, 360), saturation and value in [0, 1]. The alpha is ignored.
fun Int.toHsv(): Hsv {
    val red = (this shr 16 and 0xFF) / 255f
    val green = (this shr 8 and 0xFF) / 255f
    val blue = (this and 0xFF) / 255f
    val max = maxOf(red, green, blue)
    val delta = max - minOf(red, green, blue)
    val hue =
        when {
            delta == 0f -> 0f
            max == red -> 60f * (((green - blue) / delta).mod(6f))
            max == green -> 60f * ((blue - red) / delta + 2f)
            else -> 60f * ((red - green) / delta + 4f)
        }
    return Hsv(hue = hue, saturation = if (max == 0f) 0f else delta / max, value = max)
}

// The ARGB color with this hue, saturation and value, and the given alpha (0-255)
fun Hsv.toColor(alpha: Int): Int {
    val chroma = value * saturation
    val x = chroma * (1 - abs((hue / 60f).mod(2f) - 1))
    val (red, green, blue) =
        when {
            hue < 60f -> Triple(chroma, x, 0f)
            hue < 120f -> Triple(x, chroma, 0f)
            hue < 180f -> Triple(0f, chroma, x)
            hue < 240f -> Triple(0f, x, chroma)
            hue < 300f -> Triple(x, 0f, chroma)
            else -> Triple(chroma, 0f, x)
        }
    val match = value - chroma
    return (alpha shl 24) or
        (((red + match) * 255).roundToInt() shl 16) or
        (((green + match) * 255).roundToInt() shl 8) or
        ((blue + match) * 255).roundToInt()
}
