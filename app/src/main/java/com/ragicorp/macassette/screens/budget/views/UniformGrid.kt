package com.ragicorp.macassette.screens.budget.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth

// Grid whose cells all share the size of the tallest item, so wrapped text grows every cell instead of a single one.
// Without rowCellWidth, the items fill spanCount columns sharing the width, otherwise spanCount rows of cells that wide.
@Composable
fun UniformGrid(
    spanCount: Int,
    spacing: Dp,
    modifier: Modifier = Modifier,
    rowCellWidth: Dp? = null,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val cellWidth =
            (rowCellWidth?.roundToPx() ?: ((constraints.maxWidth - spacingPx * (spanCount - 1)) / spanCount)).coerceAtLeast(0)
        val cellHeight = measurables.maxOfOrNull { it.maxIntrinsicHeight(cellWidth) } ?: 0
        val placeables = measurables.map { it.measure(Constraints.fixed(cellWidth, cellHeight)) }
        val lineCount = (placeables.size + spanCount - 1) / spanCount
        val columnCount = if (rowCellWidth == null) spanCount else lineCount
        val rowCount = if (rowCellWidth == null) lineCount else spanCount
        val width = (columnCount * (cellWidth + spacingPx) - spacingPx).coerceAtLeast(0)
        val height = (rowCount * (cellHeight + spacingPx) - spacingPx).coerceAtLeast(0)

        layout(constraints.constrainWidth(width), constraints.constrainHeight(height)) {
            placeables.forEachIndexed { index, placeable ->
                val column = if (rowCellWidth == null) index % spanCount else index / spanCount
                val row = if (rowCellWidth == null) index / spanCount else index % spanCount
                placeable.place(column * (cellWidth + spacingPx), row * (cellHeight + spacingPx))
            }
        }
    }
}
