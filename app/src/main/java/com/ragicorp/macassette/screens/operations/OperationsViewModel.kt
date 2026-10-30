package com.ragicorp.macassette.screens.operations

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

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

    // No category selected shows all of them
    private val _selectedCategoryIds = mutableStateOf(emptySet<Long>())
    val selectedCategoryIds: State<Set<Long>> = _selectedCategoryIds

    // Null dates don't limit the operations shown
    private val _startDate = mutableStateOf<LocalDate?>(null)
    val startDate: State<LocalDate?> = _startDate

    private val _endDate = mutableStateOf<LocalDate?>(null)
    val endDate: State<LocalDate?> = _endDate

    @OptIn(ExperimentalCoroutinesApi::class)
    val operations: StateFlow<List<Operation>> =
        snapshotFlow { Triple(_selectedCategoryIds.value, _startDate.value, _endDate.value) }
            .flatMapLatest { (categoryIds, start, end) -> operationRepository.getOperations(categoryIds, start, end) }
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
        clearSelection()
    }

    fun clearCategoryFilter() {
        _selectedCategoryIds.value = emptySet()
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
