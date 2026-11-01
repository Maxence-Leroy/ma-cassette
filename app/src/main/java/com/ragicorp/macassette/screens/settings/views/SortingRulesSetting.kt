package com.ragicorp.macassette.screens.settings.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.SortingRule
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun SortingRulesSetting(
    sortingRules: List<SortingRule>,
    onAddClick: () -> Unit,
    onEditClick: (SortingRule) -> Unit,
    onDeleteSortingRule: (SortingRule) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var sortingRuleIdToDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(modifier = modifier) {
        ListItem(
            onClick = { expanded = !expanded },
            supportingContent =
                if (expanded) {
                    null
                } else {
                    ({ Text(pluralStringResource(R.plurals.settings_sortingRuleCount, sortingRules.size, sortingRules.size)) })
                },
            trailingContent = { MaterialSymbol.Filled(if (expanded) "keyboard_arrow_up" else "keyboard_arrow_down") },
        ) {
            Text(stringResource(R.string.settings_sortingRules))
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.double_space)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
            ) {
                sortingRules.forEach {
                    SortingRuleCard(
                        sortingRule = it,
                        onEditClick = { onEditClick(it) },
                        onDeleteClick = { sortingRuleIdToDelete = it.id },
                    )
                }
                TextButton(
                    onClick = onAddClick,
                    contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
                ) {
                    MaterialSymbol.Filled("add")
                    Text(
                        text = stringResource(R.string.settings_addSortingRule),
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.single_space)),
                    )
                }
            }
        }
    }

    sortingRules.find { it.id == sortingRuleIdToDelete }?.let { sortingRule ->
        DeleteSortingRuleDialog(
            sortingRule = sortingRule,
            onConfirm = {
                onDeleteSortingRule(sortingRule)
                sortingRuleIdToDelete = null
            },
            onDismissRequest = { sortingRuleIdToDelete = null },
        )
    }
}
