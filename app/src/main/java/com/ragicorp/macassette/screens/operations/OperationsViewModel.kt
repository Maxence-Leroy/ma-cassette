package com.ragicorp.macassette.screens.operations

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OperationsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    val operations: StateFlow<List<Operation>> =
        operationRepository
            .getOperations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // The list is in selection mode as long as at least one operation is selected
    private val _selectedOperationIds = mutableStateOf(emptySet<Long>())
    val selectedOperationIds: State<Set<Long>> = _selectedOperationIds

    fun deleteOperation(operation: Operation) {
        viewModelScope.launch { operationRepository.deleteOperation(operation) }
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
