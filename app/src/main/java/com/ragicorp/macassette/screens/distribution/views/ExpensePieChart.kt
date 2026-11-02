package com.ragicorp.macassette.screens.distribution.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.min
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.CategoryExpense
import com.ragicorp.macassette.utils.formatAmount
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import java.text.NumberFormat
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

// The pie takes 40% of the width at most, leaving the rest to the outer labels on both sides
private const val PIE_WIDTH_RATIO = 0.4f

// Long names wrap before being ellipsized
private const val NAME_MAX_LINES = 2
private const val PIE_HEIGHT_RATIO = 0.7f

// Distance from the center to a percentage label, as a ratio of the radius
private const val INNER_LABEL_RADIUS_RATIO = 0.65f

// Slices start at the top instead of the right, going clockwise
private const val START_DEGREE = -90f

private const val LIGHT_COLOR_LUMINANCE = 0.5f

@Composable
fun ExpensePieChart(
    expenses: List<CategoryExpense>,
    currency: String,
    modifier: Modifier = Modifier,
) {
    // Uncategorized operations look the same as in the operations list
    val noCategoryColor = MaterialTheme.colorScheme.outlineVariant
    val noCategoryName = stringResource(R.string.distribution_noCategory)
    val colors = expenses.map { expense -> expense.category?.let { Color(it.color) } ?: noCategoryColor }
    val names = expenses.map { it.category?.name ?: noCategoryName }
    val pies = remember(expenses, colors) { expenses.mapIndexed { index, it -> Pie(data = it.amount, color = colors[index]) } }
    val percentFormat = remember { NumberFormat.getPercentInstance() }
    val total = expenses.sumOf { it.amount }
    val percentages = expenses.map { percentFormat.format(it.amount / total) }
    val amounts = expenses.map { stringResource(R.string.distribution_amount, it.amount.formatAmount(), currency) }
    val textMeasurer = rememberTextMeasurer()
    val percentageStyle = MaterialTheme.typography.labelLarge
    val nameStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
    val amountStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val lineColor = MaterialTheme.colorScheme.outline
    val leaderLength = dimensionResource(R.dimen.distribution_leader_line)
    val labelSpacing = dimensionResource(R.dimen.distribution_label_spacing)
    val lineWidth = dimensionResource(R.dimen.distribution_leader_stroke)

    BoxWithConstraints(modifier = modifier) {
        val diameter = min(maxWidth * PIE_WIDTH_RATIO, maxHeight * PIE_HEIGHT_RATIO)
        PieChart(
            modifier = Modifier.size(diameter).align(Alignment.Center),
            data = pies,
            startDegree = START_DEGREE,
            style = Pie.Style.Fill,
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = diameter.toPx() / 2
            val leader = leaderLength.toPx()
            val spacing = labelSpacing.toPx()
            val outerLabelWidth = (size.width / 2 - radius - leader - spacing).toInt().coerceAtLeast(0)
            var sliceStart = START_DEGREE
            val outerLabels =
                expenses.mapIndexed { index, expense ->
                    val sweep = (expense.amount / total * 360).toFloat()
                    val middle = Math.toRadians((sliceStart + sweep / 2).toDouble())
                    sliceStart += sweep
                    val direction = Offset(cos(middle).toFloat(), sin(middle).toFloat())
                    drawPercentage(
                        textMeasurer = textMeasurer,
                        text = percentages[index],
                        style = percentageStyle,
                        sliceColor = colors[index],
                        position = center + direction * radius * INNER_LABEL_RADIUS_RATIO,
                    )
                    val isOnRight = direction.x >= 0
                    // Left labels are right-aligned against their line
                    val textAlign = if (isOnRight) TextAlign.Start else TextAlign.End
                    OuterLabel(
                        edge = center + direction * radius,
                        elbow = center + direction * (radius + leader),
                        isOnRight = isOnRight,
                        name =
                            textMeasurer.measureLabel(
                                names[index],
                                nameStyle.copy(textAlign = textAlign),
                                outerLabelWidth,
                                NAME_MAX_LINES,
                            ),
                        amount = textMeasurer.measureLabel(amounts[index], amountStyle.copy(textAlign = textAlign), outerLabelWidth, 1),
                    )
                }
            outerLabels.partition { it.isOnRight }.toList().forEach { sideLabels ->
                val tops = spreadVertically(sideLabels, spacing)
                sideLabels.forEachIndexed { index, label ->
                    drawOuterLabel(
                        label,
                        top = tops[index],
                        radius = radius,
                        leader = leader,
                        spacing = spacing,
                        lineColor,
                        lineWidth.toPx(),
                    )
                }
            }
        }
    }
}

private class OuterLabel(
    val edge: Offset,
    val elbow: Offset,
    val isOnRight: Boolean,
    val name: TextLayoutResult,
    val amount: TextLayoutResult,
) {
    val height = name.size.height + amount.size.height
}

private fun TextMeasurer.measureLabel(
    text: String,
    style: TextStyle,
    maxWidth: Int,
    maxLines: Int,
) = measure(
    text = text,
    style = style,
    overflow = TextOverflow.Ellipsis,
    maxLines = maxLines,
    constraints = Constraints(maxWidth = maxWidth),
)

// White on dark slices and black on light ones, so the percentage stays readable whatever the category's color
private fun DrawScope.drawPercentage(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    sliceColor: Color,
    position: Offset,
) {
    val layout = textMeasurer.measure(text = text, style = style)
    drawText(
        textLayoutResult = layout,
        color = if (sliceColor.luminance() > LIGHT_COLOR_LUMINANCE) Color.Black else Color.White,
        topLeft = position - Offset(layout.size.width / 2f, layout.size.height / 2f),
    )
}

// Tops of the labels of one side, as close to their slice as possible without overlapping each other or leaving the
// canvas: pushed down past the previous label first, then back up from the bottom edge
private fun DrawScope.spreadVertically(
    labels: List<OuterLabel>,
    spacing: Float,
): List<Float> {
    val order = labels.indices.sortedBy { labels[it].elbow.y }
    val tops = FloatArray(labels.size)
    var minTop = 0f
    order.forEach { index ->
        tops[index] = max(labels[index].elbow.y - labels[index].height / 2f, minTop)
        minTop = tops[index] + labels[index].height + spacing
    }
    var maxBottom = size.height
    order.reversed().forEach { index ->
        tops[index] = minOf(tops[index], maxBottom - labels[index].height).coerceAtLeast(0f)
        maxBottom = tops[index] - spacing
    }
    return tops.toList()
}

// A line from the slice out to the label, then the category's name with the amount below it, right-aligned on the left
private fun DrawScope.drawOuterLabel(
    label: OuterLabel,
    top: Float,
    radius: Float,
    leader: Float,
    spacing: Float,
    lineColor: Color,
    lineWidth: Float,
) {
    val lineY = top + label.height / 2f
    val lineEndX = if (label.isOnRight) center.x + radius + leader else center.x - radius - leader
    drawLine(color = lineColor, start = label.edge, end = Offset(label.elbow.x, lineY), strokeWidth = lineWidth)
    drawLine(color = lineColor, start = Offset(label.elbow.x, lineY), end = Offset(lineEndX, lineY), strokeWidth = lineWidth)
    listOf(label.name to top, label.amount to top + label.name.size.height).forEach { (layout, lineTop) ->
        val x = if (label.isOnRight) lineEndX + spacing else lineEndX - spacing - layout.size.width
        drawText(textLayoutResult = layout, topLeft = Offset(x, lineTop))
    }
}
