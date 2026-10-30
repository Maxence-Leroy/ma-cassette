package com.ragicorp.macassette.screens.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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
}
