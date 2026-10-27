package com.ragicorp.macassette.screens.settings

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    private val _currency = mutableStateOf(Preferences.getCurrency(application))
    val currency: State<String> = _currency

    val categories: StateFlow<List<Category>> =
        operationRepository
            .getCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteCategory(category: Category) {
        viewModelScope.launch { operationRepository.deleteCategory(category) }
    }

    fun setCurrency(currency: String) {
        Preferences.setCurrency(getApplication(), currency)
        _currency.value = Preferences.getCurrency(getApplication())
    }
}
