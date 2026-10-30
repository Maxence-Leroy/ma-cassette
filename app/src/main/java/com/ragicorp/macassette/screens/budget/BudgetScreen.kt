package com.ragicorp.macassette.screens.budget

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.budget.views.BudgetCard
import com.ragicorp.macassette.screens.budget.views.MonthSelector
import com.ragicorp.macassette.screens.budget.views.UniformGrid
import org.threeten.bp.YearMonth

private const val GRID_SPAN_COUNT = 2

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = viewModel(),
) {
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val firstMonth by viewModel.firstMonth.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val space = dimensionResource(R.dimen.double_space)
    val fabClearance = dimensionResource(R.dimen.fab_clearance)

    Column(modifier = modifier.fillMaxSize()) {
        MonthSelector(
            month = selectedMonth,
            canSelectPrevious = firstMonth?.let { selectedMonth > it } == true,
            canSelectNext = selectedMonth < YearMonth.now(),
            onPreviousClick = viewModel::selectPreviousMonth,
            onNextClick = viewModel::selectNextMonth,
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.single_space)),
        )
        if (budgets.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.budget_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val scrollModifier =
                if (isLandscape) {
                    // End padding keeps the last cards clear of the FAB menu
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                        .padding(start = space, end = fabClearance, bottom = space)
                } else {
                    // Bottom padding keeps the last cards clear of the FAB menu
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(start = space, end = space, bottom = fabClearance)
                }
            UniformGrid(
                spanCount = GRID_SPAN_COUNT,
                spacing = space,
                modifier = Modifier.weight(1f).fillMaxWidth().then(scrollModifier),
                rowCellWidth = if (isLandscape) dimensionResource(R.dimen.budget_card_width) else null,
            ) {
                budgets.forEach { budget ->
                    key(budget.category.id) {
                        BudgetCard(budget = budget, currency = viewModel.currency)
                    }
                }
            }
        }
    }
}
