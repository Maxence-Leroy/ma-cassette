package com.ragicorp.macassette.screens.operations

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Operation
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.launch

class OperationsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    private val _operations = mutableStateOf<List<Operation>>(emptyList())
    val operations: State<List<Operation>> = _operations

    init {
        viewModelScope.launch {
            operationRepository.getOperations().collect { _operations.value = it }
        }
    }
}
