package com.ragicorp.macassette.utils

// Shades first differ by a clearly visible step, then by half of it once those are all taken
private const val SHADE_STEP = 0.2f
private const val MAX_SHADE_STEPS = 4

// A shade isn't taken below these (washed out or nearly black), unless that part stays the same as the picked color
private const val MIN_SATURATION = 0.3f
private const val MIN_VALUE = 0.3f

// This ARGB color if unused, otherwise the closest shade of it (same hue, different saturation/value) that
// is. Grays have no hue to keep, so only their value changes. Falls back to the color itself when every shade is used.
fun Int.toUnusedShade(usedColors: Set<Int>): Int {
    if (this !in usedColors) return this
    val (hue, saturation, value) = toHsv()
    val alpha = this ushr 24
    val shadeOffsets =
        ((1..MAX_SHADE_STEPS).map { it * SHADE_STEP } + (0 until MAX_SHADE_STEPS).map { (it + 0.5f) * SHADE_STEP })
            .flatMap { offset ->
                listOf(0f to -offset, -offset to 0f, 0f to offset, offset to 0f, -offset to -offset, offset to offset)
            }
    return shadeOffsets
        .asSequence()
        .filter { (saturationOffset, _) -> saturation > 0f || saturationOffset == 0f }
        .map { (saturationOffset, valueOffset) -> saturation + saturationOffset to value + valueOffset }
        .filter { (shadeSaturation, shadeValue) ->
            (shadeSaturation == saturation || shadeSaturation in MIN_SATURATION..1f) &&
                (shadeValue == value || shadeValue in MIN_VALUE..1f)
        }.map { (shadeSaturation, shadeValue) -> Hsv(hue = hue, saturation = shadeSaturation, value = shadeValue).toColor(alpha) }
        .firstOrNull { it !in usedColors }
        ?: this
}
