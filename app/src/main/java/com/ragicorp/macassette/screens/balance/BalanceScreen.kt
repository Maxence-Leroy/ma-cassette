package com.ragicorp.macassette.screens.balance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.ragicorp.macassette.screens.balance.views.BalanceChart

@Composable
fun BalanceScreen(
    modifier: Modifier = Modifier,
    viewModel: BalanceViewModel = viewModel(),
) {
    val monthBalances by viewModel.monthBalances.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val balances = monthBalances ?: return@Box
        if (balances.isEmpty()) {
            Text(
                text = stringResource(R.string.balance_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // Bottom padding keeps the chart clear of the FAB menu
            BalanceChart(
                monthBalances = balances,
                currency = viewModel.currency,
                modifier =
                    Modifier.fillMaxSize().padding(
                        start = dimensionResource(R.dimen.double_space),
                        top = dimensionResource(R.dimen.double_space),
                        end = dimensionResource(R.dimen.double_space),
                        bottom = dimensionResource(R.dimen.fab_clearance),
                    ),
            )
        }
    }
}
