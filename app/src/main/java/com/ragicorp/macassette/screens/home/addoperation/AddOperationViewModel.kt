package com.ragicorp.macassette.screens.home.addoperation

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.operation.OperationState
import com.ragicorp.macassette.utils.AMOUNT_REGEX
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class AddOperationViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    val categories: StateFlow<List<Category>> =
        operationRepository
            .getCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _date = mutableStateOf(LocalDate.now())
    val date: State<LocalDate> = _date

    private val _category = mutableStateOf<Category?>(null)
    val category: State<Category?> = _category

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _amount = mutableStateOf("")
    val amount: State<String> = _amount

    private val _isEarning = mutableStateOf(false)
    val isEarning: State<Boolean> = _isEarning

    private val _notes = mutableStateOf("")
    val notes: State<String> = _notes

    val canSave: State<Boolean> =
        derivedStateOf { _category.value != null && _name.value.isNotBlank() && parsedAmount() != null }

    fun setDate(date: LocalDate) {
        _date.value = date
    }

    fun setCategory(category: Category) {
        _category.value = category
    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setAmount(amount: String) {
        if (AMOUNT_REGEX.matches(amount)) _amount.value = amount
    }

    fun setIsEarning(isEarning: Boolean) {
        _isEarning.value = isEarning
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun save(onSaved: () -> Unit) {
        val category = _category.value ?: return
        val amount = parsedAmount() ?: return
        if (_name.value.isBlank()) return
        val operation =
            Operation(
                id = 0,
                category = category,
                date = _date.value,
                // Expenses are stored as negative amounts
                amount = if (_isEarning.value) amount else -amount,
                label = _name.value.trim(),
                notes = _notes.value.trim(),
                isWaitingForAction = false,
                state = OperationState.ManuallyAdded,
            )
        viewModelScope.launch {
            operationRepository.insertOperation(operation)
            onSaved()
        }
    }

    private fun parsedAmount() =
        _amount.value
            .replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it > 0 }
}
