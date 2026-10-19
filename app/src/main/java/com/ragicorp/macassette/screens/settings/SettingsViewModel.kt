package com.ragicorp.macassette.screens.settings

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    private val _currency = mutableStateOf(Preferences.getCurrency(application))
    val currency: State<String> = _currency

    private val _categories = mutableStateOf<List<Category>>(emptyList())
    val categories: State<List<Category>> = _categories

    init {
        viewModelScope.launch {
            operationRepository.getCategories().collect { _categories.value = it }
        }
    }

    fun setCurrency(currency: String) {
        Preferences.setCurrency(getApplication(), currency)
        _currency.value = Preferences.getCurrency(getApplication())
    }
}
