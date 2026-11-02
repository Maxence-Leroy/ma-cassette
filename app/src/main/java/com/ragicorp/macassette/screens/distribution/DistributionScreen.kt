package com.ragicorp.macassette.screens.distribution

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.distribution.views.ExpensePieChart
import com.ragicorp.macassette.ui.views.MonthSelector
import org.threeten.bp.YearMonth

@Composable
fun DistributionScreen(
    modifier: Modifier = Modifier,
    viewModel: DistributionViewModel = viewModel(),
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val firstMonth by viewModel.firstMonth.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth

    Column(modifier = modifier.fillMaxSize()) {
        MonthSelector(
            month = selectedMonth,
            canSelectPrevious = firstMonth?.let { selectedMonth > it } == true,
            canSelectNext = selectedMonth < YearMonth.now(),
            onPreviousClick = viewModel::selectPreviousMonth,
            onNextClick = viewModel::selectNextMonth,
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.single_space)),
        )
        if (expenses.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.distribution_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // Bottom padding keeps the labels clear of the FAB menu
            ExpensePieChart(
                expenses = expenses,
                currency = viewModel.currency,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(R.dimen.double_space),
                            end = dimensionResource(R.dimen.double_space),
                            bottom = dimensionResource(R.dimen.fab_clearance),
                        ),
            )
        }
    }
}
