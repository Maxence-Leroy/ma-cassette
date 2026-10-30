package com.ragicorp.macassette.screens.operations.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Category

// Dropdown staying open while categories are checked, so several can be picked in a row
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilter(
    categories: List<Category>,
    selectedCategoryIds: Set<Long>,
    onCategoryToggle: (Category) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedCategories = categories.filter { it.id in selectedCategoryIds }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value =
                when (selectedCategories.size) {
                    0 -> stringResource(R.string.operations_allCategories)
                    1 -> selectedCategories.single().name
                    else -> pluralStringResource(R.plurals.operations_categoryCount, selectedCategories.size, selectedCategories.size)
                },
            onValueChange = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text(stringResource(R.string.operations_categoryFilter)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.operations_allCategories)) },
                onClick = onClear,
                trailingIcon = { Checkbox(checked = selectedCategories.isEmpty(), onCheckedChange = null) },
            )
            categories.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = { onCategoryToggle(it) },
                    leadingIcon = {
                        Box(
                            modifier =
                                Modifier
                                    .size(dimensionResource(R.dimen.category_color))
                                    .clip(CircleShape)
                                    .background(Color(it.color)),
                        )
                    },
                    trailingIcon = { Checkbox(checked = it.id in selectedCategoryIds, onCheckedChange = null) },
                )
            }
        }
    }
}
