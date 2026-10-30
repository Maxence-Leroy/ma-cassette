package com.ragicorp.macassette.screens.operations.views

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.ragicorp.macassette.R
import dev.vicart.compose.material.symbols.MaterialSymbol
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

private const val MILLIS_PER_DAY = 86_400_000L

// Read-only field opening a date picker limited to minDate..maxDate, each bound being optional. Clearing it sets a null date.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterField(
    date: LocalDate?,
    label: String,
    onDateChange: (LocalDate?) -> Unit,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)).orEmpty(),
        onValueChange = {},
        // A read-only text field consumes clicks, so intercept them before it does
        modifier =
            modifier.pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    if (waitForUpOrCancellation(pass = PointerEventPass.Initial) != null) showDatePicker = true
                }
            },
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { MaterialSymbol.Filled("calendar_month") },
        singleLine = true,
    )

    if (showDatePicker) {
        // DatePicker works in UTC milliseconds, so epoch days map to it without any time zone
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = date?.let { it.toEpochDay() * MILLIS_PER_DAY },
                yearRange = (minDate?.year ?: DatePickerDefaults.YearRange.first)..(maxDate?.year ?: DatePickerDefaults.YearRange.last),
                selectableDates =
                    object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val day = LocalDate.ofEpochDay(Math.floorDiv(utcTimeMillis, MILLIS_PER_DAY))
                            return (minDate == null || day >= minDate) && (maxDate == null || day <= maxDate)
                        }

                        override fun isSelectableYear(year: Int) =
                            (minDate == null || year >= minDate.year) && (maxDate == null || year <= maxDate.year)
                    },
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateChange(LocalDate.ofEpochDay(it / MILLIS_PER_DAY)) }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onDateChange(null)
                            showDatePicker = false
                        },
                    ) {
                        Text(stringResource(R.string.operations_clearDate))
                    }
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            },
        ) {
            // The dialog is too short for the whole calendar in landscape, so let it scroll above the buttons
            DatePicker(state = datePickerState, modifier = Modifier.verticalScroll(rememberScrollState()))
        }
    }
}
