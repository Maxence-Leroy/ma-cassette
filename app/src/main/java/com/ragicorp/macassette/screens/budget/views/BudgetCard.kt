package com.ragicorp.macassette.screens.budget.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Budget
import com.ragicorp.macassette.utils.formatAmount
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlin.math.abs

@Composable
fun BudgetCard(
    budget: Budget,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val category = budget.category
    val monthlyAmount = remember(category.budget) { category.budget?.formatAmount() }
    val remaining = remember(budget.remaining) { abs(budget.remaining).formatAmount() }
    val isExceeded = budget.remaining < 0
    val categoryColor = Color(category.color)
    // The category's color covers the share of the budget that is left, the rest is black
    val pies =
        remember(category.budget, budget.remaining, categoryColor) {
            val remainingRatio = category.budget?.takeIf { it > 0 }?.let { (budget.remaining / it).coerceIn(0.0, 1.0) } ?: 0.0
            listOf(Pie(data = remainingRatio, color = categoryColor), Pie(data = 1 - remainingRatio, color = Color.Black))
        }

    OutlinedCard(onClick = onClick, modifier = modifier) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.double_space)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Starts at the top instead of the right
                PieChart(
                    modifier = Modifier.size(dimensionResource(R.dimen.category_color)),
                    data = pies,
                    startDegree = -90f,
                    style = Pie.Style.Stroke(width = dimensionResource(R.dimen.budget_pie_stroke)),
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text =
                    stringResource(
                        if (isExceeded) R.string.budget_exceeded else R.string.budget_remaining,
                        remaining,
                        currency,
                    ),
                style = MaterialTheme.typography.titleLarge,
                color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            if (monthlyAmount != null) {
                Text(
                    text = stringResource(R.string.budget_monthlyAmount, monthlyAmount, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
