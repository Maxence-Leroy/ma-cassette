package com.ragicorp.macassette.screens.operations.views

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import dev.vicart.compose.material.symbols.MaterialSymbol

// The chips scroll sideways when the screen is too narrow, so the clear button always stays visible
@Composable
fun QuickFilters(
    isUncategorizedOnly: Boolean,
    onUncategorizedOnlyToggle: () -> Unit,
    isWaitingForActionOnly: Boolean,
    onWaitingForActionOnlyToggle: () -> Unit,
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
        ) {
            FilterChip(
                selected = isUncategorizedOnly,
                onClick = onUncategorizedOnlyToggle,
                label = { Text(stringResource(R.string.operations_noCategory)) },
                leadingIcon = if (isUncategorizedOnly) ({ MaterialSymbol.Filled("check") }) else null,
            )
            FilterChip(
                selected = isWaitingForActionOnly,
                onClick = onWaitingForActionOnlyToggle,
                label = { Text(stringResource(R.string.operations_waitingForAction)) },
                leadingIcon = if (isWaitingForActionOnly) ({ MaterialSymbol.Filled("check") }) else null,
            )
        }
        TextButton(onClick = onClearFilters, enabled = hasFilters) {
            Text(stringResource(R.string.operations_clearFilters))
        }
    }
}
