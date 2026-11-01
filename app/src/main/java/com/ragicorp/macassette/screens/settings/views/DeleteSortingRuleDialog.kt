package com.ragicorp.macassette.screens.settings.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.SortingRule
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun DeleteSortingRuleDialog(
    sortingRule: SortingRule,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.settings_deleteSortingRuleConfirm))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        icon = { MaterialSymbol.Filled("delete") },
        title = { Text(stringResource(R.string.settings_deleteSortingRuleTitle)) },
        text = { Text(stringResource(R.string.settings_deleteSortingRuleMessage, sortingRule.textToRecognize)) },
    )
}
