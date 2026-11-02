package com.ragicorp.macassette.screens.balance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.MonthBalance
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val MONTH_COUNT = 12

class BalanceViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    // Null until loaded, so the empty message doesn't flash before the chart
    val monthBalances: StateFlow<List<MonthBalance>?> =
        operationRepository
            .getMonthBalances(MONTH_COUNT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
