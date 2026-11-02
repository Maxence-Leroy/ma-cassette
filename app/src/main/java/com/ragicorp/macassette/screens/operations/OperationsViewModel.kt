package com.ragicorp.macassette.screens.operations

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationFilter
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class OperationsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    val categories: StateFlow<List<Category>> =
        operationRepository
            .getCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Earliest date the start date can be set to
    val firstOperationDate: StateFlow<LocalDate?> =
        operationRepository
            .getFirstOperationDate()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Counted over every operation, whatever the filters, so they show what is left to sort
    val uncategorizedCount: StateFlow<Int> =
        operationRepository
            .getUncategorizedCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val waitingForActionCount: StateFlow<Int> =
        operationRepository
            .getWaitingForActionCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // No category selected shows all of them
    private val _selectedCategoryIds = mutableStateOf(emptySet<Long>())
    val selectedCategoryIds: State<Set<Long>> = _selectedCategoryIds

    // Null dates don't limit the operations shown
    private val _startDate = mutableStateOf<LocalDate?>(null)
    val startDate: State<LocalDate?> = _startDate

    private val _endDate = mutableStateOf<LocalDate?>(null)
    val endDate: State<LocalDate?> = _endDate

    // Excludes selected categories, as no operation could be both without a category and in one of them
    private val _isUncategorizedOnly = mutableStateOf(false)
    val isUncategorizedOnly: State<Boolean> = _isUncategorizedOnly

    private val _isWaitingForActionOnly = mutableStateOf(false)
    val isWaitingForActionOnly: State<Boolean> = _isWaitingForActionOnly

    val hasFilters: State<Boolean> =
        derivedStateOf {
            _selectedCategoryIds.value.isNotEmpty() ||
                _startDate.value != null ||
                _endDate.value != null ||
                _isUncategorizedOnly.value ||
                _isWaitingForActionOnly.value
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val operations: StateFlow<List<Operation>> =
        snapshotFlow {
            OperationFilter(
                categoryIds = _selectedCategoryIds.value,
                start = _startDate.value,
                end = _endDate.value,
                isUncategorizedOnly = _isUncategorizedOnly.value,
                isWaitingForActionOnly = _isWaitingForActionOnly.value,
            )
        }.flatMapLatest(operationRepository::getOperations)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // The list is in selection mode as long as at least one operation is selected
    private val _selectedOperationIds = mutableStateOf(emptySet<Long>())
    val selectedOperationIds: State<Set<Long>> = _selectedOperationIds

    fun deleteOperation(operation: Operation) {
        viewModelScope.launch { operationRepository.deleteOperation(operation) }
    }

    // Changing a filter clears the selection, so operations hidden by it can't be deleted along with the visible ones
    fun toggleCategoryFilter(category: Category) {
        val selectedCategoryIds = _selectedCategoryIds.value
        _selectedCategoryIds.value =
            if (category.id in selectedCategoryIds) selectedCategoryIds - category.id else selectedCategoryIds + category.id
        _isUncategorizedOnly.value = false
        clearSelection()
    }

    fun clearCategoryFilter() {
        _selectedCategoryIds.value = emptySet()
        clearSelection()
    }

    fun toggleUncategorizedOnly() {
        _isUncategorizedOnly.value = !_isUncategorizedOnly.value
        if (_isUncategorizedOnly.value) _selectedCategoryIds.value = emptySet()
        clearSelection()
    }

    fun toggleWaitingForActionOnly() {
        _isWaitingForActionOnly.value = !_isWaitingForActionOnly.value
        clearSelection()
    }

    fun clearFilters() {
        _selectedCategoryIds.value = emptySet()
        _startDate.value = null
        _endDate.value = null
        _isUncategorizedOnly.value = false
        _isWaitingForActionOnly.value = false
        clearSelection()
    }

    fun setStartDate(date: LocalDate?) {
        _startDate.value = date
        clearSelection()
    }

    fun setEndDate(date: LocalDate?) {
        _endDate.value = date
        clearSelection()
    }

    // Shows a category's operations for a month, with dates kept within what the date pickers allow
    fun showCategoryMonth(
        categoryId: Long,
        month: YearMonth,
    ) {
        val monthStart = month.atDay(1)
        _selectedCategoryIds.value = setOf(categoryId)
        _startDate.value = firstOperationDate.value?.let { maxOf(it, monthStart) } ?: monthStart
        _endDate.value = minOf(month.atEndOfMonth(), LocalDate.now())
        _isUncategorizedOnly.value = false
        _isWaitingForActionOnly.value = false
        clearSelection()
    }

    fun toggleOperationSelection(operation: Operation) {
        val selectedOperationIds = _selectedOperationIds.value
        _selectedOperationIds.value =
            if (operation.id in selectedOperationIds) selectedOperationIds - operation.id else selectedOperationIds + operation.id
    }

    fun clearSelection() {
        _selectedOperationIds.value = emptySet()
    }

    fun deleteSelectedOperations() {
        val selectedOperationIds = _selectedOperationIds.value
        _selectedOperationIds.value = emptySet()
        viewModelScope.launch { operationRepository.deleteOperations(selectedOperationIds) }
    }
}
