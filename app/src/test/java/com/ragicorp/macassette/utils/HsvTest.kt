package com.ragicorp.macassette.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class HsvTest {
    @Test
    fun `black has no hue, saturation nor value`() {
        assertEquals(Hsv(hue = 0f, saturation = 0f, value = 0f), 0xFF000000.toInt().toHsv())
    }

    @Test
    fun `grays have no hue nor saturation`() {
        assertEquals(Hsv(hue = 0f, saturation = 0f, value = 1f), 0xFFFFFFFF.toInt().toHsv())
        assertEquals(Hsv(hue = 0f, saturation = 0f, value = 0x80 / 255f), 0xFF808080.toInt().toHsv())
    }

    @Test
    fun `the hue comes from the brightest channel`() {
        assertEquals(Hsv(hue = 0f, saturation = 1f, value = 1f), 0xFFFF0000.toInt().toHsv())
        assertEquals(Hsv(hue = 120f, saturation = 1f, value = 1f), 0xFF00FF00.toInt().toHsv())
        assertEquals(Hsv(hue = 240f, saturation = 1f, value = 1f), 0xFF0000FF.toInt().toHsv())
    }

    @Test
    fun `a red hue below 0 wraps around to the end of the circle`() {
        assertEquals(Hsv(hue = 300f, saturation = 1f, value = 1f), 0xFFFF00FF.toInt().toHsv())
    }

    @Test
    fun `the saturation is the share of the brightest channel not in the dimmest one`() {
        val hsv = 0xFFFF8080.toInt().toHsv()

        // 0x80 is 128/255, a bit over half
        assertEquals(1 - 0x80 / 255f, hsv.saturation, 0.0001f)
        assertEquals(1f, hsv.value, 0f)
    }

    @Test
    fun `the alpha is ignored`() {
        assertEquals(0xFFFF0000.toInt().toHsv(), 0x80FF0000.toInt().toHsv())
    }

    @Test
    fun `each sixth of the hue circle gives its color`() {
        val colorByHue =
            mapOf(
                0f to 0xFFFF0000,
                60f to 0xFFFFFF00,
                120f to 0xFF00FF00,
                180f to 0xFF00FFFF,
                240f to 0xFF0000FF,
                300f to 0xFFFF00FF,
            )

        colorByHue.forEach { (hue, color) ->
            assertEquals(color.toInt(), Hsv(hue = hue, saturation = 1f, value = 1f).toColor(alpha = 0xFF))
        }
    }

    @Test
    fun `hues between the sixths blend their two colors`() {
        assertEquals(0xFFFF8000.toInt(), Hsv(hue = 30f, saturation = 1f, value = 1f).toColor(alpha = 0xFF))
        assertEquals(0xFF8000FF.toInt(), Hsv(hue = 270f, saturation = 1f, value = 1f).toColor(alpha = 0xFF))
    }

    @Test
    fun `saturation and value lighten and darken the color`() {
        assertEquals(0xFFFF8080.toInt(), Hsv(hue = 0f, saturation = 0.5f, value = 1f).toColor(alpha = 0xFF))
        assertEquals(0xFF800000.toInt(), Hsv(hue = 0f, saturation = 1f, value = 0.5f).toColor(alpha = 0xFF))
        assertEquals(0xFF808080.toInt(), Hsv(hue = 0f, saturation = 0f, value = 0.5f).toColor(alpha = 0xFF))
    }

    @Test
    fun `the given alpha is kept`() {
        assertEquals(0x80FF0000.toInt(), Hsv(hue = 0f, saturation = 1f, value = 1f).toColor(alpha = 0x80))
    }

    @Test
    fun `category colors survive a round trip`() {
        listOf(0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF43A047, 0xFF00897B, 0xFF1E88E5, 0xFF5E35B1, 0xFFD81B60, 0xFF6D4C41, 0xFF757575)
            .map { it.toInt() }
            .forEach { assertEquals(it, it.toHsv().toColor(alpha = 0xFF)) }
    }
}
