package com.ragicorp.macassette.screens.distribution

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.CategoryExpense
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import org.threeten.bp.YearMonth

class DistributionViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    // Earliest month that can be selected
    val firstMonth: StateFlow<YearMonth?> =
        operationRepository
            .getFirstOperationMonth()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _selectedMonth = mutableStateOf(YearMonth.now())
    val selectedMonth: State<YearMonth> = _selectedMonth

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<CategoryExpense>> =
        snapshotFlow { _selectedMonth.value }
            .flatMapLatest(operationRepository::getExpenses)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectPreviousMonth() {
        val firstMonth = firstMonth.value ?: return
        if (_selectedMonth.value > firstMonth) _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun selectNextMonth() {
        if (_selectedMonth.value < YearMonth.now()) _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }
}
