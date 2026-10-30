package com.ragicorp.macassette.screens.budget

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.threeten.bp.YearMonth

class BudgetViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    val categories: StateFlow<List<Category>> =
        operationRepository
            .getCategories()
            .map { categories -> categories.filter { it.budget != null } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Month of the first recorded operation, the earliest month that can be selected
    val firstMonth: StateFlow<YearMonth?> =
        operationRepository
            .getFirstOperationDate()
            .map { date -> date?.let(YearMonth::from) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _selectedMonth = mutableStateOf(YearMonth.now())
    val selectedMonth: State<YearMonth> = _selectedMonth

    fun selectPreviousMonth() {
        val firstMonth = firstMonth.value ?: return
        if (_selectedMonth.value > firstMonth) _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun selectNextMonth() {
        if (_selectedMonth.value < YearMonth.now()) _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }
}
