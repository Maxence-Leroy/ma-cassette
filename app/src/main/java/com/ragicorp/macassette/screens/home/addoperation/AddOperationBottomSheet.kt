package com.ragicorp.macassette.screens.home.addoperation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragicorp.macassette.R
import com.ragicorp.macassette.operation.Category
import dev.vicart.compose.material.symbols.MaterialSymbol
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

private const val MILLIS_PER_DAY = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOperationBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddOperationViewModel = viewModel(),
) {
    val date by viewModel.date
    val category by viewModel.category
    val name by viewModel.name
    val amount by viewModel.amount
    val isEarning by viewModel.isEarning
    val notes by viewModel.notes
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

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
                text = stringResource(R.string.home_addOperation),
                style = MaterialTheme.typography.titleLarge,
            )
            DateField(
                date = date,
                onClick = { showDatePicker = true },
            )
            CategoryDropdown(
                categories = viewModel.categories,
                selectedCategory = category,
                onCategorySelect = viewModel::setCategory,
            )
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::setName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.addOperation_name)) },
                singleLine = true,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.double_space)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = viewModel::setAmount,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.addOperation_amount)) },
                    suffix = { Text(viewModel.currency) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                EarningSelector(
                    isEarning = isEarning,
                    onIsEarningChange = viewModel::setIsEarning,
                )
            }
            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::setNotes,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.addOperation_notes)) },
                minLines = 3,
            )
        }
    }

    if (showDatePicker) {
        // DatePicker works in UTC milliseconds, so epoch days map to it without any time zone
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochDay() * MILLIS_PER_DAY)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.setDate(LocalDate.ofEpochDay(it / MILLIS_PER_DAY)) }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun EarningSelector(
    isEarning: Boolean,
    onIsEarningChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.single_space)),
    ) {
        listOf(false to R.string.addOperation_expense, true to R.string.addOperation_earning).forEach { (value, text) ->
            Row(
                modifier =
                    Modifier.selectable(
                        selected = isEarning == value,
                        onClick = { onIsEarningChange(value) },
                        role = Role.RadioButton,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = isEarning == value, onClick = null)
                Text(
                    text = stringResource(text),
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.single_space)),
                )
            }
        }
    }
}

@Composable
private fun DateField(
    date: LocalDate,
    onClick: () -> Unit,
) {
    OutlinedTextField(
        value = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
        onValueChange = {},
        // A read-only text field consumes clicks, so intercept them before it does
        modifier =
            Modifier.fillMaxWidth().pointerInput(onClick) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    if (waitForUpOrCancellation(pass = PointerEventPass.Initial) != null) onClick()
                }
            },
        readOnly = true,
        label = { Text(stringResource(R.string.addOperation_date)) },
        trailingIcon = { MaterialSymbol.Filled("calendar_month") },
        singleLine = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelect: (Category) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedCategory?.name.orEmpty(),
            onValueChange = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text(stringResource(R.string.addOperation_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            categories.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onCategorySelect(it)
                        expanded = false
                    },
                )
            }
        }
    }
}
