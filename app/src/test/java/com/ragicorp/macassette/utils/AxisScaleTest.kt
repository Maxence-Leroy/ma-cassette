package com.ragicorp.macassette.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class AxisScaleTest {
    @Test
    fun `bounds and step are round and cover the values`() {
        assertEquals(AxisScale(min = -500.0, max = 2000.0, step = 500.0), axisScale(listOf(-311.7, 1688.3, 2000.0, 12.0), 5))
    }

    @Test
    fun `0 is always within the scale`() {
        assertEquals(AxisScale(min = 0.0, max = 1000.0, step = 200.0), axisScale(listOf(400.0, 950.0), 5))
        assertEquals(AxisScale(min = -1000.0, max = 0.0, step = 200.0), axisScale(listOf(-950.0, -400.0), 5))
    }

    @Test
    fun `0 is one of the ticks`() {
        val scale = axisScale(listOf(-1234.0, 5678.0), 5)
        val ticks = (0 until scale.tickCount).map { scale.max - it * scale.step }

        assertEquals(true, 0.0 in ticks)
    }

    @Test
    fun `values all at 0 get a single step`() {
        assertEquals(AxisScale(min = 0.0, max = 1.0, step = 1.0), axisScale(listOf(0.0, 0.0), 5))
    }

    @Test
    fun `the tick count includes both bounds`() {
        assertEquals(6, AxisScale(min = -500.0, max = 2000.0, step = 500.0).tickCount)
    }
}
