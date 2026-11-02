package com.ragicorp.macassette.screens.balance.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.MonthBalance
import com.ragicorp.macassette.ui.theme.EarningsColor
import com.ragicorp.macassette.ui.theme.ExpensesColor
import com.ragicorp.macassette.utils.axisScale
import com.ragicorp.macassette.utils.formatAmount
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import org.threeten.bp.format.DateTimeFormatter

private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM")

// The axis gets about this many steps between round amounts
private const val TARGET_AXIS_INTERVALS = 5

@Composable
fun BalanceChart(
    monthBalances: List<MonthBalance>,
    currency: String,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val expensesLabel = stringResource(R.string.balance_expenses)
    val earningsLabel = stringResource(R.string.balance_earnings)
    val balanceLabel = stringResource(R.string.balance_balance)
    val balanceColor = MaterialTheme.colorScheme.primary
    val lines =
        remember(monthBalances, expensesLabel, earningsLabel, balanceLabel, balanceColor) {
            listOf(
                line(expensesLabel, monthBalances.map { it.expenses }, ExpensesColor),
                line(earningsLabel, monthBalances.map { it.earnings }, EarningsColor),
                line(balanceLabel, monthBalances.map { it.balance }, balanceColor),
            )
        }
    val months = remember(monthBalances) { monthBalances.map { it.month.format(MONTH_FORMATTER) } }
    val scale = remember(monthBalances) { axisScale(lines.flatMap { it.values }, TARGET_AXIS_INTERVALS) }
    val axisTextStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val gridColor = SolidColor(MaterialTheme.colorScheme.outlineVariant)

    LineChart(
        modifier = modifier,
        data = lines,
        curvedEdges = false,
        // One horizontal line per axis amount, one vertical line per month
        gridProperties =
            GridProperties(
                xAxisProperties = GridProperties.AxisProperties(color = gridColor, lineCount = scale.tickCount),
                yAxisProperties = GridProperties.AxisProperties(color = gridColor, lineCount = monthBalances.size),
            ),
        zeroLineProperties =
            ZeroLineProperties(
                enabled = true,
                color = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                thickness = dimensionResource(R.dimen.balance_zero_line),
            ),
        indicatorProperties =
            HorizontalIndicatorProperties(
                textStyle = axisTextStyle,
                count = IndicatorCount.CountBased(scale.tickCount),
                contentBuilder = { resources.getString(R.string.balance_amount, it.formatAmount(), currency) },
            ),
        labelHelperProperties =
            LabelHelperProperties(
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            ),
        labelProperties = LabelProperties(enabled = true, textStyle = axisTextStyle, labels = months),
        popupProperties =
            PopupProperties(
                textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.inverseOnSurface),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentBuilder = { resources.getString(R.string.balance_amount, it.value.formatAmount(), currency) },
            ),
        minValue = scale.min,
        maxValue = scale.max,
    )
}

private fun line(
    label: String,
    values: List<Double>,
    color: Color,
) = Line(
    label = label,
    values = values,
    color = SolidColor(color),
    dotProperties = DotProperties(enabled = true, color = SolidColor(color), strokeColor = SolidColor(color)),
)
