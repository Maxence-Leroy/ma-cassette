package com.ragicorp.macassette.screens.budget

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.screens.budget.views.BudgetCard

private const val GRID_SPAN_COUNT = 2

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = viewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val space = dimensionResource(R.dimen.double_space)
    val fabClearance = dimensionResource(R.dimen.fab_clearance)

    if (categories.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.budget_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else if (isLandscape) {
        LazyHorizontalGrid(
            rows = GridCells.Fixed(GRID_SPAN_COUNT),
            modifier = modifier.fillMaxSize(),
            // End padding keeps the last cards clear of the FAB menu
            contentPadding = PaddingValues(start = space, top = space, end = fabClearance, bottom = space),
            horizontalArrangement = Arrangement.spacedBy(space),
            verticalArrangement = Arrangement.spacedBy(space),
        ) {
            items(categories, key = { it.id }) { category ->
                BudgetCard(
                    category = category,
                    currency = viewModel.currency,
                    modifier = Modifier.width(dimensionResource(R.dimen.budget_card_width)).fillMaxHeight(),
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_SPAN_COUNT),
            modifier = modifier.fillMaxSize(),
            // Bottom padding keeps the last cards clear of the FAB menu
            contentPadding = PaddingValues(start = space, top = space, end = space, bottom = fabClearance),
            horizontalArrangement = Arrangement.spacedBy(space),
            verticalArrangement = Arrangement.spacedBy(space),
        ) {
            items(categories, key = { it.id }) { category ->
                BudgetCard(category = category, currency = viewModel.currency)
            }
        }
    }
}
