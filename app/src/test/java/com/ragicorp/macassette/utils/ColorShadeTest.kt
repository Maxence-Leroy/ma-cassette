package com.ragicorp.macassette.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ColorShadeTest {
    private val red = 0xFFE53935.toInt()
    private val gray = 0xFF757575.toInt()

    @Test
    fun `an unused color is kept`() {
        assertEquals(red, red.toUnusedShade(setOf(gray)))
    }

    @Test
    fun `a used color is shifted to a darker shade first`() {
        val shade = red.toUnusedShade(setOf(red))

        // The value of a color is its brightest channel, out of 255: a 0.2 step is about 51
        assertEquals(red.maxChannel() - 51.0, shade.maxChannel().toDouble(), 1.0)
    }

    @Test
    fun `used shades are skipped`() {
        val firstShade = red.toUnusedShade(setOf(red))

        val secondShade = red.toUnusedShade(setOf(red, firstShade))

        assertNotEquals(red, secondShade)
        assertNotEquals(firstShade, secondShade)
    }

    @Test
    fun `a shade keeps the hue and alpha of the color`() {
        val shade = red.toUnusedShade(setOf(red))

        assertEquals(0xFF, shade ushr 24)
        assertEquals(red.hue(), shade.hue(), 1f)
    }

    @Test
    fun `a gray only changes value so it stays gray`() {
        val shade = gray.toUnusedShade(setOf(gray))

        assertNotEquals(gray, shade)
        assertEquals(shade shr 16 and 0xFF, shade shr 8 and 0xFF)
        assertEquals(shade shr 8 and 0xFF, shade and 0xFF)
    }

    @Test
    fun `each new category of a used color gets a distinct shade`() {
        val usedColors = mutableSetOf(red)

        repeat(10) { usedColors += red.toUnusedShade(usedColors) }

        assertEquals(11, usedColors.size)
    }

    private fun Int.maxChannel() = maxOf(this shr 16 and 0xFF, this shr 8 and 0xFF, this and 0xFF)

    private fun Int.hue(): Float {
        val red = this shr 16 and 0xFF
        val green = this shr 8 and 0xFF
        val blue = this and 0xFF
        val max = maxOf(red, green, blue)
        val delta = (max - minOf(red, green, blue)).toFloat()
        return when (max) {
            red -> 60f * ((green - blue) / delta).mod(6f)
            green -> 60f * ((blue - red) / delta + 2f)
            else -> 60f * ((red - green) / delta + 4f)
        }
    }
}
