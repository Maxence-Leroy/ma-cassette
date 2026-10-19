package com.ragicorp.macassette.screens.settings.addcategory

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ragicorp.macassette.operation.Category
import com.ragicorp.macassette.operation.CategoryType
import com.ragicorp.macassette.operation.OperationRepository
import com.ragicorp.macassette.utils.AMOUNT_REGEX
import com.ragicorp.macassette.utils.Preferences
import kotlinx.coroutines.launch

private const val MAX_BUDGET = 9_999_999.99

class AddCategoryViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val operationRepository = OperationRepository(application)

    val currency = Preferences.getCurrency(application)

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _color = mutableStateOf<Int?>(null)
    val color: State<Int?> = _color

    private val _budget = mutableStateOf("")
    val budget: State<String> = _budget

    private val _type = mutableStateOf(CategoryType.Expense)
    val type: State<CategoryType> = _type

    val canSave: State<Boolean> = derivedStateOf { _name.value.isNotBlank() && _color.value != null }

    fun setName(name: String) {
        _name.value = name
    }

    fun setColor(color: Int) {
        _color.value = color
    }

    fun setBudget(budget: String) {
        if (AMOUNT_REGEX.matches(budget) && (budget.replace(',', '.').toDoubleOrNull() ?: 0.0) <= MAX_BUDGET) {
            _budget.value = budget
        }
    }

    fun setType(type: CategoryType) {
        _type.value = type
    }

    fun save(onSaved: () -> Unit) {
        val color = _color.value ?: return
        if (_name.value.isBlank()) return
        val category =
            Category(
                name = _name.value.trim(),
                color = color,
                // The budget field is only shown for expenses, so ignore any value left over from expense mode
                budget = if (_type.value == CategoryType.Expense) _budget.value.replace(',', '.').toDoubleOrNull() else null,
                type = _type.value,
            )
        viewModelScope.launch {
            operationRepository.insertCategory(category)
            onSaved()
        }
    }
}
