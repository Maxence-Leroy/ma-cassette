package com.ragicorp.macassette.screens.settings

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.operation.Parser
import com.ragicorp.macassette.operation.SortingRule
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

    private val _statementUrl = mutableStateOf(Preferences.getStatementUrl(application))
    val statementUrl: State<String> = _statementUrl

    val categories: StateFlow<List<Category>> =
        operationRepository
            .getCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val parsers: StateFlow<List<Parser>> =
        operationRepository
            .getParsers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sortingRules: StateFlow<List<SortingRule>> =
        operationRepository
            .getSortingRules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteCategory(category: Category) {
        viewModelScope.launch { operationRepository.deleteCategory(category) }
    }

    fun deleteParser(parser: Parser) {
        viewModelScope.launch { operationRepository.deleteParser(parser) }
    }

    fun deleteSortingRule(sortingRule: SortingRule) {
        viewModelScope.launch { operationRepository.deleteSortingRule(sortingRule) }
    }

    fun setCurrency(currency: String) {
        Preferences.setCurrency(getApplication(), currency)
        _currency.value = Preferences.getCurrency(getApplication())
    }

    fun setStatementUrl(statementUrl: String) {
        Preferences.setStatementUrl(getApplication(), statementUrl)
        _statementUrl.value = Preferences.getStatementUrl(getApplication())
    }
}
