package com.ragicorp.macassette.screens.home.operationform

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.operation.OperationState
import com.ragicorp.macassette.utils.AMOUNT_REGEX
import com.ragicorp.macassette.utils.Preferences
import com.ragicorp.macassette.utils.formatAmount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import kotlin.math.abs

class OperationFormViewModel(
    application: Application,
    private val editedOperation: Operation?,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    val categories: StateFlow<List<Category>> =
        operationRepository
            .getCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _date = mutableStateOf(editedOperation?.date ?: LocalDate.now())
    val date: State<LocalDate> = _date

    private val _category = mutableStateOf(editedOperation?.category)
    val category: State<Category?> = _category

    private val _name = mutableStateOf(editedOperation?.label ?: "")
    val name: State<String> = _name

    // Shown as a positive number: the sign is carried by the expense / earning selector
    private val _amount =
        mutableStateOf(
            editedOperation?.amount?.let { abs(it).formatAmount(isGroupingUsed = false) } ?: "",
        )
    val amount: State<String> = _amount

    private val _isEarning = mutableStateOf(editedOperation?.let { it.amount > 0 } ?: false)
    val isEarning: State<Boolean> = _isEarning

    private val _isWaitingForAction = mutableStateOf(editedOperation?.isWaitingForAction ?: false)
    val isWaitingForAction: State<Boolean> = _isWaitingForAction

    private val _notes = mutableStateOf(editedOperation?.notes ?: "")
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

    fun setIsWaitingForAction(isWaitingForAction: Boolean) {
        _isWaitingForAction.value = isWaitingForAction
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
                id = editedOperation?.id ?: 0,
                category = category,
                date = _date.value,
                // Expenses are stored as negative amounts
                amount = if (_isEarning.value) amount else -amount,
                label = _name.value.trim(),
                notes = _notes.value.trim(),
                recurrentPeriod = editedOperation?.recurrentPeriod,
                isWaitingForAction = _isWaitingForAction.value,
                state = editedOperation?.state ?: OperationState.ManuallyAdded,
            )
        viewModelScope.launch {
            if (editedOperation == null) {
                operationRepository.insertOperation(operation)
            } else {
                operationRepository.updateOperation(operation)
            }
            onSaved()
        }
    }

    private fun parsedAmount() =
        _amount.value
            .replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it > 0 }

    // The default viewModel() factory can only pass the Application, so this one also passes the operation being
    // edited (null when adding a new one). It's only used when the ViewModel is first created, which is why the sheet
    // gets its own ViewModelStoreOwner each time it opens.
    class OperationFormViewModelFactory(
        private val application: Application,
        private val editedOperation: Operation?,
    ) : ViewModelProvider.NewInstanceFactory() {
        // Only ever asked for OperationFormViewModel, so the cast to T is safe
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = OperationFormViewModel(application, editedOperation) as T
    }
}
