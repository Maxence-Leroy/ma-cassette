package com.ragicorp.macassette.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints

// Moves the content up by -offset and shrinks to what is left visible, reading the offset at layout time only
fun Modifier.collapsible(
    offset: () -> Int,
    onHeightChange: (Int) -> Unit,
) = clipToBounds().layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    onHeightChange(placeable.height)
    val currentOffset = offset().coerceIn(-placeable.height, 0)
    layout(placeable.width, placeable.height + currentOffset) {
        placeable.place(0, currentOffset)
    }
}
