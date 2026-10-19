package com.ragicorp.macassette.screens.settings.addcategory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.ui.theme.CategoryColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddCategoryViewModel = viewModel(),
) {
    val name by viewModel.name
    val color by viewModel.color
    val budget by viewModel.budget
    val isEarning by viewModel.isEarning

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState =
            rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = dimensionResource(R.dimen.double_space),
                        end = dimensionResource(R.dimen.double_space),
                        bottom = dimensionResource(R.dimen.double_space),
                    ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
        ) {
            Text(
                text = stringResource(R.string.settings_addCategory),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::setName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.addCategory_name)) },
                singleLine = true,
            )
            EarningSelector(
                isEarning = isEarning,
                onIsEarningChange = viewModel::setIsEarning,
            )
            AnimatedVisibility(visible = !isEarning) {
                OutlinedTextField(
                    value = budget,
                    onValueChange = viewModel::setBudget,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.addCategory_budget)) },
                    suffix = { Text(viewModel.currency) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
            Text(
                text = stringResource(R.string.addCategory_color),
                style = MaterialTheme.typography.titleSmall,
            )
            ColorSelector(
                selectedColor = color,
                onColorSelect = viewModel::setColor,
            )
        }
    }
}

@Composable
private fun EarningSelector(
    isEarning: Boolean,
    onIsEarningChange: (Boolean) -> Unit,
) {
    val options = listOf(false to R.string.addCategory_expense, true to R.string.addCategory_earning)
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, text) ->
            SegmentedButton(
                selected = isEarning == value,
                onClick = { onIsEarningChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(stringResource(text))
            }
        }
    }
}

@Composable
private fun ColorSelector(
    selectedColor: Int,
    onColorSelect: (Int) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
    ) {
        CategoryColors.forEach {
            val color = it.toArgb()
            Box(
                modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.color_swatch))
                        .clip(CircleShape)
                        .background(it)
                        .border(
                            width = dimensionResource(R.dimen.color_swatch_border),
                            color = if (color == selectedColor) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape,
                        ).selectable(
                            selected = color == selectedColor,
                            onClick = { onColorSelect(color) },
                            role = Role.RadioButton,
                        ),
            )
        }
    }
}
